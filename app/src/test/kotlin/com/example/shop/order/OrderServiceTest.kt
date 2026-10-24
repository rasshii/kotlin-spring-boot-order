package com.example.shop.order

import com.example.shop.TestcontainersConfiguration
import com.example.shop.inventory.Inventory
import com.example.shop.inventory.InventoryRepository
import com.example.shop.product.Product
import com.example.shop.product.ProductRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.data.repository.findByIdOrNull
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

/** 注文の作成と取り消しが、在庫をどう変えるか。テストの終わりにロールバックするので、テストどうしは影響しない。 */
@Import(TestcontainersConfiguration::class)
@SpringBootTest
@Transactional
class OrderServiceTest(
    @Autowired private val orderService: OrderService,
    @Autowired private val productRepository: ProductRepository,
    @Autowired private val inventoryRepository: InventoryRepository,
) {
    private val notebook = UUID.fromString("0199f3a8-0000-7000-8000-000000000001")
    private val marker = UUID.fromString("0199f3a8-0000-7000-8000-000000000004")

    @BeforeEach
    fun insertProducts() {
        productRepository.save(Product(id = notebook, name = "ノート", price = 330, taxRate = 10))
        productRepository.save(Product(id = marker, name = "マーカー", price = 880, taxRate = 10))
        inventoryRepository.save(Inventory(productId = notebook, quantity = 50))
        inventoryRepository.save(Inventory(productId = marker, quantity = 3))
    }

    private fun inventoryQuantity(productId: UUID): Int? = inventoryRepository.findByIdOrNull(productId)?.quantity

    private fun command(vararg lines: Pair<UUID, Int>) = CreateOrderCommand(
        contactEmail = "taro@example.com",
        lines = lines.map { (productId, quantity) -> CreateOrderCommand.Line(productId, quantity) },
    )

    @Test
    fun `注文を作ると、明細ごとに在庫を引き当てる`() {
        val result = orderService.create(command(notebook to 2, marker to 1))

        assertInstanceOf(CreateOrderResult.Created::class.java, result)
        assertEquals(48, inventoryQuantity(notebook))
        assertEquals(2, inventoryQuantity(marker))
    }

    @Test
    fun `1 つでも在庫が足りない明細があれば、どの明細も引き当てない`() {
        val result = orderService.create(command(notebook to 2, marker to 5))

        assertEquals(
            CreateOrderResult.InsufficientInventory(listOf(Shortage(productId = marker, requested = 5, available = 3))),
            result,
        )
        assertEquals(50, inventoryQuantity(notebook))
        assertEquals(3, inventoryQuantity(marker))
    }

    @Test
    fun `取り消すと、明細ごとに引き当てを戻す`() {
        val created = orderService.create(command(notebook to 2, marker to 1)) as CreateOrderResult.Created

        val result = orderService.cancel(created.order.id)

        assertEquals(OrderStatus.CANCELLED, (result as CancelOrderResult.Cancelled).order.status)
        assertEquals(50, inventoryQuantity(notebook))
        assertEquals(3, inventoryQuantity(marker))
    }

    @Test
    fun `取消の注文をもう一度取り消すと、引き当てを戻さずに失敗を返す`() {
        val created = orderService.create(command(notebook to 2)) as CreateOrderResult.Created
        orderService.cancel(created.order.id)

        val result = orderService.cancel(created.order.id)

        assertEquals(CancelOrderResult.AlreadyCancelled, result)
        assertEquals(50, inventoryQuantity(notebook))
    }
}
