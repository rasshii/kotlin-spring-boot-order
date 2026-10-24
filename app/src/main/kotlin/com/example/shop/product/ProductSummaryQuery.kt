package com.example.shop.product

import org.springframework.jdbc.core.simple.JdbcClient
import org.springframework.stereotype.Repository
import java.sql.ResultSet
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
            .query(::toSummary)
            .list()

    /**
     * [sort] の順に、[after] の次から [limit] 件を読む。[allocatableOnly] なら、在庫が 1 個以上の (引き当てられる) 商品だけにする。
     *
     * 1 件多く読んで、続きのページがあるかを決める。総件数 (COUNT) は読まない。ORDER BY と続きの位置の条件は
     * [ProductSort] がコードに持つ断片だけを使い、リクエストの値を SQL に入れない。
     */
    fun findPage(sort: ProductSort, after: ProductCursor?, allocatableOnly: Boolean, limit: Int): ProductPage {
        val conditions = listOfNotNull(
            "i.quantity > 0".takeIf { allocatableOnly },
            sort.after.takeIf { after != null },
        )
        val where = if (conditions.isEmpty()) "" else conditions.joinToString(" and ", prefix = "where ")
        val rows = jdbcClient.sql(
            """
            select p.id, p.name, p.price, p.tax_rate, i.quantity
            from products p
            join inventories i on i.product_id = p.id
            $where
            order by ${sort.orderBy}
            limit :limit
            """.trimIndent(),
        )
            .param("afterId", after?.id)
            .param("afterPrice", after?.price)
            .param("limit", limit + 1)
            .query(::toSummary)
            .list()
        val items = rows.take(limit)
        val next = if (rows.size > limit) items.last().let { ProductCursor(sort, it.price, it.id) } else null
        return ProductPage(items, next)
    }

    private fun toSummary(rs: ResultSet, rowNum: Int): ProductSummary =
        ProductSummary(
            id = rs.getObject("id", UUID::class.java),
            name = rs.getString("name"),
            price = rs.getInt("price"),
            taxRate = rs.getInt("tax_rate"),
            inventoryQuantity = rs.getInt("quantity"),
        )
}
