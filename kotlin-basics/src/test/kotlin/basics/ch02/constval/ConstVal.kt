package basics.ch02.constval

import basics.assertSnippetOutput
import org.junit.jupiter.api.Test

// snippet:ch02-const-val begin
const val MAX_LINES_PER_ORDER = 100

fun main() {
    val lineCount = 120
    println(MAX_LINES_PER_ORDER)
    println(lineCount > MAX_LINES_PER_ORDER)
}
// snippet:ch02-const-val end

class ConstValTest {
    @Test
    fun constVal() = assertSnippetOutput("ch02-const-val") { main() }
}
