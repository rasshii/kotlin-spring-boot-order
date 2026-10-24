package com.example.shop.snippet

import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test

/** 第 13 章の curl の例。記事に載せた順に送る。 */
class Ch13ResponseSnippetsTest : ResponseSnippetTest() {

    @Test
    @Order(1)
    fun `税率の違う 2 つの明細の注文を作る`() = assertSnippetResponse("ch13-create-order")

    @Test
    @Order(2)
    fun `作った注文を取得する`() = assertSnippetResponse("ch13-get-order")
}
