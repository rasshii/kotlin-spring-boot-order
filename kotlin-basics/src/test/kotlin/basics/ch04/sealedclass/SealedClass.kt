package basics.ch04.sealedclass

import basics.assertSnippetOutput
import org.junit.jupiter.api.Test

// snippet:ch04-sealed-class begin
sealed class OrderState(val label: String) {
    data object Pending : OrderState("受付")
    data class Shipped(val trackingNumber: String) : OrderState("発送済み")
    data class Cancelled(val reason: String) : OrderState("取消")
}

fun detail(state: OrderState): String = when (state) {
    OrderState.Pending -> "発送を待っています"
    is OrderState.Shipped -> "追跡番号 ${state.trackingNumber}"
    is OrderState.Cancelled -> "理由: ${state.reason}"
}

fun main() {
    for (state in listOf(OrderState.Pending, OrderState.Shipped("TN-001"), OrderState.Cancelled("在庫切れ"))) {
        println("${state.label} ${detail(state)}")
    }
    println(OrderState.Pending)
}
// snippet:ch04-sealed-class end

class SealedClassTest {
    @Test
    fun sealedClass() = assertSnippetOutput("ch04-sealed-class") { main() }
}
