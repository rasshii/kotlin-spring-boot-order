package com.example.shop.order

import jakarta.validation.Validation
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.util.UUID

/** 注文を作るリクエストの注釈が、どの項目にどのメッセージの違反を出すか。Spring を起動せずに確かめる。 */
class CreateOrderRequestValidationTest {

    private val validator = Validation.buildDefaultValidatorFactory().validator

    private val notebook = UUID.fromString("0199f3a8-0000-7000-8000-000000000001")
    private val coffee = UUID.fromString("0199f3a8-0000-7000-8000-000000000005")

    /** 違反を「項目: メッセージ」の形にして並べる。validate が返す集合は順を持たないので、並べてから比べる。 */
    private fun violations(request: CreateOrderRequest): List<String> =
        validator.validate(request).map { "${it.propertyPath}: ${it.message}" }.sorted()

    @Test
    fun `正しいリクエストには違反が無い`() {
        val request = CreateOrderRequest(
            contactEmail = "taro@example.com",
            lines = listOf(CreateOrderLineRequest(notebook, 2), CreateOrderLineRequest(coffee, 1)),
        )

        assertEquals(emptyList<String>(), violations(request))
    }

    @Test
    fun `空のメールアドレスと数量 0 は、それぞれの項目の違反になる`() {
        val request = CreateOrderRequest(
            contactEmail = "",
            lines = listOf(CreateOrderLineRequest(notebook, 0)),
        )

        assertEquals(
            listOf(
                "contactEmail: 連絡先メールアドレスを入れてください",
                "lines[0].quantity: 数量は 1 以上にしてください",
            ),
            violations(request),
        )
    }

    @Test
    fun `同じ商品の明細が重なると、明細のリストの違反になる`() {
        val request = CreateOrderRequest(
            contactEmail = "taro@example.com",
            lines = listOf(CreateOrderLineRequest(notebook, 2), CreateOrderLineRequest(notebook, 1)),
        )

        assertEquals(listOf("lines: 同じ商品の注文明細が重なっています"), violations(request))
    }
}
