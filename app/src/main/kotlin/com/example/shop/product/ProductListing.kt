package com.example.shop.product

import java.util.Base64
import java.util.UUID

/**
 * 一覧の並び。リクエストの値を、コードに書いた ORDER BY の断片と、続きの位置の条件に写す (許可リスト)。
 *
 * 並びの項目の後ろに主キーを足して、並びを一意にする。一意でないと、同じ値の行がページの境目でどちらに入るかが
 * 決まらず、行が重なったり抜けたりする。続きの位置の条件は行値の比較で書く (PostgreSQL は索引の範囲で読める)。
 */
enum class ProductSort(val parameter: String, val orderBy: String, val after: String) {
    ID("id", "p.id", "p.id > :afterId"),
    PRICE("price", "p.price, p.id", "(p.price, p.id) > (:afterPrice, :afterId)"),
    ;

    companion object {
        fun fromParameter(value: String): ProductSort? = entries.firstOrNull { it.parameter == value }
    }
}

/**
 * 一覧の続きの位置。直前のページの最後の商品の、価格と ID を持つ (ID の並びでは価格を使わない)。
 *
 * クライアントには、中身を約束しない文字列 ([encode]) で渡す。並びの名前も入れるので、別の並びの一覧に渡された
 * 位置は [decode] で断れる。
 */
data class ProductCursor(val sort: ProductSort, val price: Int, val id: UUID) {

    fun encode(): String =
        Base64.getUrlEncoder().withoutPadding().encodeToString("${sort.parameter}:$price:$id".toByteArray())

    companion object {
        /** [token] を [sort] の一覧の位置として読む。読めないか、別の並びの位置なら null。 */
        fun decode(sort: ProductSort, token: String): ProductCursor? {
            val parts = try {
                String(Base64.getUrlDecoder().decode(token)).split(":")
            } catch (e: IllegalArgumentException) {
                return null
            }
            if (parts.size != 3 || parts[0] != sort.parameter) return null
            val price = parts[1].toIntOrNull() ?: return null
            val id = try {
                UUID.fromString(parts[2])
            } catch (e: IllegalArgumentException) {
                return null
            }
            return ProductCursor(sort, price, id)
        }
    }
}

/** 一覧の 1 ページ。[next] は続きのページの位置で、最後のページなら null。 */
data class ProductPage(val items: List<ProductSummary>, val next: ProductCursor?)
