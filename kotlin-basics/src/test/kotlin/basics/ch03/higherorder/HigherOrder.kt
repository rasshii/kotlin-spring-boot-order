package basics.ch03.higherorder

import basics.assertSnippetOutput
import org.junit.jupiter.api.Test

// snippet:ch03-higher-order begin
fun applyDiscount(price: Int, rule: (Int) -> Int): Int {
    val discounted = rule(price)
    return if (discounted < 0) 0 else discounted
}

fun main() {
    println(applyDiscount(1000, { price -> price - 300 }))
    println(applyDiscount(1000) { it * 90 / 100 })
    println(applyDiscount(200) { it - 300 })
}
// snippet:ch03-higher-order end

class HigherOrderTest {
    @Test
    fun higherOrder() = assertSnippetOutput("ch03-higher-order") { main() }
}
