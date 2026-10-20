package basics.ch03.exerciseskipbefore

import basics.assertSnippetOutput
import org.junit.jupiter.api.Test

// snippet:ch03-exercise-skip-before begin
fun printLines(quantities: List<Int>) {
    quantities.forEach { quantity ->
        if (quantity == 0) return
        println("数量 $quantity")
    }
    println("表示を終えました")
}

fun main() {
    printLines(listOf(2, 0, 1))
}
// snippet:ch03-exercise-skip-before end

class ExerciseSkipBeforeTest {
    @Test
    fun exerciseSkipBefore() = assertSnippetOutput("ch03-exercise-skip-before") { main() }
}
