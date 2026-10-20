package basics.ch02.unit

import basics.assertSnippetOutput
import org.junit.jupiter.api.Test

// snippet:ch02-unit begin
fun printInventory(productCode: String, quantity: Int) {
    println("$productCode の在庫: $quantity")
}

fun main() {
    val result = printInventory("P-001", 12)
    println(result)
}
// snippet:ch02-unit end

class UnitReturnTest {
    @Test
    fun unitReturn() = assertSnippetOutput("ch02-unit") { main() }
}
