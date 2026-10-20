package basics.ch05.genericclass

import basics.assertSnippetOutput
import basics.ch05.orderdata.notebook
import basics.ch05.orderdata.pen
import org.junit.jupiter.api.Test

// snippet:ch05-generic-class begin
data class Page<T>(val items: List<T>, val totalCount: Int) {
    fun <R> map(transform: (T) -> R): Page<R> = Page(items.map(transform), totalCount)
}

fun main() {
    val products = Page(listOf(notebook, pen), totalCount = 3)
    val names: Page<String> = products.map { it.name }
    println(names)
    println(products.items.first().price)
}
// snippet:ch05-generic-class end

class GenericClassTest {
    @Test
    fun page() = assertSnippetOutput("ch05-generic-class") { main() }
}
