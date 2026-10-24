package com.example.shop.order

import jakarta.validation.Valid
import jakarta.validation.constraints.AssertTrue
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.Size
import java.util.UUID

/**
 * 注文を作るリクエストの本文。形の検証 (データベースを見なくても判断できるもの) だけをここで行う。
 * 商品があるか、在庫が足りるかは、データベースを読んで判断する。
 *
 * メッセージは注釈に明示する。既定のメッセージは JVM のロケールで言語が変わる。
 */
data class CreateOrderRequest(
    @NotBlank(message = "連絡先メールアドレスを入れてください")
    @Email(message = "連絡先メールアドレスの形が正しくありません")
    @Size(max = 254, message = "連絡先メールアドレスは 254 文字以内にしてください")
    val contactEmail: String,
    @NotEmpty(message = "注文明細を 1 行以上入れてください")
    @Size(max = 20, message = "注文明細は 20 行以内にしてください")
    val lines: List<@Valid CreateOrderLineRequest>,
) {
    /** 同じ商品の明細が 2 行あると、どちらの数量で注文するのかが決まらない。 */
    @get:AssertTrue(message = "同じ商品の注文明細が重なっています")
    val hasDistinctProducts: Boolean
        get() = lines.distinctBy { it.productId }.size == lines.size
}

data class CreateOrderLineRequest(
    val productId: UUID,
    @Min(value = 1, message = "数量は 1 以上にしてください")
    @Max(value = 99, message = "数量は 99 以下にしてください")
    val quantity: Int,
)
