package com.example.shop.product

import com.example.shop.TestcontainersConfiguration
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.client.RestTestClient
import java.util.UUID

/** 商品の一覧のリクエストの値の扱い。初期データの商品を使うので dev の profile で動かす。 */
@Import(TestcontainersConfiguration::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureRestTestClient
@ActiveProfiles("dev")
class ProductControllerTest(@Autowired private val client: RestTestClient) {

    @ParameterizedTest(name = "{0}")
    @ValueSource(strings = ["sort=name", "sort=price;drop", "limit=0", "limit=101", "limit=abc", "cursor=not-a-cursor"])
    fun `許可リストに無い並び、範囲の外の件数、読めない続きの位置は 400`(query: String) {
        client.get().uri("/api/v1/products?$query")
            .exchange()
            .expectStatus().isBadRequest()
    }

    @Test
    fun `別の並びの続きの位置は 400`() {
        val priceCursor = ProductCursor(ProductSort.PRICE, 150, UUID.fromString("0199f3a8-0000-7000-8000-000000000002"))

        client.get().uri("/api/v1/products?sort=id&cursor=${priceCursor.encode()}")
            .exchange()
            .expectStatus().isBadRequest()
    }

    @Test
    fun `件数は上限ちょうどまで受ける`() {
        client.get().uri("/api/v1/products?limit=100")
            .exchange()
            .expectStatus().isOk()
    }
}
