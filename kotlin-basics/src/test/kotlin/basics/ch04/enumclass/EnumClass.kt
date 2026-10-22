package basics.ch04.enumclass

import basics.assertSnippetOutput
import org.junit.jupiter.api.Test

// snippet:ch04-enum-class begin
enum class OrderStatus(val label: String) {
    PENDING("受付"), SHIPPED("発送済み"), CANCELLED("取消");

    fun canCancel(): Boolean = this == PENDING
}

fun main() {
    val status = OrderStatus.valueOf("SHIPPED")
    println("${status.name} ${status.label} ${status.ordinal} ${status.canCancel()}")
    println(OrderStatus.entries.joinToString { it.label })
    println(when (status) {
        OrderStatus.PENDING -> "まだ取り消せます"
        OrderStatus.SHIPPED -> "発送済みです"
        OrderStatus.CANCELLED -> "取り消し済みです"
    })
}
// snippet:ch04-enum-class end

class EnumClassTest {
    @Test
    fun enumClass() = assertSnippetOutput("ch04-enum-class") { main() }
}
