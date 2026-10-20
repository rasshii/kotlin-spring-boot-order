package basics.ch03.variablearguments

import basics.assertSnippetOutput
import org.junit.jupiter.api.Test

// snippet:ch03-variable-arguments begin
fun totalQuantity(vararg quantities: Int): Int {
    var total = 0
    for (quantity in quantities) {
        total += quantity
    }
    return total
}

fun main() {
    println(totalQuantity())
    println(totalQuantity(2, 3))
    val fromCart = intArrayOf(1, 4, 5)
    println(totalQuantity(*fromCart))
    println(totalQuantity(10, *fromCart))
}
// snippet:ch03-variable-arguments end

class VariableArgumentsTest {
    @Test
    fun variableArguments() = assertSnippetOutput("ch03-variable-arguments") { main() }
}
