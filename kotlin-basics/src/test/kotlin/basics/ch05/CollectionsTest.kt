package basics.ch05

import basics.assertSnippetOutput
import org.junit.jupiter.api.Test

/** 05 章のうち、題材のデータを使わないコード例 (配列、コレクションの作り方、Set と Map の操作、List の共変)。 */
class CollectionsTest {

    @Test
    fun arrays() = assertSnippetOutput("ch05-arrays") {
        // snippet:ch05-arrays begin
        val quantities = intArrayOf(4, 2, 1)
        quantities[1] = 5
        println(quantities.size)
        println(quantities[0])
        println(quantities.contentToString())
        println(quantities.sum())

        val names: Array<String> = arrayOf("ノート", "ペン")
        println(names.joinToString())

        val squares = IntArray(4) { it * it }
        println(squares.contentToString())

        val other = intArrayOf(4, 5, 1)
        println(quantities == other)
        println(quantities.contentEquals(other))
        // snippet:ch05-arrays end
    }

    @Test
    fun createCollections() = assertSnippetOutput("ch05-create-collections") {
        // snippet:ch05-create-collections begin
        val productNames = listOf("ノート", "ペン", "ノート")
        val uniqueNames = setOf("ノート", "ペン", "ノート")
        val prices = mapOf("ノート" to 200, "ペン" to 150)

        println(productNames)
        println(uniqueNames)
        println(prices)
        println(productNames[1])
        println(productNames.getOrNull(5))
        println("ペン" in uniqueNames)
        println(prices["ペン"])
        println(prices["消しゴム"])
        // snippet:ch05-create-collections end
    }

    @Test
    fun mutableCollections() = assertSnippetOutput("ch05-mutable-collections") {
        // snippet:ch05-mutable-collections begin
        val cart = mutableListOf("ノート")
        cart.add("ペン")
        cart += "消しゴム"
        cart.remove("ノート")
        println(cart)

        val reserved = mutableMapOf<String, Int>()
        reserved["P1"] = 3
        reserved["P1"] = (reserved["P1"] ?: 0) + 2
        reserved.getOrPut("P2") { 1 }
        println(reserved)

        val names = buildList {
            add("ノート")
            addAll(cart)
        }
        println(names)

        var history = listOf("ノート")
        val before = history
        history += "ペン"
        println(before)
        println(history)
        // snippet:ch05-mutable-collections end
    }

    @Test
    fun readOnlyView() = assertSnippetOutput("ch05-read-only-view") {
        // snippet:ch05-read-only-view begin
        val cart = mutableListOf("ノート")
        val view: List<String> = cart
        val copy = cart.toList()
        cart.add("ペン")
        println(view)
        println(copy)
        // snippet:ch05-read-only-view end
    }

    @Test
    fun setOperations() = assertSnippetOutput("ch05-set-operations") {
        // snippet:ch05-set-operations begin
        val orderedIds = setOf("P1", "P2", "P3")
        val discontinuedIds = setOf("P3", "P9")

        println(orderedIds intersect discontinuedIds)
        println(orderedIds subtract discontinuedIds)
        println(orderedIds union setOf("P4"))
        println(orderedIds - "P1")
        println(listOf("P1", "P2", "P1").toSet())
        // snippet:ch05-set-operations end
    }

    @Test
    fun mapOperations() = assertSnippetOutput("ch05-map-operations") {
        // snippet:ch05-map-operations begin
        val inventory = mapOf("P1" to 10, "P2" to 1, "P3" to 5)

        for ((productId, quantity) in inventory) {
            println("$productId: $quantity")
        }
        println(inventory.getOrElse("P9") { 0 })
        println(inventory.filterValues { it < 5 })
        println(inventory.mapValues { (_, quantity) -> quantity * 2 })
        println(inventory + ("P4" to 3))
        println(inventory - "P1")
        // snippet:ch05-map-operations end
    }

    @Test
    fun listCovariance() = assertSnippetOutput("ch05-list-covariance") {
        // snippet:ch05-list-covariance begin
        val names: List<String> = listOf("ノート", "ペン")
        val values: List<Any> = names
        println(values)

        val cart: MutableList<String> = mutableListOf("ノート")
        val readOnly: List<Any> = cart
        println(readOnly)
        // snippet:ch05-list-covariance end
    }
}
