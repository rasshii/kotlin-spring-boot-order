package basics.ch03.scoperunwith

import basics.assertSnippetOutput
import org.junit.jupiter.api.Test

// snippet:ch03-scope-run-with begin
data class OrderLine(val productName: String, val unitPrice: Int, val quantity: Int)

fun main() {
    val line = OrderLine("ノート", 500, 3)

    val summary = with(line) {
        "$productName × $quantity、小計 ${unitPrice * quantity} 円"
    }
    println(summary)

    val subtotal = line.run { unitPrice * quantity }
    println(subtotal)

    val total = run {
        val shippingFee = 500
        subtotal + shippingFee
    }
    println(total)
}
// snippet:ch03-scope-run-with end

class ScopeRunWithTest {
    @Test
    fun scopeRunWith() = assertSnippetOutput("ch03-scope-run-with") { main() }
}
