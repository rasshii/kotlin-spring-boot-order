package basics.ch03.crossinlinelambda

import basics.assertSnippetOutput
import org.junit.jupiter.api.Test

// snippet:ch03-crossinline-lambda begin
inline fun afterCommit(crossinline action: () -> Unit): () -> Unit {
    return {
        println("コミットしました")
        action()
    }
}

fun main() {
    val notify = afterCommit { println("確認メールを送ります") }
    println("登録を続けます")
    notify()
}
// snippet:ch03-crossinline-lambda end

class CrossinlineLambdaTest {
    @Test
    fun crossinlineLambda() = assertSnippetOutput("ch03-crossinline-lambda") { main() }
}
