package basics.ch03.exerciseordertotal

import basics.assertSnippetOutput
import org.junit.jupiter.api.Test

// snippet:ch03-exercise-order-total begin
data class OrderLine(val productName: String, val unitPrice: Int, val quantity: Int)

val OrderLine.subtotal: Int
    get() = unitPrice * quantity

fun orderTotal(lines: List<OrderLine>, shippingFee: Int = 500): Int {
    var total = 0
    for (line in lines) {
        total += line.subtotal
    }
    return total + shippingFee
}

fun main() {
    val lines = listOf(OrderLine("ノート", 500, 3), OrderLine("ペン", 200, 2))
    println(orderTotal(lines))
    println(orderTotal(lines, shippingFee = 0))
}
// snippet:ch03-exercise-order-total end

class ExerciseOrderTotalTest {
    @Test
    fun exerciseOrderTotal() = assertSnippetOutput("ch03-exercise-order-total") { main() }
}
