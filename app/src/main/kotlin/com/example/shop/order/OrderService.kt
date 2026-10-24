package com.example.shop.order

import com.example.shop.inventory.InventoryRepository
import com.example.shop.product.ProductRepository
import com.example.shop.shared.IdGenerator
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Clock
import java.util.UUID

/** 注文を作るのに要る値。Web のリクエストの形 (CreateOrderRequest) から切り離して受け取る。 */
data class CreateOrderCommand(
    val contactEmail: String,
    val lines: List<Line>,
) {
    data class Line(val productId: UUID, val quantity: Int)
}

/** 注文を作った結果。失敗は、利用者の注文しだいで普通に起こるので、例外でなくこの型で返す。 */
sealed interface CreateOrderResult {
    data class Created(val order: Order) : CreateOrderResult
    data class ProductNotFound(val productIds: List<UUID>) : CreateOrderResult
    data class InsufficientInventory(val shortages: List<Shortage>) : CreateOrderResult
}

/** 在庫が足りない明細。注文した数量と、その時点の在庫の数量を持つ。 */
data class Shortage(val productId: UUID, val requested: Int, val available: Int)

/** 注文を取り消した結果。 */
sealed interface CancelOrderResult {
    data class Cancelled(val order: Order) : CancelOrderResult
    data object NotFound : CancelOrderResult
    data object AlreadyCancelled : CancelOrderResult
}

@Service
class OrderService(
    private val orderRepository: OrderRepository,
    private val productRepository: ProductRepository,
    private val inventoryRepository: InventoryRepository,
    private val idGenerator: IdGenerator,
    private val clock: Clock,
) {

    /**
     * 注文を作り、明細ごとに在庫を引き当てる。
     *
     * すべての明細を確かめてから書き込む。sealed の失敗は例外でないので、トランザクションは commit される。
     * 書き込んだ後で失敗を返すと、その前の書き込みが残る。書き込みの途中で例外が出たときは、ロールバックで
     * すべて戻る (RuntimeException はロールバックの対象)。
     */
    @Transactional
    fun create(command: CreateOrderCommand): CreateOrderResult {
        val productIds = command.lines.map { it.productId }
        val products = productRepository.findAllById(productIds).associateBy { it.id }
        val missing = productIds.filterNot { it in products }
        if (missing.isNotEmpty()) {
            return CreateOrderResult.ProductNotFound(missing)
        }

        val inventories = inventoryRepository.findAllById(productIds).associateBy { it.productId }
        val allocations = command.lines.map { line ->
            // 商品には在庫の行を 1 つずつ持たせる決まりなので、在庫の行が無いのはデータの誤り。
            val inventory = inventories[line.productId] ?: error("在庫の行がありません: ${line.productId}")
            inventory to line.quantity
        }
        val shortages = allocations
            .filterNot { (inventory, quantity) -> inventory.canAllocate(quantity) }
            .map { (inventory, quantity) -> Shortage(inventory.productId, quantity, inventory.quantity) }
        if (shortages.isNotEmpty()) {
            return CreateOrderResult.InsufficientInventory(shortages)
        }

        allocations.forEach { (inventory, quantity) -> inventoryRepository.save(inventory.allocate(quantity)) }
        val order = orderRepository.save(
            Order(
                id = idGenerator.newId(),
                contactEmail = command.contactEmail,
                status = OrderStatus.PENDING,
                orderedAt = clock.instant(),
                lines = command.lines.map { line ->
                    val product = products.getValue(line.productId)
                    OrderLine(
                        productId = product.id,
                        quantity = line.quantity,
                        unitPrice = product.price,
                        taxRate = product.taxRate,
                    )
                },
            ),
        )
        return CreateOrderResult.Created(order)
    }

    /** 受付の注文を取り消し、明細ごとに引き当てを戻す。 */
    @Transactional
    fun cancel(id: UUID): CancelOrderResult {
        val order = orderRepository.findByIdOrNull(id) ?: return CancelOrderResult.NotFound
        if (!order.canCancel()) {
            return CancelOrderResult.AlreadyCancelled
        }
        order.lines.forEach { line ->
            val inventory = inventoryRepository.findByIdOrNull(line.productId)
                ?: error("在庫の行がありません: ${line.productId}")
            inventoryRepository.save(inventory.deallocate(line.quantity))
        }
        return CancelOrderResult.Cancelled(orderRepository.save(order.cancel()))
    }

    @Transactional(readOnly = true)
    fun find(id: UUID): Order? = orderRepository.findByIdOrNull(id)
}
