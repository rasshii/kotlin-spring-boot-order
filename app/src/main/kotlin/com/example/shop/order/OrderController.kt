package com.example.shop.order

import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.net.URI
import java.util.UUID

/** HTTP のリクエストを受け、サービスの結果をステータスコードに変える。業務の判断はサービスが持つ。 */
@RestController
@RequestMapping("/api/v1/orders")
class OrderController(private val orderService: OrderService) {

    @PostMapping
    fun create(@Valid @RequestBody request: CreateOrderRequest): ResponseEntity<OrderResponse> {
        val command = CreateOrderCommand(
            contactEmail = request.contactEmail,
            lines = request.lines.map { CreateOrderCommand.Line(it.productId, it.quantity) },
        )
        return when (val result = orderService.create(command)) {
            is CreateOrderResult.Created ->
                ResponseEntity.created(URI.create("/api/v1/orders/${result.order.id}")).body(OrderResponse.from(result.order))
            is CreateOrderResult.ProductNotFound -> ResponseEntity.unprocessableContent().build()
            is CreateOrderResult.InsufficientInventory -> ResponseEntity.status(HttpStatus.CONFLICT).build()
        }
    }

    @GetMapping("/{id}")
    fun find(@PathVariable id: UUID): ResponseEntity<OrderResponse> {
        val order = orderService.find(id) ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(OrderResponse.from(order))
    }

    @PostMapping("/{id}/cancel")
    fun cancel(@PathVariable id: UUID): ResponseEntity<OrderResponse> =
        when (val result = orderService.cancel(id)) {
            is CancelOrderResult.Cancelled -> ResponseEntity.ok(OrderResponse.from(result.order))
            CancelOrderResult.NotFound -> ResponseEntity.notFound().build()
            CancelOrderResult.AlreadyCancelled -> ResponseEntity.status(HttpStatus.CONFLICT).build()
        }
}
