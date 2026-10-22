package basics.ch04.objectexpression

import basics.assertSnippetOutput
import org.junit.jupiter.api.Test

// snippet:ch04-object-expression begin
interface PriceRule { fun adjust(price: Int): Int }

fun main() {
    var applied = 0
    val halfPrice = object : PriceRule {
        override fun adjust(price: Int): Int {
            applied++
            return price / 2
        }
    }
    println(halfPrice.adjust(1000))
    println(halfPrice.adjust(300))
    println(applied)
}
// snippet:ch04-object-expression end

class ObjectExpressionTest {
    @Test
    fun objectExpression() = assertSnippetOutput("ch04-object-expression") { main() }
}
