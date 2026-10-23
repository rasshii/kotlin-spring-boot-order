package com.example.shop.product

import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * 一覧 API が 1 回に返す件数の設定。application.yaml の `shop.listing` を読む。
 *
 * 値が矛盾していれば、アプリは起動の時点で止まる。一覧を返すときまで誤りが分からない形にしない。
 */
@ConfigurationProperties("shop.listing")
data class ListingProperties(
    val defaultLimit: Int,
    val maxLimit: Int,
) {
    init {
        require(maxLimit >= 1) { "shop.listing.max-limit は 1 以上にしてください: $maxLimit" }
        require(defaultLimit in 1..maxLimit) {
            "shop.listing.default-limit は 1 から max-limit ($maxLimit) にしてください: $defaultLimit"
        }
    }
}
