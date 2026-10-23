package com.example.shop.order

import com.example.shop.TestcontainersConfiguration
import com.example.shop.product.Product
import com.example.shop.product.ProductRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.data.repository.findByIdOrNull
import org.springframework.transaction.annotation.Transactional
import java.time.Clock
import java.time.Instant
import java.util.UUID

@Import(TestcontainersConfiguration::class)
@SpringBootTest
@Transactional
class OrderRepositoryTest(
    @Autowired private val orderRepository: OrderRepository,
    @Autowired private val productRepository: ProductRepository,
    @Autowired private val clock: Clock,
) {

    private fun saveNotebook(): Product =
        productRepository.save(
            Product(id = UUID.fromString("0199f3a8-0000-7000-8000-000000000001"), name = "ノート", price = 330, taxRate = 10),
        )

    private fun newOrder(product: Product, orderedAt: Instant) = Order(
        id = UUID.fromString("019a1c00-0000-7000-8000-000000000001"),
        contactEmail = "taro@example.com",
        status = OrderStatus.PENDING,
        orderedAt = orderedAt,
        lines = listOf(OrderLine(productId = product.id, quantity = 2, unitPrice = 330, taxRate = 10)),
    )

    @Test
    fun `保存した注文を、注文明細ごと読み戻せる`() {
        val order = newOrder(saveNotebook(), orderedAt = Instant.parse("2026-10-24T01:00:00Z"))

        val saved = orderRepository.save(order)
        val found = orderRepository.findByIdOrNull(order.id)

        assertEquals(order.copy(version = 0), saved)
        assertEquals(saved, found)
    }

    @Test
    fun `時計の時刻で作った注文は、読み戻しても日時が変わらない`() {
        val order = newOrder(saveNotebook(), orderedAt = clock.instant())

        orderRepository.save(order)

        assertEquals(order.orderedAt, orderRepository.findByIdOrNull(order.id)?.orderedAt)
    }
}
