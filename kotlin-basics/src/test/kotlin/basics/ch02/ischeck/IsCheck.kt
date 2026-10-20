package basics.ch02.ischeck

import basics.assertSnippetOutput
import org.junit.jupiter.api.Test

// snippet:ch02-is-check begin
fun toQuantity(value: Any): Int {
    if (value is Int) {
        return value
    }
    if (value !is String) {
        return 0
    }
    return value.toIntOrNull() ?: 0
}

fun main() {
    println(toQuantity(3))
    println(toQuantity("4"))
    println(toQuantity(2.5))
}
// snippet:ch02-is-check end

class IsCheckTest {
    @Test
    fun isCheck() = assertSnippetOutput("ch02-is-check") { main() }
}
