package com.example.shop.order

import com.fasterxml.jackson.annotation.JsonValue
import java.time.Instant
import java.util.UUID

/**
 * 注文の応答の本文。集約 (Order) から、クライアントに約束する項目だけを写す。
 *
 * 出す項目をすべて主コンストラクタの val にする。Jackson 3 はコンストラクタのプロパティを宣言の順に書くので、
 * JSON の項目の並びがこの宣言の順に決まる。getter だけのプロパティや、get・is で始まる関数は、コンストラクタの
 * 外でも応答の項目になる (アルファベット順で後ろに並ぶ) ので、このクラスには足さない。計算は集約に置く。
 *
 * version (楽観ロックの列) と contactEmail (連絡先メールアドレス) は出さない。version はサーバーの内部の値で、
 * contactEmail は、誰の注文かを確かめる仕組み (認証) がまだ無いので、注文の ID を知る人なら誰でも読めてしまう。
 */
data class OrderResponse(
    val id: UUID,
    val status: OrderStatusResponse,
    val orderedAt: Instant,
    val lines: List<OrderLineResponse>,
    val totalAmount: Int,
    val taxAmounts: List<TaxAmountResponse>,
) {
    companion object {
        fun from(order: Order): OrderResponse = OrderResponse(
            id = order.id,
            status = OrderStatusResponse.from(order.status),
            orderedAt = order.orderedAt,
            lines = order.lines.map(OrderLineResponse::from),
            totalAmount = order.totalAmount,
            taxAmounts = order.taxAmounts.map(TaxAmountResponse::from),
        )
    }
}

/** 注文明細の応答。金額は消費税を含む円の整数、税率はパーセント。 */
data class OrderLineResponse(
    val productId: UUID,
    val quantity: Int,
    val unitPrice: Int,
    val taxRate: Int,
    val subtotal: Int,
) {
    companion object {
        fun from(line: OrderLine): OrderLineResponse = OrderLineResponse(
            productId = line.productId,
            quantity = line.quantity,
            unitPrice = line.unitPrice,
            taxRate = line.taxRate,
            subtotal = line.subtotal,
        )
    }
}

/** 1 つの税率の消費税額の応答。 */
data class TaxAmountResponse(
    val taxRate: Int,
    val amount: Int,
) {
    companion object {
        fun from(taxAmount: TaxAmount): TaxAmountResponse = TaxAmountResponse(taxAmount.taxRate, taxAmount.amount)
    }
}

/**
 * 注文の状態の、応答での値。集約の OrderStatus (データベースには name() で書く) とは別に、JSON の値を決める。
 *
 * Jackson 3 は enum を toString() で書くので、@JsonValue で値を明示する。集約の側の状態を足すと、[from] の when が
 * コンパイルエラーになり、応答での値を決め忘れない。
 */
enum class OrderStatusResponse(@get:JsonValue val value: String) {
    PENDING("pending"),
    CANCELLED("cancelled"),
    ;

    companion object {
        fun from(status: OrderStatus): OrderStatusResponse = when (status) {
            OrderStatus.PENDING -> PENDING
            OrderStatus.CANCELLED -> CANCELLED
        }
    }
}
