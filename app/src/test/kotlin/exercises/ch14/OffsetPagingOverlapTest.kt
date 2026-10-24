package exercises.ch14

import com.example.shop.ShopApplication
import com.example.shop.TestcontainersConfiguration
import com.example.shop.inventory.Inventory
import com.example.shop.inventory.InventoryRepository
import com.example.shop.product.Product
import com.example.shop.product.ProductRepository
import com.example.shop.product.ProductSort
import com.example.shop.product.ProductSummaryQuery
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.jdbc.core.simple.JdbcClient
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

/**
 * 第 14 章の練習問題 1 の解答例が述べる挙動: 1 ページ目を読んだ後で、先頭に並ぶ商品が足されると、
 * offset のページングの 2 ページ目は 1 ページ目と重なる。続きの位置のページングは重ならない。
 */
@Import(TestcontainersConfiguration::class)
@SpringBootTest(classes = [ShopApplication::class])
@Transactional
class OffsetPagingOverlapTest(
    @Autowired private val jdbcClient: JdbcClient,
    @Autowired private val productRepository: ProductRepository,
    @Autowired private val inventoryRepository: InventoryRepository,
    @Autowired private val query: ProductSummaryQuery,
) {
    private val pen = UUID.fromString("0199f3a8-0000-7000-8000-000000000002")
    private val sticky = UUID.fromString("0199f3a8-0000-7000-8000-000000000003")
    private val notebook = UUID.fromString("0199f3a8-0000-7000-8000-000000000001")
    private val marker = UUID.fromString("0199f3a8-0000-7000-8000-000000000004")
    private val clip = UUID.fromString("0199f3a8-0000-7000-8000-000000000008")

    private fun insert(id: UUID, name: String, price: Int) {
        productRepository.save(Product(id = id, name = name, price = price, taxRate = 10))
        inventoryRepository.save(Inventory(productId = id, quantity = 10))
    }

    /** 価格の順の [offset] 件目から [limit] 件を、LIMIT と OFFSET で読む。 */
    private fun offsetPage(offset: Int, limit: Int): List<UUID> =
        jdbcClient.sql("select p.id from products p order by p.price, p.id limit :limit offset :offset")
            .param("limit", limit)
            .param("offset", offset)
            .query { rs, _ -> rs.getObject("id", UUID::class.java) }
            .list()

    @Test
    fun `先頭に並ぶ商品が足されると、offset の 2 ページ目は 1 ページ目の最後の商品と重なる`() {
        insert(pen, "ボールペン", 150)
        insert(sticky, "付箋", 220)
        insert(notebook, "ノート", 330)
        insert(marker, "マーカー", 880)
        val firstByOffset = offsetPage(offset = 0, limit = 2)
        val firstByCursor = query.findPage(ProductSort.PRICE, after = null, allocatableOnly = false, limit = 2)

        // 1 ページ目を読んだ後で、どの商品より安いクリップが足される。
        insert(clip, "クリップ", 100)
        val secondByOffset = offsetPage(offset = 2, limit = 2)
        val secondByCursor = query.findPage(ProductSort.PRICE, after = firstByCursor.next, allocatableOnly = false, limit = 2)

        assertEquals(listOf(pen, sticky), firstByOffset)
        assertEquals(listOf(sticky, notebook), secondByOffset)
        assertEquals(listOf(notebook, marker), secondByCursor.items.map { it.id })
    }
}
