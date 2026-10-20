package basics.ch04.openclass

import basics.assertSnippetOutput
import org.junit.jupiter.api.Test

// snippet:ch04-open-class begin
open class Product(val name: String, val price: Int) {
    open fun shippingFee(): Int = 500
    fun describe(): String = "$name ${price}円 送料 ${shippingFee()}円"
}

class DigitalProduct(name: String, price: Int) : Product(name, price) {
    override fun shippingFee(): Int = 0
}

class HeavyProduct(name: String, price: Int) : Product(name, price) {
    override fun shippingFee(): Int = super.shippingFee() + 300
}

fun main() {
    for (product in listOf(Product("ノート", 200), DigitalProduct("電子書籍", 1500), HeavyProduct("米 10kg", 4000))) {
        println(product.describe())
    }
}
// snippet:ch04-open-class end

class OpenClassTest {
    @Test
    fun openClass() = assertSnippetOutput("ch04-open-class") { main() }
}
