package basics.ch03.scopeapplyalso

import basics.assertSnippetOutput
import org.junit.jupiter.api.Test

// snippet:ch03-scope-apply-also begin
data class OrderDraft(var deliveryNote: String = "", var giftWrap: Boolean = false)

fun main() {
    val draft = OrderDraft().apply {
        deliveryNote = "置き配"
        giftWrap = true
    }
    println(draft)

    val confirmed = draft.also { println("確定する下書き: ${it.deliveryNote}") }
    println(confirmed === draft)
}
// snippet:ch03-scope-apply-also end

class ScopeApplyAlsoTest {
    @Test
    fun scopeApplyAlso() = assertSnippetOutput("ch03-scope-apply-also") { main() }
}
