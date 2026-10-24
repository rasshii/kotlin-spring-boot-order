package com.example.shop.snippet

import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test

/** 第 15 章の curl の例。記事に載せた順に送る。 */
class Ch15ResponseSnippetsTest : ResponseSnippetTest() {

    @Test
    @Order(1)
    fun `在庫が足りない注文を作る`() = assertSnippetResponse("ch15-create-order-insufficient-inventory")

    @Test
    @Order(2)
    fun `空のメールアドレスと数量 0 の注文を作る`() = assertSnippetResponse("ch15-create-order-invalid-fields")

    @Test
    @Order(3)
    fun `同じ商品の明細が重なる注文を作る`() = assertSnippetResponse("ch15-create-order-duplicate-product")

    @Test
    @Order(4)
    fun `商品の ID が無い明細の注文を作る`() = assertSnippetResponse("ch15-create-order-missing-product-id")

    @Test
    @Order(5)
    fun `数でない件数で一覧を読む`() = assertSnippetResponse("ch15-list-products-invalid-limit")

    @Test
    @Order(6)
    fun `無い注文を取得する`() = assertSnippetResponse("ch15-get-order-not-found")
}
