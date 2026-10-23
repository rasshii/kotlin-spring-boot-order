package com.example.shop.product

import com.example.shop.TestcontainersConfiguration
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.dao.OptimisticLockingFailureException
import org.springframework.data.annotation.Id
import org.springframework.data.jdbc.core.JdbcAggregateTemplate
import org.springframework.data.relational.core.mapping.Table
import org.springframework.jdbc.core.simple.JdbcClient
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

/** Product と同じテーブルに対応付けた、version のプロパティを持たないクラス。 */
@Table("products")
data class ProductWithoutVersion(
    @Id val id: UUID,
    val name: String,
    val price: Int,
    val taxRate: Int,
)

/**
 * アプリが ID を入れてから save すると、Spring Data JDBC は INSERT と UPDATE のどちらを出すか。
 * repository の save も、JdbcAggregateTemplate の save と同じ判定で動く。
 */
@Import(TestcontainersConfiguration::class)
@SpringBootTest
@Transactional
class SaveWithAssignedIdTest(
    @Autowired private val template: JdbcAggregateTemplate,
    @Autowired private val jdbcClient: JdbcClient,
) {
    private val id = UUID.fromString("0199f3a8-0000-7000-8000-000000000001")

    private fun countRows(): Long =
        jdbcClient.sql("select count(*) from products where id = :id")
            .param("id", id)
            .query(Long::class.java)
            .single()

    @Test
    fun `version の無いクラスは、ID があると既存の行とみなされ、行が入らない`() {
        template.save(ProductWithoutVersion(id = id, name = "ノート", price = 330, taxRate = 10))

        assertEquals(0L, countRows())
    }

    @Test
    fun `version が null なら新しい行とみなされ、行が入る`() {
        val saved = template.save(Product(id = id, name = "ノート", price = 330, taxRate = 10))

        assertEquals(1L, countRows())
        assertEquals(0L, saved.version)
    }

    @Test
    fun `読んだ後に書き換えられた行を、古い version のまま保存すると失敗する`() {
        val saved = template.save(Product(id = id, name = "ノート", price = 330, taxRate = 10))
        template.save(saved.copy(price = 350))

        assertThrows<OptimisticLockingFailureException> {
            template.save(saved.copy(price = 360))
        }
    }
}
