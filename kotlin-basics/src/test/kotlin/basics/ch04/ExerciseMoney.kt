package basics.ch04.exercisemoney

import basics.assertSnippetOutput
import org.junit.jupiter.api.Test

// snippet:ch04-exercise-money begin
@JvmInline
value class Money(val amount: Long) {
    operator fun plus(other: Money): Money = Money(amount + other.amount)
    operator fun times(quantity: Int): Money = Money(amount * quantity)
}

data class OrderLine(val productName: String, val unitPrice: Money, val quantity: Int) {
    val amount: Money
        get() = unitPrice * quantity
}

fun main() {
    val lines = listOf(OrderLine("ノート", Money(200), 3), OrderLine("消しゴム", Money(80), 2))
    var total = Money(0)
    for (line in lines) total += line.amount
    println(total.amount)
}
// snippet:ch04-exercise-money end

class ExerciseMoneyTest {
    @Test
    fun exerciseMoney() = assertSnippetOutput("ch04-exercise-money") { main() }
}
