package basics.ch03.receiverfunctiontype

import basics.assertSnippetOutput
import org.junit.jupiter.api.Test

// snippet:ch03-receiver-function-type begin
fun buildLabel(build: StringBuilder.() -> Unit): String {
    val builder = StringBuilder()
    builder.build()
    return builder.toString()
}

fun main() {
    val label = buildLabel {
        append("注文 A-1")
        append("、")
        append("明細 3 件")
    }
    println(label)
}
// snippet:ch03-receiver-function-type end

class ReceiverFunctionTypeTest {
    @Test
    fun receiverFunctionType() = assertSnippetOutput("ch03-receiver-function-type") { main() }
}
