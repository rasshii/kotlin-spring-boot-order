package basics.ch05.exercisefirstoftype

import basics.assertSnippetOutput
import basics.ch05.orderdata.Inventory
import basics.ch05.orderdata.Product
import basics.ch05.orderdata.notebook
import basics.ch05.orderdata.pen
import org.junit.jupiter.api.Test

// snippet:ch05-exercise-first-of-type begin
inline fun <reified T> List<*>.firstOfType(): T? = firstOrNull { it is T } as T?

fun main() {
    val records: List<Any> = listOf(Inventory("P1", 10), pen, notebook)
    println(records.firstOfType<Product>())
    println(records.firstOfType<String>())
}
// snippet:ch05-exercise-first-of-type end

class ExerciseFirstOfTypeTest {
    @Test
    fun firstOfType() = assertSnippetOutput("ch05-exercise-first-of-type") { main() }
}
