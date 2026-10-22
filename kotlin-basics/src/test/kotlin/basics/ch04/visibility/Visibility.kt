package basics.ch04.visibility

import basics.assertSnippetOutput
import org.junit.jupiter.api.Test

// snippet:ch04-visibility begin
class Inventory(val productName: String, initial: Int) {
    var quantity: Int = initial
        private set
    fun reserve(amount: Int): Boolean {
        if (amount > quantity) return false
        quantity -= amount
        return true
    }
}

internal fun describe(inventory: Inventory): String = "${inventory.productName}: 残り ${inventory.quantity}"

fun main() {
    val inventory = Inventory("ノート", 10)
    println(inventory.reserve(3))
    println(inventory.reserve(20))
    println(describe(inventory))
}
// snippet:ch04-visibility end

class VisibilityTest {
    @Test
    fun visibility() = assertSnippetOutput("ch04-visibility") { main() }
}
