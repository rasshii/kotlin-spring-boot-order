package com.example.shop.order

import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Version
import org.springframework.data.relational.core.mapping.MappedCollection
import org.springframework.data.relational.core.mapping.Table
import java.time.Instant
import java.util.UUID

/**
 * 1 回の購入の依頼。注文明細を含めた 1 つの集約として保存し、読み出す。
 *
 * version が null の注文は、まだ保存していない新しい注文として扱われる。
 */
@Table("orders")
data class Order(
    @Id val id: UUID,
    val contactEmail: String,
    val status: OrderStatus,
    val orderedAt: Instant,
    @MappedCollection(idColumn = "order_id", keyColumn = "line_index")
    val lines: List<OrderLine>,
    @Version val version: Long? = null,
)

enum class OrderStatus { PENDING, CANCELLED }

/** 注文に含まれる 1 行。単価と税率は、注文した時点の商品の値を写したもの。 */
@Table("order_lines")
data class OrderLine(
    val productId: UUID,
    val quantity: Int,
    val unitPrice: Int,
    val taxRate: Int,
)
