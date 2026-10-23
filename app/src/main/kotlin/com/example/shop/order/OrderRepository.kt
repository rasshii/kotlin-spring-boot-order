package com.example.shop.order

import org.springframework.data.repository.ListCrudRepository
import java.util.UUID

interface OrderRepository : ListCrudRepository<Order, UUID>
