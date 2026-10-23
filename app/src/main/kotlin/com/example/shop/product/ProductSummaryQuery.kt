package com.example.shop.product

import org.springframework.jdbc.core.simple.JdbcClient
import org.springframework.stereotype.Repository
import java.util.UUID

/** 一覧に出す商品の 1 行。商品と在庫の 2 つの集約から読んだ値を、1 つにまとめる。 */
data class ProductSummary(
    val id: UUID,
    val name: String,
    val price: Int,
    val taxRate: Int,
    val inventoryQuantity: Int,
)

/**
 * 商品を在庫の数と一緒に読む。集約の境界をまたいで読むだけの問い合わせなので、repository を通さずに SQL で書く。
 */
@Repository
class ProductSummaryQuery(private val jdbcClient: JdbcClient) {

    fun findAll(): List<ProductSummary> =
        jdbcClient.sql(
            """
            select p.id, p.name, p.price, p.tax_rate, i.quantity
            from products p
            join inventories i on i.product_id = p.id
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
