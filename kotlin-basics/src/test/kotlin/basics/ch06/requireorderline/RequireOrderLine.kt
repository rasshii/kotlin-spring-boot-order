package basics.ch06.requireorderline

import basics.assertSnippetOutput
import org.junit.jupiter.api.Test

// snippet:ch06-require-order-line begin
data class OrderLine(val productId: String, val quantity: Int, val unitPrice: Int) {
    init {
        require(quantity in 1..99) { "数量は 1 から 99 にしてください: $quantity" }
        require(unitPrice >= 0) { "単価は 0 以上にしてください: $unitPrice" }
    }
}
// snippet:ch06-require-order-line end

class RequireOrderLineTest {
    @Test
    fun requireUsage() = assertSnippetOutput("ch06-require-usage") {
        // snippet:ch06-require-usage begin
        println(OrderLine("P-001", quantity = 2, unitPrice = 1500))
        try {
            OrderLine("P-001", quantity = 0, unitPrice = 1500)
        } catch (e: IllegalArgumentException) {
            println("${e::class.simpleName}: ${e.message}")
        }
        // snippet:ch06-require-usage end
    }
}
