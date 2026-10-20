package basics.ch04.customaccessor

import basics.assertSnippetOutput
import org.junit.jupiter.api.Test

// snippet:ch04-custom-accessor begin
class Inventory(val productName: String) {
    var quantity: Int = 0
        set(value) {
            require(value >= 0) { "在庫数は 0 以上: $value" }
            field = value
        }
    val isEmpty: Boolean
        get() = quantity == 0
}

fun main() {
    val inventory = Inventory("ボールペン")
    println(inventory.isEmpty)
    inventory.quantity = 5
    println(inventory.isEmpty)
    try { inventory.quantity = -1 } catch (e: IllegalArgumentException) { println(e.message) }
}
// snippet:ch04-custom-accessor end

class CustomAccessorTest {
    @Test
    fun customAccessor() = assertSnippetOutput("ch04-custom-accessor") { main() }
}
