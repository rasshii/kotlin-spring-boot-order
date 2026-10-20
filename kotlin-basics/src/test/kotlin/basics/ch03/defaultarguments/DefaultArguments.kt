package basics.ch03.defaultarguments

import basics.assertSnippetOutput
import org.junit.jupiter.api.Test

// snippet:ch03-default-arguments begin
fun describeLine(
    productName: String,
    quantity: Int = 1,
    giftWrap: Boolean = false,
): String {
    val wrap = if (giftWrap) "あり" else "なし"
    return "$productName × $quantity (ギフト包装: $wrap)"
}

fun main() {
    println(describeLine("ノート"))
    println(describeLine("ノート", 3))
    println(describeLine("ノート", giftWrap = true))
    println(describeLine(quantity = 2, productName = "ペン"))
}
// snippet:ch03-default-arguments end

class DefaultArgumentsTest {
    @Test
    fun defaultArguments() = assertSnippetOutput("ch03-default-arguments") { main() }
}
