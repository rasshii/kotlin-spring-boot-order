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
) {
    /** 注文の合計。価格は消費税を含むので、合計も消費税を含む。 */
    val totalAmount: Int
        get() = lines.sumOf { it.subtotal }

    /**
     * 税率ごとの消費税額。税率ごとの合計から割り戻し、1 円未満を切り捨てる (合計 × 税率 ÷ (100 + 税率))。
     * 明細ごとに割り戻して足すと、切り捨てが明細の数だけ重なり、合計の消費税額が変わる。
     */
    val taxAmounts: List<TaxAmount>
        get() = lines.groupBy { it.taxRate }
            .map { (taxRate, lines) -> TaxAmount(taxRate, lines.sumOf { it.subtotal } * taxRate / (100 + taxRate)) }
            .sortedByDescending { it.taxRate }

    fun canCancel(): Boolean = status == OrderStatus.PENDING

    /**
     * 取り消した注文を返す。
     *
     * 取り消せるかは、呼ぶ側が [canCancel] で先に確かめる。取り消せない注文に呼ぶのはプログラムの誤りなので、check で止める。
     */
    fun cancel(): Order {
        check(canCancel()) { "受付の注文だけを取り消せます: $id は $status" }
        return copy(status = OrderStatus.CANCELLED)
    }
}

enum class OrderStatus { PENDING, CANCELLED }

/** 注文に含まれる 1 行。単価と税率は、注文した時点の商品の値を写したもの。 */
@Table("order_lines")
data class OrderLine(
    val productId: UUID,
    val quantity: Int,
    val unitPrice: Int,
    val taxRate: Int,
) {
    val subtotal: Int
        get() = unitPrice * quantity
}

/** 1 つの税率の消費税額。 */
data class TaxAmount(val taxRate: Int, val amount: Int)
