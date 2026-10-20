package basics.ch02.nothing

import basics.assertSnippetOutput
import org.junit.jupiter.api.Test

// snippet:ch02-nothing begin
fun rejectOrder(reason: String): Nothing {
    throw IllegalArgumentException(reason)
}

fun parseQuantity(input: String): Int {
    val quantity = input.toIntOrNull() ?: rejectOrder("数量が数字でない: $input")
    return quantity
}

fun main() {
    println(parseQuantity("3"))
    try {
        parseQuantity("三")
    } catch (e: IllegalArgumentException) {
        println(e.message)
    }
}
// snippet:ch02-nothing end

class NothingReturnTest {
    @Test
    fun nothingReturn() = assertSnippetOutput("ch02-nothing") { main() }
}
