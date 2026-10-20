package basics.ch02.practicenullquestion

import org.junit.jupiter.api.Test

// snippet:ch02-practice-null-question begin
fun lineTotal(unitPrice: Int?, quantityText: String): Int {
    return unitPrice!! * quantityText.toInt()
}
// snippet:ch02-practice-null-question end

/** 練習問題の書き直す前の関数。記事に実行結果を載せないので、値の揃った入力で動くことだけを確かめる。 */
class PracticeNullQuestionTest {
    @Test
    fun practiceNullQuestion() {
        lineTotal(1200, "3")
    }
}
