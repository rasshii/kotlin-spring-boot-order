package basics.ch03.singleexpression

import basics.assertSnippetOutput
import org.junit.jupiter.api.Test

// snippet:ch03-single-expression begin
fun lineTotal(unitPrice: Int, quantity: Int): Int = unitPrice * quantity

fun shippingFee(subtotal: Int) = if (subtotal >= 5000) 0 else 500

fun main() {
    println(lineTotal(500, 3))
    println(shippingFee(1500))
    println(shippingFee(6000))
}
// snippet:ch03-single-expression end

class SingleExpressionTest {
    @Test
    fun singleExpression() = assertSnippetOutput("ch03-single-expression") { main() }
}
