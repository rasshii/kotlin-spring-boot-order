package com.example.shop.snippet

import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test

/** 第 12 章の curl の例。記事に載せた順に送る。 */
class Ch12ResponseSnippetsTest : ResponseSnippetTest() {

    @Test
    @Order(1)
    fun `注文を作る`() = assertSnippetResponse("ch12-create-order")

    @Test
    @Order(2)
    fun `在庫が足りない注文を作る`() = assertSnippetResponse("ch12-create-order-insufficient-inventory")

    @Test
    @Order(3)
    fun `注文を取り消す`() = assertSnippetResponse("ch12-cancel-order")

    @Test
    @Order(4)
    fun `取消の注文をもう一度取り消す`() = assertSnippetResponse("ch12-cancel-order-again")

    @Test
    @Order(5)
    fun `無い注文を取り消す`() = assertSnippetResponse("ch12-cancel-order-not-found")
}
