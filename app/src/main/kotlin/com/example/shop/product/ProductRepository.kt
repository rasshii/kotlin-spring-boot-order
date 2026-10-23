package com.example.shop.product

import org.springframework.data.repository.ListCrudRepository
import java.util.UUID

interface ProductRepository : ListCrudRepository<Product, UUID>
