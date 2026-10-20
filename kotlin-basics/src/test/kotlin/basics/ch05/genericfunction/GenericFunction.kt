package basics.ch05.genericfunction

import basics.assertSnippetOutput
import org.junit.jupiter.api.Test

// snippet:ch05-generic-function begin
fun <T> List<T>.secondOrNull(): T? = if (size >= 2) this[1] else null

fun <T : Comparable<T>> larger(a: T, b: T): T = if (a >= b) a else b

fun <T> smallestNonBlank(items: List<T>): T?
    where T : CharSequence, T : Comparable<T> =
    items.sorted().firstOrNull { it.isNotBlank() }

fun main() {
    println(listOf("ノート", "ペン").secondOrNull())
    println(listOf(1).secondOrNull())
    println(larger(3, 7))
    println(larger("P1", "P2"))
    println(smallestNonBlank(listOf("ペン", " ", "ノート")))
}
// snippet:ch05-generic-function end

class GenericFunctionTest {
    @Test
    fun genericFunctions() = assertSnippetOutput("ch05-generic-function") { main() }
}
