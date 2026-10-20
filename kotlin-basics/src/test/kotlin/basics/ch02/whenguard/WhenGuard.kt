package basics.ch02.whenguard

import basics.assertSnippetOutput
import org.junit.jupiter.api.Test

// snippet:ch02-when-guard begin
fun toQuantity(value: Any): Int {
    return when (value) {
        is Int if value > 0 -> value
        is String -> value.trim().toIntOrNull() ?: 0
        else -> 0
    }
}

fun main() {
    println(toQuantity(4))
    println(toQuantity(-2))
    println(toQuantity(" 7 "))
    println(toQuantity("七"))
}
// snippet:ch02-when-guard end

class WhenGuardTest {
    @Test
    fun whenGuard() = assertSnippetOutput("ch02-when-guard") { main() }
}
