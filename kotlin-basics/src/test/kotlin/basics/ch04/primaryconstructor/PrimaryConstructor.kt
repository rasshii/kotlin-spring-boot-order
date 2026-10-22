package basics.ch04.primaryconstructor

import basics.assertSnippetOutput
import org.junit.jupiter.api.Test

// snippet:ch04-primary-constructor begin
class Product(val name: String, val price: Int)

class OrderLine(val product: Product, val quantity: Int = 1) {
    val amount: Int = product.price * quantity
    init {
        require(quantity > 0) { "数量は 1 以上: $quantity" }
    }
    constructor(name: String, price: Int, quantity: Int) : this(Product(name, price), quantity)
}

fun main() {
    val pen = Product("ボールペン", 120)
    println(OrderLine(pen, 3).amount)
    println(OrderLine(pen).quantity)
    println(OrderLine("ノート", 200, 2).amount)
    try { OrderLine(pen, 0) } catch (e: IllegalArgumentException) { println(e.message) }
}
// snippet:ch04-primary-constructor end

class PrimaryConstructorTest {
    @Test
    fun primaryConstructor() = assertSnippetOutput("ch04-primary-constructor") { main() }
}
