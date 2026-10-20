package basics.ch03.extensionproperty

import basics.assertSnippetOutput
import org.junit.jupiter.api.Test

// snippet:ch03-extension-property begin
data class OrderLine(val productName: String, val unitPrice: Int, val quantity: Int)

val OrderLine.subtotal: Int
    get() = unitPrice * quantity

fun main() {
    val line = OrderLine("ノート", 500, 3)
    println(line.subtotal)
}
// snippet:ch03-extension-property end

class ExtensionPropertyTest {
    @Test
    fun extensionProperty() = assertSnippetOutput("ch03-extension-property") { main() }
}
