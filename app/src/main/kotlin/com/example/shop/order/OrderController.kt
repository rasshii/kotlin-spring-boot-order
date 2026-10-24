package com.example.shop.order

import com.example.shop.web.ProblemType
import com.example.shop.web.toResponseEntity
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.net.URI
import java.util.UUID

/**
 * HTTP のリクエストを受け、サービスの結果を応答に変える。業務の判断はサービスが持つ。
 *
 * 失敗は Problem Details の本文で返すので、応答の本文の型は成功と失敗で違う (ResponseEntity<*>)。
 */
@RestController
@RequestMapping("/api/v1/orders")
class OrderController(private val orderService: OrderService) {

    @PostMapping
    fun create(@Valid @RequestBody request: CreateOrderRequest): ResponseEntity<*> {
        val command = CreateOrderCommand(
            contactEmail = request.contactEmail,
            lines = request.lines.map { CreateOrderCommand.Line(it.productId, it.quantity) },
        )
        return when (val result = orderService.create(command)) {
            is CreateOrderResult.Created ->
                ResponseEntity.created(URI.create("/api/v1/orders/${result.order.id}")).body(OrderResponse.from(result.order))
            is CreateOrderResult.ProductNotFound ->
                ProblemType.PRODUCT_NOT_FOUND.problem("注文した商品のうち ${result.productIds.size} 件が見つかりません")
                    .also { it.setProperty("productIds", result.productIds) }
                    .toResponseEntity()
            is CreateOrderResult.InsufficientInventory ->
                ProblemType.INSUFFICIENT_INVENTORY.problem("在庫の足りない商品が ${result.shortages.size} 件あります")
                    .also { it.setProperty("shortages", result.shortages) }
                    .toResponseEntity()
        }
    }

    @GetMapping("/{id}")
    fun find(@PathVariable id: UUID): ResponseEntity<*> {
        val order = orderService.find(id) ?: return orderNotFound()
        return ResponseEntity.ok(OrderResponse.from(order))
    }

    @PostMapping("/{id}/cancel")
    fun cancel(@PathVariable id: UUID): ResponseEntity<*> =
        when (val result = orderService.cancel(id)) {
            is CancelOrderResult.Cancelled -> ResponseEntity.ok(OrderResponse.from(result.order))
            CancelOrderResult.NotFound -> orderNotFound()
            CancelOrderResult.AlreadyCancelled ->
                ProblemType.ORDER_CANCELLED.problem("この注文はすでに取消の状態です").toResponseEntity()
        }

    /** 無い注文は、ステータスコードだけで意味が決まるので、独自の type を付けない (about:blank)。 */
    private fun orderNotFound(): ResponseEntity<ProblemDetail> =
        ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, "注文が見つかりません").toResponseEntity()
}
