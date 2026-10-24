package com.example.shop.product

import com.example.shop.TestcontainersConfiguration
import com.example.shop.inventory.Inventory
import com.example.shop.inventory.InventoryRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Import(TestcontainersConfiguration::class)
@SpringBootTest
@Transactional
class ProductSummaryQueryTest(
    @Autowired private val query: ProductSummaryQuery,
    @Autowired private val productRepository: ProductRepository,
    @Autowired private val inventoryRepository: InventoryRepository,
) {

    @Test
    fun `商品を在庫の数と一緒に、ID の順に読む`() {
        val pen = UUID.fromString("0199f3a8-0000-7000-8000-000000000002")
        val notebook = UUID.fromString("0199f3a8-0000-7000-8000-000000000001")
        productRepository.save(Product(id = pen, name = "ボールペン", price = 150, taxRate = 10))
        productRepository.save(Product(id = notebook, name = "ノート", price = 330, taxRate = 10))
        inventoryRepository.save(Inventory(productId = pen, quantity = 100))
        inventoryRepository.save(Inventory(productId = notebook, quantity = 50))

        assertEquals(
            listOf(
                ProductSummary(id = notebook, name = "ノート", price = 330, taxRate = 10, inventoryQuantity = 50),
                ProductSummary(id = pen, name = "ボールペン", price = 150, taxRate = 10, inventoryQuantity = 100),
            ),
            query.findAll(),
        )
    }

    private val pen = UUID.fromString("0199f3a8-0000-7000-8000-000000000002")
    private val eraser = UUID.fromString("0199f3a8-0000-7000-8000-000000000007")
    private val sticky = UUID.fromString("0199f3a8-0000-7000-8000-000000000003")
    private val notebook = UUID.fromString("0199f3a8-0000-7000-8000-000000000001")
    private val tea = UUID.fromString("0199f3a8-0000-7000-8000-000000000006")

    /**
     * 価格の順はボールペンと消しゴム (同じ 150 円、ID の順)・付箋・ノート・緑茶。緑茶だけ在庫が 0。
     * 消しゴムをボールペンより先に入れる。入れた順と ID の順が同じだと、入れた順のまま返った行も ID の順に見える。
     */
    private fun insertProducts() {
        listOf(
            Triple(notebook, "ノート", 330),
            Triple(eraser, "消しゴム", 150),
            Triple(sticky, "付箋", 220),
            Triple(tea, "緑茶", 540),
            Triple(pen, "ボールペン", 150),
        ).forEach { (id, name, price) ->
            productRepository.save(Product(id = id, name = name, price = price, taxRate = 10))
            inventoryRepository.save(Inventory(productId = id, quantity = if (id == tea) 0 else 10))
        }
    }

    @Test
    fun `価格の順に、続きの位置の次から読み、最後のページでは続きの位置が無い`() {
        insertProducts()

        val first = query.findPage(ProductSort.PRICE, after = null, allocatableOnly = false, limit = 2)
        val second = query.findPage(ProductSort.PRICE, after = first.next, allocatableOnly = false, limit = 2)
        val third = query.findPage(ProductSort.PRICE, after = second.next, allocatableOnly = false, limit = 2)

        assertEquals(listOf(pen, eraser), first.items.map { it.id })
        assertEquals(listOf(sticky, notebook), second.items.map { it.id })
        assertEquals(listOf(tea), third.items.map { it.id })
        assertNull(third.next)
    }

    @Test
    fun `ID の順に、続きの位置の次から読む`() {
        insertProducts()

        val first = query.findPage(ProductSort.ID, after = null, allocatableOnly = false, limit = 2)
        val second = query.findPage(ProductSort.ID, after = first.next, allocatableOnly = false, limit = 2)
        val third = query.findPage(ProductSort.ID, after = second.next, allocatableOnly = false, limit = 2)

        assertEquals(listOf(notebook, pen), first.items.map { it.id })
        assertEquals(listOf(sticky, tea), second.items.map { it.id })
        assertEquals(listOf(eraser), third.items.map { it.id })
        assertNull(third.next)
    }

    @Test
    fun `同じ価格の商品がページの境目にあっても、重ならず抜けない`() {
        insertProducts()

        // 続きの位置が進まない誤りでも終わるように、商品の数より多い 10 ページで打ち切る。
        val ids = generateSequence(query.findPage(ProductSort.PRICE, after = null, allocatableOnly = false, limit = 1)) { page ->
            page.next?.let { query.findPage(ProductSort.PRICE, after = it, allocatableOnly = false, limit = 1) }
        }.take(10).flatMap { it.items.map(ProductSummary::id) }.toList()

        assertEquals(listOf(pen, eraser, sticky, notebook, tea), ids)
    }

    @Test
    fun `件数がちょうど上限のときは、続きの位置が無い`() {
        insertProducts()

        assertNull(query.findPage(ProductSort.ID, after = null, allocatableOnly = false, limit = 5).next)
        assertNotNull(query.findPage(ProductSort.ID, after = null, allocatableOnly = false, limit = 4).next)
    }

    @Test
    fun `在庫のある商品だけにすると、在庫が 0 の商品を除く`() {
        insertProducts()

        assertEquals(
            listOf(pen, eraser, sticky, notebook),
            query.findPage(ProductSort.PRICE, after = null, allocatableOnly = true, limit = 20).items.map { it.id },
        )
    }
}
