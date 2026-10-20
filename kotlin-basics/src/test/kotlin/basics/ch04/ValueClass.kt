package basics.ch04.valueclass

import basics.assertSnippetOutput
import org.junit.jupiter.api.Test

// snippet:ch04-value-class begin
@JvmInline
value class ProductId(val value: Long) {
    init { require(value > 0) { "商品 ID は正の数: $value" } }
}

@JvmInline
value class Quantity(val value: Int)

fun reserve(productId: ProductId, quantity: Quantity): String =
    "商品 ${productId.value} を ${quantity.value} 個引き当てます"

fun main() {
    println(reserve(ProductId(42), Quantity(3)))
    println(ProductId(42) == ProductId(42))
    try { ProductId(0) } catch (e: IllegalArgumentException) { println(e.message) }
}
// snippet:ch04-value-class end

class ValueClassTest {
    @Test
    fun valueClass() = assertSnippetOutput("ch04-value-class") { main() }
}
