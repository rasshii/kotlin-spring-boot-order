package basics.ch06.uncheckedexception

import basics.assertSnippetOutput
import org.junit.jupiter.api.Test

// snippet:ch06-unchecked-exception begin
class InsufficientInventoryException(message: String) : RuntimeException(message)

/**
 * 在庫の数 [available] から [quantity] 個を引き当て、残りの数を返す。
 *
 * @throws InsufficientInventoryException 在庫が [quantity] に足りないとき
 */
fun reserve(productId: String, available: Int, quantity: Int): Int {
    if (available < quantity) {
        throw InsufficientInventoryException("在庫が足りません: $productId")
    }
    return available - quantity
}

fun main() {
    println(reserve("P-001", available = 5, quantity = 2))
    try {
        reserve("P-002", available = 1, quantity = 3)
    } catch (e: InsufficientInventoryException) {
        println("${e::class.simpleName}: ${e.message}")
    }
}
// snippet:ch06-unchecked-exception end

class UncheckedExceptionTest {
    @Test
    fun uncheckedException() = assertSnippetOutput("ch06-unchecked-exception") { main() }
}
