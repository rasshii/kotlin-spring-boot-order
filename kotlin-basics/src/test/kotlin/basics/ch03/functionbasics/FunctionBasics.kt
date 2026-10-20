package basics.ch03.functionbasics

import basics.assertSnippetOutput
import org.junit.jupiter.api.Test

// snippet:ch03-function-basics begin
fun lineTotal(unitPrice: Int, quantity: Int): Int {
    return unitPrice * quantity
}

fun printLine(productName: String, quantity: Int) {
    println("$productName × $quantity")
}

fun main() {
    println(lineTotal(500, 3))
    printLine("ノート", 3)
}
// snippet:ch03-function-basics end

class FunctionBasicsTest {
    @Test
    fun functionBasics() = assertSnippetOutput("ch03-function-basics") { main() }
}
