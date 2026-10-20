package basics.ch01.hello

import basics.assertSnippetOutput
import org.junit.jupiter.api.Test

// snippet:ch01-hello begin
fun main() {
    println("こんにちは、Kotlin")
}
// snippet:ch01-hello end

class HelloTest {
    @Test
    fun hello() = assertSnippetOutput("ch01-hello") { main() }
}
