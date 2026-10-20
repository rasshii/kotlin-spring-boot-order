package basics.ch03.inlinefunction

import basics.assertSnippetOutput
import org.junit.jupiter.api.Test

// snippet:ch03-inline-function begin
inline fun step(label: String, block: () -> Unit) {
    println("開始: $label")
    block()
    println("終了: $label")
}

fun reserve(quantities: List<Int>): Boolean {
    step("在庫の引き当て") {
        for (quantity in quantities) {
            if (quantity <= 0) return false
        }
    }
    return true
}

fun main() {
    println(reserve(listOf(1, 2)))
    println(reserve(listOf(1, 0)))
}
// snippet:ch03-inline-function end

class InlineFunctionTest {
    @Test
    fun inlineFunction() = assertSnippetOutput("ch03-inline-function") { main() }
}
