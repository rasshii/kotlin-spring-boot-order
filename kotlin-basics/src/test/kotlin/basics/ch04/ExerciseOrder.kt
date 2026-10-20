package basics.ch04.exerciseorder

import basics.assertSnippetOutput
import org.junit.jupiter.api.Test

// snippet:ch04-exercise-order begin
data class OrderLine(val productName: String, val quantity: Int, val unitPrice: Int)

class Order(val id: Long) {
    private val _lines = mutableListOf<OrderLine>()
    val lines: List<OrderLine>
        get() = _lines
    val total: Int
        get() = _lines.sumOf { it.quantity * it.unitPrice }
    fun addLine(line: OrderLine) {
        require(line.quantity > 0) { "数量は 1 以上: ${line.quantity}" }
        _lines.add(line)
    }
}

fun main() {
    val order = Order(1L)
    order.addLine(OrderLine("ノート", 3, 200))
    order.addLine(OrderLine("消しゴム", 2, 80))
    try { order.addLine(OrderLine("ノート", -5, 200)) } catch (e: IllegalArgumentException) { println(e.message) }
    println("${order.lines.size} ${order.total}")
}
// snippet:ch04-exercise-order end

class ExerciseOrderTest {
    @Test
    fun exerciseOrder() = assertSnippetOutput("ch04-exercise-order") { main() }
}
