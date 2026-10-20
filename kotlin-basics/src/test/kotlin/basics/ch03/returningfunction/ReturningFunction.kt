package basics.ch03.returningfunction

import basics.assertSnippetOutput
import org.junit.jupiter.api.Test

// snippet:ch03-returning-function begin
fun discountOf(percent: Int): (Int) -> Int = { price -> price * (100 - percent) / 100 }

fun main() {
    val tenPercentOff = discountOf(10)
    val halfPrice = discountOf(50)
    println(tenPercentOff(1000))
    println(halfPrice(1000))
}
// snippet:ch03-returning-function end

class ReturningFunctionTest {
    @Test
    fun returningFunction() = assertSnippetOutput("ch03-returning-function") { main() }
}
