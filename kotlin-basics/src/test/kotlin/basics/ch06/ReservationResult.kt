package basics.ch06.reservationresult

import basics.assertSnippetOutput
import org.junit.jupiter.api.Test

// snippet:ch06-reservation-result begin
sealed interface ReservationResult {
    data class Reserved(val productId: String, val remaining: Int) : ReservationResult
    data class InsufficientInventory(
        val productId: String,
        val requested: Int,
        val available: Int,
    ) : ReservationResult
    data class ProductNotFound(val productId: String) : ReservationResult
}

class Inventory(val productId: String, available: Int) {
    var available = available
        private set

    fun reserve(quantity: Int): ReservationResult {
        require(quantity > 0) { "数量は 1 以上にしてください: $quantity" }
        if (available < quantity) {
            return ReservationResult.InsufficientInventory(productId, quantity, available)
        }
        available -= quantity
        return ReservationResult.Reserved(productId, available)
    }
}

fun reserve(inventories: Map<String, Inventory>, productId: String, quantity: Int): ReservationResult {
    val inventory = inventories[productId] ?: return ReservationResult.ProductNotFound(productId)
    return inventory.reserve(quantity)
}
// snippet:ch06-reservation-result end

class ReservationResultTest {
    @Test
    fun reservationWhen() = assertSnippetOutput("ch06-reservation-when") {
        // snippet:ch06-reservation-when begin
        val inventories = mapOf(
            "P-001" to Inventory("P-001", available = 5),
            "P-002" to Inventory("P-002", available = 1),
        )
        val requests = listOf("P-001" to 2, "P-002" to 3, "P-999" to 1)
        for ((productId, quantity) in requests) {
            val message = when (val result = reserve(inventories, productId, quantity)) {
                is ReservationResult.Reserved ->
                    "引き当てました: ${result.productId} 残り ${result.remaining}"
                is ReservationResult.InsufficientInventory ->
                    "在庫が足りません: ${result.productId} 注文 ${result.requested} 在庫 ${result.available}"
                is ReservationResult.ProductNotFound ->
                    "商品がありません: ${result.productId}"
            }
            println(message)
        }
        // snippet:ch06-reservation-when end
    }
}
