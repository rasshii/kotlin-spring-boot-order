package basics.ch02.practicewhenanswer

import basics.assertSnippetOutput
import org.junit.jupiter.api.Test

// snippet:ch02-practice-when-answer begin
fun discountPercent(quantity: Int): Int {
    return when {
        quantity <= 0 -> throw IllegalArgumentException("数量は 1 以上にしてください: $quantity")
        quantity < 10 -> 0
        quantity < 50 -> 5
        else -> 10
    }
}

fun main() {
    for (quantity in listOf(1, 10, 50)) {
        println(discountPercent(quantity))
    }
    try {
        discountPercent(0)
    } catch (e: IllegalArgumentException) {
        println(e.message)
    }
}
// snippet:ch02-practice-when-answer end

class PracticeWhenAnswerTest {
    @Test
    fun practiceWhenAnswer() = assertSnippetOutput("ch02-practice-when-answer") { main() }
}
