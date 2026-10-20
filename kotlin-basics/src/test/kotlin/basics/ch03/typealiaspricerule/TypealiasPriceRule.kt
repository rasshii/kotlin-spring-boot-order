package basics.ch03.typealiaspricerule

import basics.assertSnippetOutput
import org.junit.jupiter.api.Test

// snippet:ch03-typealias-price-rule begin
typealias PriceRule = (Int) -> Int
typealias PriceTable = Map<String, Int>

fun priceOf(table: PriceTable, productName: String, rule: PriceRule): Int? {
    val base = table[productName] ?: return null
    return rule(base)
}

fun main() {
    val table: PriceTable = mapOf("ノート" to 500, "ペン" to 200)
    val halfPrice: PriceRule = { it / 2 }
    println(priceOf(table, "ノート", halfPrice))
    println(priceOf(table, "消しゴム", halfPrice))

    val plain: (Int) -> Int = halfPrice
    println(plain(300))
}
// snippet:ch03-typealias-price-rule end

class TypealiasPriceRuleTest {
    @Test
    fun typealiasPriceRule() = assertSnippetOutput("ch03-typealias-price-rule") { main() }
}
