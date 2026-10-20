package basics.ch03.exerciseskipafter

import basics.assertSnippetOutput
import org.junit.jupiter.api.Test

// snippet:ch03-exercise-skip-after begin
fun printLines(quantities: List<Int>) {
    quantities.forEach { quantity ->
        if (quantity == 0) return@forEach
        println("数量 $quantity")
    }
    println("表示を終えました")
}

fun main() {
    printLines(listOf(2, 0, 1))
}
// snippet:ch03-exercise-skip-after end

class ExerciseSkipAfterTest {
    @Test
    fun exerciseSkipAfter() = assertSnippetOutput("ch03-exercise-skip-after") { main() }
}
