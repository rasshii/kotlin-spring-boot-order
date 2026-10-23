package com.example.shop.inventory

import org.springframework.data.repository.ListCrudRepository
import java.util.UUID

interface InventoryRepository : ListCrudRepository<Inventory, UUID>
