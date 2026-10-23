package com.example.shop.product

import com.example.shop.TestcontainersConfiguration
import com.example.shop.inventory.Inventory
import com.example.shop.inventory.InventoryRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Import(TestcontainersConfiguration::class)
@SpringBootTest
@Transactional
class ProductSummaryQueryTest(
    @Autowired private val query: ProductSummaryQuery,
    @Autowired private val productRepository: ProductRepository,
    @Autowired private val inventoryRepository: InventoryRepository,
) {

    @Test
    fun `商品を在庫の数と一緒に、ID の順に読む`() {
        val pen = UUID.fromString("0199f3a8-0000-7000-8000-000000000002")
        val notebook = UUID.fromString("0199f3a8-0000-7000-8000-000000000001")
        productRepository.save(Product(id = pen, name = "ボールペン", price = 150, taxRate = 10))
        productRepository.save(Product(id = notebook, name = "ノート", price = 330, taxRate = 10))
        inventoryRepository.save(Inventory(productId = pen, quantity = 100))
        inventoryRepository.save(Inventory(productId = notebook, quantity = 50))

        assertEquals(
            listOf(
                ProductSummary(id = notebook, name = "ノート", price = 330, taxRate = 10, inventoryQuantity = 50),
                ProductSummary(id = pen, name = "ボールペン", price = 150, taxRate = 10, inventoryQuantity = 100),
            ),
            query.findAll(),
        )
    }
}
