package basics.ch06.exercisequantityanswer

import basics.assertSnippetOutput
import org.junit.jupiter.api.Test

// snippet:ch06-exercise-quantity-answer begin
sealed interface QuantityInput {
    data class Valid(val quantity: Int) : QuantityInput
    data class NotANumber(val text: String) : QuantityInput
    data class OutOfRange(val quantity: Int) : QuantityInput
}

fun parseQuantity(text: String): QuantityInput {
    val quantity = text.toIntOrNull() ?: return QuantityInput.NotANumber(text)
    if (quantity !in 1..99) return QuantityInput.OutOfRange(quantity)
    return QuantityInput.Valid(quantity)
}

fun main() {
    for (text in listOf("3", "2個", "120")) {
        println(parseQuantity(text))
    }
}
// snippet:ch06-exercise-quantity-answer end

class ExerciseQuantityAnswerTest {
    @Test
    fun exerciseQuantityAnswer() = assertSnippetOutput("ch06-exercise-quantity-answer") { main() }
}
