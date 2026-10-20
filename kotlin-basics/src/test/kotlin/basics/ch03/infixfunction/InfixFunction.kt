package basics.ch03.infixfunction

import basics.assertSnippetOutput
import org.junit.jupiter.api.Test

// snippet:ch03-infix-function begin
data class Inventory(val productName: String, val available: Int) {
    infix fun canFulfill(quantity: Int): Boolean = available >= quantity
}

fun main() {
    val note = Inventory("ノート", 5)
    println(note canFulfill 3)
    println(note.canFulfill(3))
    println(note canFulfill 2 + 4)
    println("ノート" to 3)
}
// snippet:ch03-infix-function end

class InfixFunctionTest {
    @Test
    fun infixFunction() = assertSnippetOutput("ch03-infix-function") { main() }
}
