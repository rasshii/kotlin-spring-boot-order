package basics.ch03.scopelet

import basics.assertSnippetOutput
import org.junit.jupiter.api.Test

// snippet:ch03-scope-let begin
fun couponDiscount(couponCode: String?): Int =
    couponCode?.let { code -> if (code.startsWith("SPRING")) 300 else 100 } ?: 0

fun main() {
    println(couponDiscount("SPRING2026"))
    println(couponDiscount("WELCOME"))
    println(couponDiscount(null))
}
// snippet:ch03-scope-let end

class ScopeLetTest {
    @Test
    fun scopeLet() = assertSnippetOutput("ch03-scope-let") { main() }
}
