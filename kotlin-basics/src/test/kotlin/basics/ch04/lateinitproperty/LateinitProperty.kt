package basics.ch04.lateinitproperty

import basics.assertSnippetOutput
import org.junit.jupiter.api.Test

// snippet:ch04-lateinit-property begin
class OrderMailer {
    lateinit var sender: String

    fun send(orderId: Long) {
        val message = if (this::sender.isInitialized) "$sender から注文 $orderId を送ります" else "送信元が未設定です"
        println(message)
    }
}

fun main() {
    val mailer = OrderMailer()
    mailer.send(1L)
    mailer.sender = "shop@example.com"
    mailer.send(1L)
    try { println(OrderMailer().sender) } catch (e: UninitializedPropertyAccessException) { println(e::class.simpleName) }
}
// snippet:ch04-lateinit-property end

class LateinitPropertyTest {
    @Test
    fun lateinitProperty() = assertSnippetOutput("ch04-lateinit-property") { main() }
}
