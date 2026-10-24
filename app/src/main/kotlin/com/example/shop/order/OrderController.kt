package com.example.shop.order

import com.example.shop.product.ProductRepository
import com.example.shop.shared.IdGenerator
import jakarta.validation.Valid
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.net.URI
import java.time.Clock
import java.util.UUID

@RestController
@RequestMapping("/api/v1/orders")
class OrderController(
    private val orderRepository: OrderRepository,
    private val productRepository: ProductRepository,
    private val idGenerator: IdGenerator,
    private val clock: Clock,
) {

    @PostMapping
    fun create(@Valid @RequestBody request: CreateOrderRequest): ResponseEntity<Order> {
        val products = productRepository.findAllById(request.lines.map { it.productId }).associateBy { it.id }
        val lines = request.lines.map { line ->
            val product = products[line.productId] ?: return ResponseEntity.unprocessableContent().build()
            OrderLine(
                productId = product.id,
                quantity = line.quantity,
                unitPrice = product.price,
                taxRate = product.taxRate,
            )
        }
        val order = orderRepository.save(
            Order(
                id = idGenerator.newId(),
                contactEmail = request.contactEmail,
                status = OrderStatus.PENDING,
                orderedAt = clock.instant(),
                lines = lines,
            ),
        )
        return ResponseEntity.created(URI.create("/api/v1/orders/${order.id}")).body(order)
    }

    @GetMapping("/{id}")
    fun find(@PathVariable id: UUID): ResponseEntity<Order> {
        val order = orderRepository.findByIdOrNull(id) ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(order)
    }
}
