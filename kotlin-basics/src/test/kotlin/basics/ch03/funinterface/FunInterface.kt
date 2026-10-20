package basics.ch03.funinterface

import basics.assertSnippetOutput
import org.junit.jupiter.api.Test

// snippet:ch03-fun-interface begin
fun interface DiscountPolicy {
    fun discount(price: Int): Int
}

fun checkout(price: Int, policy: DiscountPolicy): Int = policy.discount(price)

fun main() {
    val memberDiscount = DiscountPolicy { price -> price * 95 / 100 }
    println(checkout(1000, memberDiscount))
    println(checkout(1000) { it - 100 })
}
// snippet:ch03-fun-interface end

class FunInterfaceTest {
    @Test
    fun funInterface() = assertSnippetOutput("ch03-fun-interface") { main() }
}
