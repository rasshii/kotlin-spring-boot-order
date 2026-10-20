package basics.ch03.nullablecallback

import basics.assertSnippetOutput
import org.junit.jupiter.api.Test

// snippet:ch03-nullable-callback begin
fun placeOrder(orderId: String, onPlaced: ((String) -> Unit)? = null) {
    println("$orderId を登録しました")
    onPlaced?.invoke(orderId)
}

fun main() {
    placeOrder("A-1")
    placeOrder("A-2") { println("$it の確認メールを送ります") }
}
// snippet:ch03-nullable-callback end

class NullableCallbackTest {
    @Test
    fun nullableCallback() = assertSnippetOutput("ch03-nullable-callback") { main() }
}
