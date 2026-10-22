package basics.ch04.nestedinner

import basics.assertSnippetOutput
import org.junit.jupiter.api.Test

// snippet:ch04-nested-inner begin
class Order(val id: Long) {
    class Summary(val lineCount: Int, val total: Int)

    inner class Note(val text: String) {
        fun render(): String = "注文 $id: $text"
    }
}

fun main() {
    println(Order.Summary(lineCount = 2, total = 680).total)
    val note = Order(7L).Note("午前中に配達")
    println(note.render())
}
// snippet:ch04-nested-inner end

class NestedInnerTest {
    @Test
    fun nestedInner() = assertSnippetOutput("ch04-nested-inner") { main() }
}
