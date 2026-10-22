package basics.ch04.classdelegation

import basics.assertSnippetOutput
import org.junit.jupiter.api.Test

// snippet:ch04-class-delegation begin
interface InventoryRepository {
    fun quantityOf(productName: String): Int
    fun save(productName: String, quantity: Int)
}

class InMemoryInventoryRepository : InventoryRepository {
    private val quantities = mutableMapOf<String, Int>()
    override fun quantityOf(productName: String): Int = quantities[productName] ?: 0
    override fun save(productName: String, quantity: Int) { quantities[productName] = quantity }
}

class LoggingInventoryRepository(private val inner: InventoryRepository) : InventoryRepository by inner {
    override fun save(productName: String, quantity: Int) {
        println("保存: $productName = $quantity")
        inner.save(productName, quantity)
    }
}

fun main() {
    val repository = LoggingInventoryRepository(InMemoryInventoryRepository())
    repository.save("ノート", 30)
    println(repository.quantityOf("ノート"))
}
// snippet:ch04-class-delegation end

class ClassDelegationTest {
    @Test
    fun classDelegation() = assertSnippetOutput("ch04-class-delegation") { main() }
}
