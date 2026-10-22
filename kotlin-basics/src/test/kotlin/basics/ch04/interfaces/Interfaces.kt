package basics.ch04.interfaces

import basics.assertSnippetOutput
import org.junit.jupiter.api.Test

// snippet:ch04-interfaces begin
interface Priced {
    val price: Int
    fun priceWithTax(): Int = price * 110 / 100
    fun label(): String = "価格あり"
}

interface Shippable {
    val weightGrams: Int
    fun label(): String = "配送あり"
}

class Product(val name: String, override val price: Int, override val weightGrams: Int) : Priced, Shippable {
    override fun label(): String = "${super<Priced>.label()} ${super<Shippable>.label()}"
}

fun main() {
    val product = Product("ノート", 200, 150)
    println(product.priceWithTax())
    println(product.label())
}
// snippet:ch04-interfaces end

class InterfacesTest {
    @Test
    fun interfaces() = assertSnippetOutput("ch04-interfaces") { main() }
}
