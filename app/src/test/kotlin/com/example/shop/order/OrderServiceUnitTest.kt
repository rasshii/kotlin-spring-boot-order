package com.example.shop.order

import com.example.shop.inventory.Inventory
import com.example.shop.inventory.InventoryRepository
import com.example.shop.product.Product
import com.example.shop.product.ProductRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import io.mockk.verifyOrder
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import java.util.UUID

/**
 * OrderService の分岐と書き込みの順を、Spring もデータベースも使わずに確かめる。repository は MockK の mock にする。
 *
 * トランザクション (途中の例外で書き込みが戻ること) と SQL は、このテストでは確かめられない。
 * OrderServiceTest と OrderServiceRollbackTest が、Spring とデータベースを通して確かめる。
 */
class OrderServiceUnitTest {
    private val orderRepository = mockk<OrderRepository>()
    private val productRepository = mockk<ProductRepository>()
    private val inventoryRepository = mockk<InventoryRepository>()

    private val orderId = UUID.fromString("019a1c00-0000-7000-8000-000000000001")
    private val orderedAt = Instant.parse("2026-10-24T01:00:00Z")
    private val service = OrderService(
        orderRepository = orderRepository,
        productRepository = productRepository,
        inventoryRepository = inventoryRepository,
        idGenerator = { orderId },
        clock = Clock.fixed(orderedAt, ZoneOffset.UTC),
    )

    private val notebook = Product(id = UUID.fromString("0199f3a8-0000-7000-8000-000000000001"), name = "ノート", price = 330, taxRate = 10)
    private val marker = Product(id = UUID.fromString("0199f3a8-0000-7000-8000-000000000004"), name = "マーカー", price = 880, taxRate = 10)

    private fun command(vararg lines: Pair<Product, Int>) = CreateOrderCommand(
        contactEmail = "taro@example.com",
        lines = lines.map { (product, quantity) -> CreateOrderCommand.Line(product.id, quantity) },
    )

    /** ノートの在庫は 50、マーカーは 3。 */
    private fun stubProductsAndInventories() {
        every { productRepository.findAllById(any()) } returns listOf(notebook, marker)
        every { inventoryRepository.findAllById(any()) } returns listOf(
            Inventory(productId = notebook.id, quantity = 50, version = 0),
            Inventory(productId = marker.id, quantity = 3, version = 0),
        )
    }

    @Test
    fun `在庫が足りれば、明細ごとに引き当ててから注文を保存する`() {
        stubProductsAndInventories()
        every { inventoryRepository.save(any<Inventory>()) } answers { firstArg() }
        val savedOrder = slot<Order>()
        every { orderRepository.save(capture(savedOrder)) } answers { savedOrder.captured }

        service.create(command(notebook to 2, marker to 1))

        verifyOrder {
            inventoryRepository.save(Inventory(productId = notebook.id, quantity = 48, version = 0))
            inventoryRepository.save(Inventory(productId = marker.id, quantity = 2, version = 0))
            orderRepository.save(any<Order>())
        }
        assertEquals(
            listOf(
                OrderLine(productId = notebook.id, quantity = 2, unitPrice = 330, taxRate = 10),
                OrderLine(productId = marker.id, quantity = 1, unitPrice = 880, taxRate = 10),
            ),
            savedOrder.captured.lines,
        )
        assertEquals(orderedAt, savedOrder.captured.orderedAt)
    }

    @Test
    fun `どれかの明細の在庫が足りなければ、どの在庫も注文も保存しない`() {
        stubProductsAndInventories()
        // save にも答えを決めておき、呼ばれたかどうかを verify で判定する。答えが無いと、呼ばれた時点で mock が例外を
        // 投げ、verify まで届かない。
        every { inventoryRepository.save(any<Inventory>()) } answers { firstArg() }
        every { orderRepository.save(any<Order>()) } answers { firstArg() }

        val result = service.create(command(notebook to 2, marker to 5))

        assertEquals(CreateOrderResult.InsufficientInventory(listOf(Shortage(marker.id, requested = 5, available = 3))), result)
        verify(exactly = 0) { inventoryRepository.save(any<Inventory>()) }
        verify(exactly = 0) { orderRepository.save(any<Order>()) }
    }
}
