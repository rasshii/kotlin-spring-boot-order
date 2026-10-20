package basics.ch04.operatoroverloading

import basics.assertSnippetOutput
import org.junit.jupiter.api.Test

// snippet:ch04-operator-overloading begin
data class Money(val amount: Long) {
    operator fun plus(other: Money): Money = Money(amount + other.amount)
    operator fun times(count: Int): Money = Money(amount * count)
    operator fun compareTo(other: Money): Int = amount.compareTo(other.amount)
}

class Cart {
    private val quantities = mutableMapOf<String, Int>()
    operator fun get(productName: String): Int = quantities[productName] ?: 0
    operator fun contains(productName: String): Boolean = productName in quantities
    operator fun plusAssign(productName: String) { quantities[productName] = this[productName] + 1 }
}

fun main() {
    val total = Money(200) * 3 + Money(80)
    println(total)
    println(total > Money(600))
    val cart = Cart()
    cart += "ノート"
    cart += "ノート"
    println(cart["ノート"])
    println("消しゴム" in cart)
}
// snippet:ch04-operator-overloading end

class OperatorOverloadingTest {
    @Test
    fun operatorOverloading() = assertSnippetOutput("ch04-operator-overloading") { main() }
}
