package basics.ch01

import basics.assertSnippetOutput
import org.junit.jupiter.api.Test

class Ch01SnippetsTest {
    @Test
    fun statements() = assertSnippetOutput("ch01-statements") {
        // snippet:ch01-statements begin
        val product = "ノート"
        val quantity = 3
        println(product)
        println(quantity * 2)
        // snippet:ch01-statements end
    }
}
