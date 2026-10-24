package com.example.shop.order

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.json.JsonTest
import tools.jackson.databind.json.JsonMapper
import java.time.Instant
import java.util.UUID

/**
 * 注文の応答の JSON の形 (項目と並びと値の表し方) を、Boot が組み立てる JsonMapper を通して、DB を使わずに確かめる。
 *
 * JsonMapper を自分で作ると Kotlin のモジュールが入らず、項目の名前と並びが本番と変わるので、注入したものを使う。
 */
@JsonTest
class OrderResponseJsonTest(@Autowired private val jsonMapper: JsonMapper) {

    private val order = Order(
        id = UUID.fromString("019a1c00-0000-7000-8000-000000000001"),
        contactEmail = "taro@example.com",
        status = OrderStatus.CANCELLED,
        orderedAt = Instant.parse("2026-10-24T01:00:00.123456Z"),
        lines = listOf(
            OrderLine(
                productId = UUID.fromString("0199f3a8-0000-7000-8000-000000000005"),
                quantity = 1,
                unitPrice = 1280,
                taxRate = 8,
            ),
        ),
        version = 1,
    )

    @Test
    fun `宣言の順の項目だけを出し、状態は応答の値、日時は UTC の文字列、金額は数にする`() {
        assertEquals(
            """{"id":"019a1c00-0000-7000-8000-000000000001","status":"cancelled",""" +
                """"orderedAt":"2026-10-24T01:00:00.123456Z",""" +
                """"lines":[{"productId":"0199f3a8-0000-7000-8000-000000000005","quantity":1,"unitPrice":1280,""" +
                """"taxRate":8,"subtotal":1280}],"totalAmount":1280,"taxAmounts":[{"taxRate":8,"amount":94}]}""",
            jsonMapper.writeValueAsString(OrderResponse.from(order)),
        )
    }

    @Test
    fun `状態は応答の値から読み戻せる`() {
        assertEquals(OrderStatusResponse.PENDING, jsonMapper.readValue("\"pending\"", OrderStatusResponse::class.java))
    }
}
