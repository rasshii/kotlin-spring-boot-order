package basics.ch03.noinlinelambda

import basics.assertSnippetOutput
import org.junit.jupiter.api.Test

// snippet:ch03-noinline-lambda begin
inline fun placeOrder(
    orderId: String,
    tasks: MutableList<() -> Unit>,
    check: () -> Boolean,
    noinline onPlaced: () -> Unit,
) {
    if (!check()) {
        println("$orderId は登録できません")
        return
    }
    println("$orderId を登録しました")
    tasks.add(onPlaced)
}

fun main() {
    val tasks = mutableListOf<() -> Unit>()
    placeOrder("A-1", tasks, { true }) { println("A-1 の確認メールを送ります") }
    placeOrder("A-2", tasks, { false }) { println("A-2 の確認メールを送ります") }
    println("登録の後で、残した処理を実行します")
    for (task in tasks) {
        task()
    }
}
// snippet:ch03-noinline-lambda end

class NoinlineLambdaTest {
    @Test
    fun noinlineLambda() = assertSnippetOutput("ch03-noinline-lambda") { main() }
}
