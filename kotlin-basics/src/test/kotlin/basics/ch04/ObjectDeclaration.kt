package basics.ch04.objectdeclaration

import basics.assertSnippetOutput
import org.junit.jupiter.api.Test

// snippet:ch04-object-declaration begin
object TaxRate {
    const val PERCENT = 10
    fun withTax(price: Int): Int = price * (100 + PERCENT) / 100
}

class Product private constructor(val name: String, val price: Int) {
    companion object {
        fun of(name: String, price: Int): Product {
            require(price >= 0) { "価格は 0 以上: $price" }
            return Product(name, price)
        }
    }
}

fun main() {
    println(TaxRate.withTax(1000))
    val product = Product.of("ノート", 200)
    println("${product.name} ${product.price}")
}
// snippet:ch04-object-declaration end

class ObjectDeclarationTest {
    @Test
    fun objectDeclaration() = assertSnippetOutput("ch04-object-declaration") { main() }
}
