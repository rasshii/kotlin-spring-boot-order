// snippet:ch06-when-missing-branch begin
sealed interface ReservationResult {
    data class Reserved(val productId: String, val remaining: Int) : ReservationResult
    data class InsufficientInventory(
        val productId: String,
        val requested: Int,
        val available: Int,
    ) : ReservationResult
    data class ProductNotFound(val productId: String) : ReservationResult
    data class Discontinued(val productId: String) : ReservationResult
}

fun describe(result: ReservationResult): String = when (result) {
    is ReservationResult.Reserved -> "引き当てました: ${result.productId}"
    is ReservationResult.InsufficientInventory -> "在庫が足りません: ${result.productId}"
    is ReservationResult.ProductNotFound -> "商品がありません: ${result.productId}"
}
// snippet:ch06-when-missing-branch end
