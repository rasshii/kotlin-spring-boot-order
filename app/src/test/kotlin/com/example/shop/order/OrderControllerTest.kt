package com.example.shop.order

import com.example.shop.TestcontainersConfiguration
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.client.RestTestClient
import org.springframework.test.web.servlet.client.expectBody
import java.net.URI
import java.util.UUID

/**
 * 注文のエンドポイントを、起動したサーバーに HTTP で送って確かめる。URL の対応付け、リクエストの本文の読み取り、
 * ステータスコードとヘッダを、コントローラから永続化まで通して見る。
 *
 * 初期データの商品を使うので dev の profile で動かす。
 */
@Import(TestcontainersConfiguration::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureRestTestClient
@ActiveProfiles("dev")
class OrderControllerTest(@Autowired private val client: RestTestClient) {

    private val notebook = UUID.fromString("0199f3a8-0000-7000-8000-000000000001")

    @Test
    fun `作った注文は 201 で返り、Location の URL で同じ注文を取得できる`() {
        val created = client.post().uri("/api/v1/orders")
            .contentType(MediaType.APPLICATION_JSON)
            .body(CreateOrderRequest(contactEmail = "taro@example.com", lines = listOf(CreateOrderLineRequest(notebook, 2))))
            .exchange()
            .expectStatus().isCreated()
            .expectBody<OrderResponse>()
            .returnResult()
        val order = requireNotNull(created.responseBody)

        assertEquals(URI.create("/api/v1/orders/${order.id}"), created.responseHeaders.location)
        client.get().uri("/api/v1/orders/${order.id}")
            .exchange()
            .expectStatus().isOk()
            .expectBody<OrderResponse>().isEqualTo(order)
    }

    @Test
    fun `無い注文の取得は 404`() {
        client.get().uri("/api/v1/orders/019a1c00-0000-7000-8000-000000000099")
            .exchange()
            .expectStatus().isNotFound()
    }
}
