package basics.ch03.extensionfunction

import basics.assertSnippetOutput
import org.junit.jupiter.api.Test

// snippet:ch03-extension-function begin
fun Int.withTax(): Int = this * 110 / 100

fun String.toOrderLabel(): String = "注文 $this"

fun main() {
    println(1000.withTax())
    println("A-1".toOrderLabel())
}
// snippet:ch03-extension-function end

class ExtensionFunctionTest {
    @Test
    fun extensionFunction() = assertSnippetOutput("ch03-extension-function") { main() }
}
