package basics.ch05.reified

import basics.assertSnippetOutput
import basics.ch05.orderdata.Inventory
import basics.ch05.orderdata.Product
import basics.ch05.orderdata.notebook
import basics.ch05.orderdata.pen
import org.junit.jupiter.api.Test

// snippet:ch05-reified begin
inline fun <reified T> List<*>.countOf(): Int = count { it is T }

inline fun <reified T : Any> typeName(): String = T::class.simpleName ?: "名前なし"

fun main() {
    val records: List<Any> = listOf(notebook, Inventory("P1", 10), pen)
    println(records.countOf<Product>())
    println(records.countOf<Inventory>())
    println(records.filterIsInstance<Inventory>())
    println(typeName<Product>())
}
// snippet:ch05-reified end

class ReifiedTest {
    @Test
    fun reified() = assertSnippetOutput("ch05-reified") { main() }
}
