package exercises.ch12

import com.example.shop.order.Order
import com.example.shop.order.OrderLine
import com.example.shop.order.OrderStatus
import com.example.shop.order.TaxAmount
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.UUID

/** 第 12 章の練習問題 1 の解答例: 明細ごとに割り戻して足した消費税額。 */
fun taxAmountByLines(order: Order, taxRate: Int): Int =
    order.lines
        .filter { it.taxRate == taxRate }
        .sumOf { it.subtotal * taxRate / (100 + taxRate) }

class TaxRoundingTest {

    private fun line(productId: String, unitPrice: Int) =
        OrderLine(productId = UUID.fromString(productId), quantity = 1, unitPrice = unitPrice, taxRate = 8)

    // 価格 100 円 (軽減税率) の商品を 3 種類、1 個ずつ注文する。
    private val order = Order(
        id = UUID.fromString("019a1c00-0000-7000-8000-000000000001"),
        contactEmail = "taro@example.com",
        status = OrderStatus.PENDING,
        orderedAt = Instant.parse("2026-10-24T01:00:00Z"),
        lines = listOf(
            line("0199f3a8-0000-7000-8000-000000000101", 100),
            line("0199f3a8-0000-7000-8000-000000000102", 100),
            line("0199f3a8-0000-7000-8000-000000000103", 100),
        ),
    )

    @Test
    fun `税率ごとに割り戻すと、合計 300 円の消費税額は 22 円`() {
        assertEquals(listOf(TaxAmount(taxRate = 8, amount = 22)), order.taxAmounts)
    }

    @Test
    fun `明細ごとに割り戻して足すと、切り捨てが 3 回重なって 21 円になる`() {
        assertEquals(21, taxAmountByLines(order, taxRate = 8))
    }
}
