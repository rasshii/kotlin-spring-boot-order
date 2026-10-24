package exercises.ch14

import com.example.shop.ShopApplication
import com.example.shop.TestcontainersConfiguration
import com.example.shop.product.Product
import com.example.shop.product.ProductRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.jdbc.BadSqlGrammarException
import org.springframework.jdbc.core.simple.JdbcClient
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

/**
 * 第 14 章の練習問題 2 の解答例が述べる挙動: ORDER BY の列名を束縛変数で渡すと、エラーにならないまま、
 * 並べ替えが効かない。束縛変数は値の代わりにしかなれないので、データベースは同じ文字列の定数で並べる。
 */
@Import(TestcontainersConfiguration::class)
@SpringBootTest(classes = [ShopApplication::class])
@Transactional
class OrderByBindParameterTest(
    @Autowired private val jdbcClient: JdbcClient,
    @Autowired private val productRepository: ProductRepository,
) {
    private val notebook = UUID.fromString("0199f3a8-0000-7000-8000-000000000001")
    private val pen = UUID.fromString("0199f3a8-0000-7000-8000-000000000002")
    private val sticky = UUID.fromString("0199f3a8-0000-7000-8000-000000000003")

    private fun ids(sql: String, column: String? = null): List<UUID> =
        jdbcClient.sql(sql)
            .apply { if (column != null) param("column", column) }
            .query { rs, _ -> rs.getObject("id", UUID::class.java) }
            .list()

    @Test
    fun `列名を束縛変数で渡すと、価格の順に並ばない`() {
        // 価格の順 (ボールペン・付箋・ノート) と違う順に入れる。
        productRepository.save(Product(id = notebook, name = "ノート", price = 330, taxRate = 10))
        productRepository.save(Product(id = pen, name = "ボールペン", price = 150, taxRate = 10))
        productRepository.save(Product(id = sticky, name = "付箋", price = 220, taxRate = 10))
        val byPrice = listOf(pen, sticky, notebook)

        assertEquals(byPrice, ids("select id from products order by price"))
        assertNotEquals(byPrice, ids("select id from products order by :column", column = "price"))
        // 同じ文字列を定数で書くと、PostgreSQL は並びに使えない定数として断る。
        assertThrows<BadSqlGrammarException> { ids("select id from products order by 'price'") }
    }
}
