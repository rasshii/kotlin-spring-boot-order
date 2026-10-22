package basics.ch04.propertydelegation

import basics.assertSnippetOutput
import org.junit.jupiter.api.Test
// snippet:ch04-property-delegation begin
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class NonNegative(private var current: Int) : ReadWriteProperty<Any?, Int> {
    override fun getValue(thisRef: Any?, property: KProperty<*>): Int = current
    override fun setValue(thisRef: Any?, property: KProperty<*>, value: Int) {
        require(value >= 0) { "${property.name} は 0 以上: $value" }
        current = value
    }
}

class Inventory {
    var quantity: Int by NonNegative(0)
    var reserved: Int by NonNegative(0)
}

fun main() {
    val inventory = Inventory()
    inventory.quantity = 10
    try { inventory.reserved = -1 } catch (e: IllegalArgumentException) { println(e.message) }
    println("${inventory.quantity} ${inventory.reserved}")
}
// snippet:ch04-property-delegation end

class PropertyDelegationTest {
    @Test
    fun propertyDelegation() = assertSnippetOutput("ch04-property-delegation") { main() }
}
