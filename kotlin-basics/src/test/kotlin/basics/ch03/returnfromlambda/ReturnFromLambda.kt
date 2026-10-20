package basics.ch03.returnfromlambda

import basics.assertSnippetOutput
import org.junit.jupiter.api.Test

// snippet:ch03-return-from-lambda begin
fun firstShortage(availableQuantities: List<Int>, required: Int): Int? {
    availableQuantities.forEach { available ->
        if (available < required) return available
    }
    return null
}

fun main() {
    println(firstShortage(listOf(10, 2, 5), 3))
    println(firstShortage(listOf(10, 8), 3))
}
// snippet:ch03-return-from-lambda end

class ReturnFromLambdaTest {
    @Test
    fun returnFromLambda() = assertSnippetOutput("ch03-return-from-lambda") { main() }
}
