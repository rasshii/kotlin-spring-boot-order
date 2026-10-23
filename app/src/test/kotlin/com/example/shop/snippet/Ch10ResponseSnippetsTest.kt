package com.example.shop.snippet

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.simple.JdbcClient

/** 第 10 章の curl の例。記事に載せた順に送る。 */
class Ch10ResponseSnippetsTest : ResponseSnippetTest() {

    @Autowired
    private lateinit var jdbcClient: JdbcClient

    private fun countOrders(): Long = jdbcClient.sql("select count(*) from orders").query(Long::class.java).single()

    @Test
    @Order(1)
    fun `注文を作る`() = assertSnippetResponse("ch10-create-order")

    @Test
    @Order(2)
    fun `作った注文を取得する`() = assertSnippetResponse("ch10-get-order")

    @Test
    @Order(3)
    fun `無い注文を取得する`() = assertSnippetResponse("ch10-get-order-not-found")

    @Test
    @Order(4)
    fun `無い商品を指して注文を作る`() = assertSnippetResponse("ch10-create-order-unknown-product")

    // 練習問題の解答例が述べる挙動。応答は既定のエラー応答で日時を含むので、ステータスコードだけを確かめる。

    @Test
    @Order(5)
    fun `ID の形でない文字列で注文を取得する`() = assertSnippetResponse("ch10-exercise-get-order-invalid-id", status = 400)

    @Test
    @Order(6)
    fun `数量が 0 の注文を作っても、注文の行は残らない`() {
        val before = countOrders()

        assertSnippetResponse("ch10-exercise-create-order-zero-quantity", status = 500)

        assertEquals(before, countOrders())
    }
}
