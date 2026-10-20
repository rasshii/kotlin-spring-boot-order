package basics.ch06.checkorder

import basics.assertSnippetOutput
import org.junit.jupiter.api.Test

// snippet:ch06-check-order begin
enum class OrderStatus { DRAFT, CONFIRMED }

class Order(val id: String) {
    var status = OrderStatus.DRAFT
        private set

    fun confirm() {
        check(status == OrderStatus.DRAFT) { "下書きの注文だけを確定できます: $id は $status" }
        status = OrderStatus.CONFIRMED
    }
}
// snippet:ch06-check-order end

class CheckOrderTest {
    @Test
    fun checkUsage() = assertSnippetOutput("ch06-check-usage") {
        // snippet:ch06-check-usage begin
        val order = Order("O-001")
        order.confirm()
        println(order.status)
        try {
            order.confirm()
        } catch (e: IllegalStateException) {
            println("${e::class.simpleName}: ${e.message}")
        }
        // snippet:ch06-check-usage end
    }
}
