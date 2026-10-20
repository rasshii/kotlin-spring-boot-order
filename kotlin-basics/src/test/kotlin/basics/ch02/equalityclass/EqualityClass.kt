package basics.ch02.equalityclass

import basics.assertSnippetOutput
import org.junit.jupiter.api.Test

// snippet:ch02-equality-class begin
class Product(val code: String)

fun main() {
    val first = Product("P-001")
    val second = Product("P-001")
    val same = first
    println(first == second)
    println(first === same)
}
// snippet:ch02-equality-class end

class EqualityClassTest {
    @Test
    fun equalityClass() = assertSnippetOutput("ch02-equality-class") { main() }
}
