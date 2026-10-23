package exercises.ch09

import com.example.shop.product.ProductSummary
import org.springframework.jdbc.core.simple.JdbcClient
import java.util.UUID

/** 第 9 章の練習問題 1 の解答例: 在庫が 1 個以上ある商品だけを読む。 */
class AllocatableProductQuery(private val jdbcClient: JdbcClient) {

    fun findAllocatable(): List<ProductSummary> =
        jdbcClient.sql(
            """
            select p.id, p.name, p.price, p.tax_rate, i.quantity
            from products p
            join inventories i on i.product_id = p.id
            where i.quantity > 0
            order by p.id
            """.trimIndent(),
        )
            .query { rs, _ ->
                ProductSummary(
                    id = rs.getObject("id", UUID::class.java),
                    name = rs.getString("name"),
                    price = rs.getInt("price"),
                    taxRate = rs.getInt("tax_rate"),
                    inventoryQuantity = rs.getInt("quantity"),
                )
            }
            .list()
}
