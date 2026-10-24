package com.example.shop.snippet

import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test

/** 第 14 章の curl の例。記事に載せた順に送る。 */
class Ch14ResponseSnippetsTest : ResponseSnippetTest() {

    @Test
    @Order(1)
    fun `価格の順に 2 件ずつ読む`() = assertSnippetResponse("ch14-list-products-by-price")

    @Test
    @Order(2)
    fun `続きの位置を渡して次の 2 件を読む`() = assertSnippetResponse("ch14-list-products-next-page")

    @Test
    @Order(3)
    fun `在庫のある商品だけを価格の順に読む`() = assertSnippetResponse("ch14-list-products-allocatable")

    @Test
    @Order(4)
    fun `許可リストに無い並びを指す`() = assertSnippetResponse("ch14-list-products-unknown-sort")
}
