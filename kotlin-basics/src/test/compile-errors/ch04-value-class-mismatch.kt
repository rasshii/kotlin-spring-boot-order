@JvmInline
value class ProductId(val value: Long)

@JvmInline
value class Quantity(val value: Int)

fun reserve(productId: ProductId, quantity: Quantity): String =
    "商品 ${productId.value} を ${quantity.value} 個引き当てます"

fun main() {
    // snippet:ch04-value-class-mismatch begin
    reserve(Quantity(3), ProductId(42))
    // snippet:ch04-value-class-mismatch end
}
