package basics.ch05

import basics.assertSnippetOutput
import basics.ch05.orderdata.Order
import basics.ch05.orderdata.OrderLine
import basics.ch05.orderdata.eraser
import basics.ch05.orderdata.inventories
import basics.ch05.orderdata.lines
import basics.ch05.orderdata.notebook
import basics.ch05.orderdata.pen
import org.junit.jupiter.api.Test

/** 05 章のうち、ch05-order-data の注文明細を集計するコード例 (コレクションの操作と Sequence)。 */
class OrderLineOperationsTest {

    @Test
    fun mapAndFilter() = assertSnippetOutput("ch05-map-filter") {
        // snippet:ch05-map-filter begin
        val subtotals = lines.map { it.subtotal }
        println(subtotals)

        val bulkLines = lines.filter { it.quantity >= 3 }
        println(bulkLines.map { it.product.name })

        println(lines.sumOf { it.subtotal })
        println(lines.mapNotNull { if (it.quantity >= 3) it.product.id else null })
        // snippet:ch05-map-filter end
    }

    @Test
    fun predicates() = assertSnippetOutput("ch05-predicates") {
        // snippet:ch05-predicates begin
        println(lines.any { it.quantity > 4 })
        println(lines.all { it.quantity > 0 })
        println(lines.none { it.product == pen })
        println(lines.count { it.product == notebook })
        println(lines.find { it.product == eraser }?.quantity)
        println(lines.firstOrNull { it.product.price > 1000 })
        // snippet:ch05-predicates end
    }

    @Test
    fun sorting() = assertSnippetOutput("ch05-sort") {
        // snippet:ch05-sort begin
        println(lines.sortedBy { it.quantity }.map { it.quantity })
        println(lines.sortedByDescending { it.subtotal }.map { it.subtotal })

        val byIdThenQuantity = lines.sortedWith(
            compareBy<OrderLine> { it.product.id }.thenBy { it.quantity },
        )
        println(byIdThenQuantity.map { "${it.product.id}:${it.quantity}" })

        println(lines.maxBy { it.subtotal }.product.name)
        // snippet:ch05-sort end
    }

    @Test
    fun groupBy() = assertSnippetOutput("ch05-group-by") {
        // snippet:ch05-group-by begin
        val linesByName = lines.groupBy { it.product.name }
        println(linesByName.keys)
        println(linesByName.getValue("ノート").map { it.quantity })

        val quantityByName = lines
            .groupBy({ it.product.name }, { it.quantity })
            .mapValues { (_, quantities) -> quantities.sum() }
        println(quantityByName)
        // snippet:ch05-group-by end
    }

    @Test
    fun associate() = assertSnippetOutput("ch05-associate") {
        // snippet:ch05-associate begin
        val products = listOf(notebook, pen, eraser)

        val productById = products.associateBy { it.id }
        println(productById.getValue("P2").name)

        val priceByName = products.associate { it.name to it.price }
        println(priceByName)

        val lineCount = products.associateWith { product -> lines.count { it.product == product } }
        println(lineCount.mapKeys { it.key.name })

        val inventoryById = inventories.associateBy { it.productId }
        val shortLines = lines.filter { it.quantity > inventoryById.getValue(it.product.id).quantity }
        println(shortLines.map { it.product.name })
        // snippet:ch05-associate end
    }

    @Test
    fun fold() = assertSnippetOutput("ch05-fold") {
        // snippet:ch05-fold begin
        val total = lines.fold(0) { acc, line -> acc + line.subtotal }
        println(total)

        val quantityById = lines
            .groupingBy { it.product.id }
            .fold(0) { acc, line -> acc + line.quantity }
        println(quantityById)
        println(lines.groupingBy { it.product.id }.eachCount())

        val largest = lines.map { it.subtotal }.reduce { acc, subtotal -> maxOf(acc, subtotal) }
        println(largest)
        println(emptyList<Int>().reduceOrNull { acc, n -> acc + n })
        // snippet:ch05-fold end
    }

    @Test
    fun flatMapAndOthers() = assertSnippetOutput("ch05-flat-map") {
        // snippet:ch05-flat-map begin
        val orders = listOf(
            Order("O1", listOf(OrderLine(notebook, 3), OrderLine(pen, 2))),
            Order("O2", listOf(OrderLine(notebook, 1), OrderLine(eraser, 5))),
        )
        println(orders.flatMap { it.lines }.map { it.product.id })
        println(orders.map { order -> order.id to order.lines.sumOf { it.subtotal } })

        val (bulk, small) = lines.partition { it.quantity >= 3 }
        println(bulk.map { it.quantity })
        println(small.map { it.quantity })
        println(lines.map { it.product.id }.distinct())
        println(lines.take(2).map { it.product.id })
        // snippet:ch05-flat-map end
    }

    @Test
    fun listIsEager() = assertSnippetOutput("ch05-list-eager") {
        // snippet:ch05-list-eager begin
        val firstSmall = lines
            .map {
                println("map: ${it.product.id}")
                it.subtotal
            }
            .filter {
                println("filter: $it")
                it < 300
            }
            .first()
        println(firstSmall)
        // snippet:ch05-list-eager end
    }

    @Test
    fun sequenceIsLazy() = assertSnippetOutput("ch05-sequence-lazy") {
        // snippet:ch05-sequence-lazy begin
        val firstSmall = lines.asSequence()
            .map {
                println("map: ${it.product.id}")
                it.subtotal
            }
            .filter {
                println("filter: $it")
                it < 300
            }
            .first()
        println(firstSmall)
        // snippet:ch05-sequence-lazy end
    }

    @Test
    fun sequenceTerminal() = assertSnippetOutput("ch05-sequence-terminal") {
        // snippet:ch05-sequence-terminal begin
        val subtotals = lines.asSequence().map {
            println("map: ${it.product.id}")
            it.subtotal
        }
        println("toList の前")
        println(subtotals.toList())

        val orderIds = generateSequence(1) { it + 1 }.map { "O$it" }
        println(orderIds.take(3).toList())
        // snippet:ch05-sequence-terminal end
    }

    @Test
    fun exerciseShortInventory() = assertSnippetOutput("ch05-exercise-short-inventory") {
        // snippet:ch05-exercise-short-inventory begin
        val orderLines = listOf(OrderLine(eraser, 3), OrderLine(eraser, 4), OrderLine(pen, 1))
        val inventoryById = inventories.associateBy { it.productId }

        val shortIds = orderLines
            .groupingBy { it.product.id }
            .fold(0) { acc, line -> acc + line.quantity }
            .filter { (productId, quantity) -> quantity > inventoryById.getValue(productId).quantity }
            .keys
        println(shortIds)
        // snippet:ch05-exercise-short-inventory end
    }
}
