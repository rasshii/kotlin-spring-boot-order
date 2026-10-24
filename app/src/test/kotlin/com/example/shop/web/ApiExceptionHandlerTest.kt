package com.example.shop.web

import com.example.shop.TestcontainersConfiguration
import com.example.shop.inventory.Inventory
import com.example.shop.inventory.InventoryRepository
import com.example.webtest.UnconvertibleParameterController
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.test.system.CapturedOutput
import org.springframework.boot.test.system.OutputCaptureExtension
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.Primary
import org.springframework.dao.OptimisticLockingFailureException
import org.springframework.dao.PessimisticLockingFailureException
import org.springframework.http.MediaType
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.json.JsonCompareMode
import org.springframework.test.web.servlet.client.RestTestClient
import java.sql.SQLException
import java.util.UUID

/** 本物の在庫の repository に委譲し、[failure] があれば save でそれを投げる。注文を作る途中の失敗の場面を作る。 */
class FailingSaveInventoryRepository(
    private val delegate: InventoryRepository,
) : InventoryRepository by delegate {
    @Volatile
    var failure: RuntimeException? = null

    override fun <S : Inventory> save(entity: S): S {
        failure?.let { throw it }
        return delegate.save(entity)
    }
}

/**
 * advice が例外を Problem Details に変えることを、起動したサーバーに HTTP で送って確かめる。業務の失敗の応答は、
 * 記事の curl の例 (snippets) が確かめる。
 *
 * 初期データの商品を使うので dev の profile で動かす。
 */
@Import(
    TestcontainersConfiguration::class,
    ApiExceptionHandlerTest.FailureConfiguration::class,
    UnconvertibleParameterController::class,
)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureRestTestClient
@ActiveProfiles("dev")
@ExtendWith(OutputCaptureExtension::class)
class ApiExceptionHandlerTest(
    @Autowired private val client: RestTestClient,
    @Autowired private val inventories: FailingSaveInventoryRepository,
    @Autowired private val jdbcTemplate: JdbcTemplate,
) {

    @TestConfiguration(proxyBeanMethods = false)
    class FailureConfiguration {
        /** @Primary なので、OrderService にはこちらが渡る。委譲先は、名前で指した本物の repository。 */
        @Bean
        @Primary
        fun failingSaveInventoryRepository(
            @Qualifier("inventoryRepository") delegate: InventoryRepository,
        ) = FailingSaveInventoryRepository(delegate)

        @Bean
        @Primary
        fun fixedTrackingIdGenerator() = TrackingIdGenerator { TRACKING_ID }
    }

    @AfterEach
    fun clearFailure() {
        inventories.failure = null
    }

    private fun createOrder() =
        client.post().uri("/api/v1/orders")
            .contentType(MediaType.APPLICATION_JSON)
            .body(
                """{"contactEmail": "taro@example.com", "lines": [{"productId": "0199f3a8-0000-7000-8000-000000000001", "quantity": 1}]}""",
            )
            .exchange()

    @Test
    fun `楽観ロックの失敗は 409 にし、例外のメッセージを本文に入れない`() {
        inventories.failure = OptimisticLockingFailureException("在庫の版が合わない (テストで投げる例外)")

        createOrder()
            .expectStatus().isEqualTo(409)
            .expectHeader().contentType(MediaType.APPLICATION_PROBLEM_JSON)
            .expectBody().json(CONCURRENT_UPDATE_BODY, JsonCompareMode.STRICT)
    }

    @Test
    fun `デッドロックで止められた更新も 409 にする`() {
        // PostgreSQL は、デッドロックになったトランザクションの片方を SQLState 40P01 で止める。アプリの JdbcTemplate と
        // 同じ変換で、Spring の例外にする。
        val deadlock = jdbcTemplate.exceptionTranslator.translate("在庫の更新", null, SQLException("deadlock detected", "40P01"))
        assertInstanceOf(PessimisticLockingFailureException::class.java, deadlock)
        inventories.failure = deadlock

        createOrder()
            .expectStatus().isEqualTo(409)
            .expectHeader().contentType(MediaType.APPLICATION_PROBLEM_JSON)
            .expectBody().json(CONCURRENT_UPDATE_BODY, JsonCompareMode.STRICT)
    }

    @Test
    fun `捕まえなかった例外は 500 にし、追跡 ID を instance とログに載せ、例外の中身は本文に入れない`(output: CapturedOutput) {
        inventories.failure = IllegalStateException("在庫の行を読めない (テストで投げる例外)")

        createOrder()
            .expectStatus().isEqualTo(500)
            .expectHeader().contentType(MediaType.APPLICATION_PROBLEM_JSON)
            .expectBody().json(INTERNAL_ERROR_BODY, JsonCompareMode.STRICT)
        assertTrue(output.out.contains("想定していない例外 (追跡 ID: $TRACKING_ID)"))
        assertTrue(output.out.contains("java.lang.IllegalStateException: 在庫の行を読めない (テストで投げる例外)"))
    }

    @Test
    fun `Spring MVC が 500 で返す例外も、同じ本文と追跡 ID にする`(output: CapturedOutput) {
        client.get().uri("/test/unconvertible?value=x")
            .exchange()
            .expectStatus().isEqualTo(500)
            .expectBody().json(INTERNAL_ERROR_BODY, JsonCompareMode.STRICT)
        assertTrue(output.out.contains("想定していない例外 (追跡 ID: $TRACKING_ID)"))
        assertTrue(output.out.contains("ConversionNotSupportedException"))
    }

    companion object {
        val TRACKING_ID: UUID = UUID.fromString("00000000-0000-4000-8000-000000000001")

        val CONCURRENT_UPDATE_BODY =
            """
            {
              "type": "${ProblemType.CONCURRENT_UPDATE.uri}",
              "title": "ほかの更新と重なりました",
              "status": 409,
              "detail": "ほかの処理と同じデータの更新が重なりました。もう一度送ってください。",
              "instance": "/api/v1/orders"
            }
            """.trimIndent()

        val INTERNAL_ERROR_BODY =
            """
            {
              "title": "Internal Server Error",
              "status": 500,
              "detail": "サーバーで想定していないエラーが起きました。問い合わせのときは instance の値を伝えてください。",
              "instance": "urn:uuid:$TRACKING_ID"
            }
            """.trimIndent()
    }
}
