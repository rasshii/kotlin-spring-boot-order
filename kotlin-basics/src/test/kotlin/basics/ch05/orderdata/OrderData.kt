package basics.ch05.orderdata

// snippet:ch05-order-data begin
data class Product(val id: String, val name: String, val price: Int)

data class OrderLine(val product: Product, val quantity: Int) {
    val subtotal: Int
        get() = product.price * quantity
}

data class Order(val id: String, val lines: List<OrderLine>)

data class Inventory(val productId: String, val quantity: Int)

val notebook = Product("P1", "ノート", 200)
val pen = Product("P2", "ペン", 150)
val eraser = Product("P3", "消しゴム", 80)

val lines = listOf(
    OrderLine(notebook, 3),
    OrderLine(pen, 2),
    OrderLine(notebook, 1),
    OrderLine(eraser, 5),
)

val inventories = listOf(
    Inventory("P1", 10),
    Inventory("P2", 1),
    Inventory("P3", 5),
)
// snippet:ch05-order-data end
