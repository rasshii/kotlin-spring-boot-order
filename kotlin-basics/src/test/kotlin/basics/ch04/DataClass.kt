package basics.ch04.dataclass

import basics.assertSnippetOutput
import org.junit.jupiter.api.Test

// snippet:ch04-data-class begin
data class OrderLine(val productName: String, val quantity: Int, val unitPrice: Int)

fun main() {
    val line = OrderLine("ノート", 2, 200)
    println(line)
    println(line == OrderLine("ノート", 2, 200))
    val more = line.copy(quantity = 5)
    println(more)

    val (name, quantity) = more
    println("$name x $quantity")
    for ((productName, count) in listOf(line, more)) {
        println("$productName $count")
    }
}
// snippet:ch04-data-class end

class DataClassTest {
    @Test
    fun dataClass() = assertSnippetOutput("ch04-data-class") { main() }
}
