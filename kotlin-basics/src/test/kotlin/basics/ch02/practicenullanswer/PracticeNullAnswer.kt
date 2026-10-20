package basics.ch02.practicenullanswer

import basics.assertSnippetOutput
import org.junit.jupiter.api.Test

// snippet:ch02-practice-null-answer begin
fun lineTotal(unitPrice: Int?, quantityText: String): Int {
    val quantity = quantityText.toIntOrNull() ?: return 0
    return (unitPrice ?: 0) * quantity
}

fun main() {
    println(lineTotal(1200, "3"))
    println(lineTotal(null, "3"))
    println(lineTotal(1200, "三"))
}
// snippet:ch02-practice-null-answer end

class PracticeNullAnswerTest {
    @Test
    fun practiceNullAnswer() = assertSnippetOutput("ch02-practice-null-answer") { main() }
}
