package basics.ch04.abstractclass

import basics.assertSnippetOutput
import org.junit.jupiter.api.Test

// snippet:ch04-abstract-class begin
abstract class Discount(val label: String) {
    abstract fun discountedPrice(price: Int): Int
    fun describe(price: Int): String = "$label: $price -> ${discountedPrice(price)}"
}

class PercentDiscount(private val percent: Int) : Discount("${percent}% 引き") {
    override fun discountedPrice(price: Int): Int = price * (100 - percent) / 100
}

class FixedDiscount(private val amount: Int) : Discount("${amount} 円引き") {
    override fun discountedPrice(price: Int): Int = maxOf(price - amount, 0)
}

fun main() {
    for (discount in listOf(PercentDiscount(10), FixedDiscount(300))) {
        println(discount.describe(1000))
    }
}
// snippet:ch04-abstract-class end

class AbstractClassTest {
    @Test
    fun abstractClass() = assertSnippetOutput("ch04-abstract-class") { main() }
}
