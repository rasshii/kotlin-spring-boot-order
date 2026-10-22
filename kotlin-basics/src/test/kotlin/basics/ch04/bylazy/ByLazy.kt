package basics.ch04.bylazy

import basics.assertSnippetOutput
import org.junit.jupiter.api.Test

// snippet:ch04-by-lazy begin
class OrderSummary(private val amounts: List<Int>) {
    val total: Int by lazy {
        println("合計を計算します")
        amounts.sum()
    }
}

fun main() {
    val summary = OrderSummary(listOf(600, 80, 1200))
    println("作成しました")
    println(summary.total)
    println(summary.total)
}
// snippet:ch04-by-lazy end

class ByLazyTest {
    @Test
    fun byLazy() = assertSnippetOutput("ch04-by-lazy") { main() }
}
