package com.example.shop.order

import com.example.shop.web.ProblemType
import com.example.shop.web.RandomTrackingIdGenerator
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.slot
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.assertj.MockMvcTester
import java.time.Instant
import java.util.UUID

/**
 * コントローラだけを Spring MVC に載せ (Web 層のスライス)、サービスを MockK の mock に差し替えて確かめる。
 * URL の対応付け、リクエストの読み取りと検証、サービスの結果から応答への変換を見る。データベースは使わない。
 *
 * スライスは @RestControllerAdvice の ApiExceptionHandler も読み込むが、advice が使う追跡 ID の bean (@Component)
 * は読み込まないので、@Import で足す。
 */
@WebMvcTest(OrderController::class)
@Import(RandomTrackingIdGenerator::class)
class OrderControllerWebMvcTest(@Autowired private val mvc: MockMvcTester) {

    @MockkBean
    private lateinit var orderService: OrderService

    private val notebook = UUID.fromString("0199f3a8-0000-7000-8000-000000000001")
    private val order = Order(
        id = UUID.fromString("019a1c00-0000-7000-8000-000000000001"),
        contactEmail = "taro@example.com",
        status = OrderStatus.PENDING,
        orderedAt = Instant.parse("2026-10-24T01:00:00Z"),
        lines = listOf(OrderLine(productId = notebook, quantity = 2, unitPrice = 330, taxRate = 10)),
    )

    private fun postOrder(body: String) =
        mvc.post().uri("/api/v1/orders").contentType(MediaType.APPLICATION_JSON).content(body)

    @Test
    fun `作成のリクエストをコマンドに写してサービスに渡し、作った注文を 201 で返す`() {
        val command = slot<CreateOrderCommand>()
        every { orderService.create(capture(command)) } returns CreateOrderResult.Created(order)

        val result = postOrder(
            """{"contactEmail": "taro@example.com", "lines": [{"productId": "$notebook", "quantity": 2}]}""",
        ).exchange()

        assertThat(result).hasStatus(HttpStatus.CREATED).hasHeader("Location", "/api/v1/orders/${order.id}")
        assertThat(result).bodyJson().extractingPath("$.totalAmount").isEqualTo(660)
        assertEquals(
            CreateOrderCommand(contactEmail = "taro@example.com", lines = listOf(CreateOrderCommand.Line(notebook, 2))),
            command.captured,
        )
    }

    @Test
    fun `検証に通らないリクエストは、入力の誤りの 400 にし、サービスを呼ばない`() {
        // 検証を通り抜けてサービスが呼ばれたら、201 が返ってステータスコードの行で落ちるよう、答えを決めておく。
        every { orderService.create(any()) } returns CreateOrderResult.Created(order)

        assertThat(postOrder("""{"contactEmail": "", "lines": []}"""))
            .hasStatus(HttpStatus.BAD_REQUEST)
            .bodyJson().extractingPath("$.errors[*].pointer").isEqualTo(listOf("#/contactEmail", "#/lines"))
        verify(exactly = 0) { orderService.create(any()) }
    }

    @Test
    fun `在庫が足りなければ、在庫不足の type の 409 にする`() {
        every { orderService.create(any()) } returns
            CreateOrderResult.InsufficientInventory(listOf(Shortage(notebook, requested = 2, available = 1)))

        assertThat(postOrder("""{"contactEmail": "taro@example.com", "lines": [{"productId": "$notebook", "quantity": 2}]}"""))
            .hasStatus(HttpStatus.CONFLICT)
            .bodyJson().extractingPath("$.type").isEqualTo(ProblemType.INSUFFICIENT_INVENTORY.uri.toString())
    }
}
