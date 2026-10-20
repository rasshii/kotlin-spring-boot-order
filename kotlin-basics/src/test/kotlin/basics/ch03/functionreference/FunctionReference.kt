package basics.ch03.functionreference

import basics.assertSnippetOutput
import org.junit.jupiter.api.Test

// snippet:ch03-function-reference begin
data class Product(val name: String, val price: Int)

fun isAffordable(product: Product): Boolean = product.price <= 1000

fun main() {
    val products = listOf(Product("ノート", 500), Product("万年筆", 12000), Product("ペン", 200))

    val check: (Product) -> Boolean = ::isAffordable
    println(check(products[1]))
    println(products.filter(::isAffordable))
    println(products.map(Product::name))

    val note = products[0]
    val priceOfNote: () -> Int = note::price
    println(priceOfNote())

    val create: (String, Int) -> Product = ::Product
    println(create("消しゴム", 100))
}
// snippet:ch03-function-reference end

class FunctionReferenceTest {
    @Test
    fun functionReference() = assertSnippetOutput("ch03-function-reference") { main() }
}
