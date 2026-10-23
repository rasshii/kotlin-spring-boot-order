package exercises.ch09

import com.example.shop.ShopApplication
import com.example.shop.TestcontainersConfiguration
import com.example.shop.inventory.Inventory
import com.example.shop.inventory.InventoryRepository
import com.example.shop.product.Product
import com.example.shop.product.ProductRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.jdbc.core.simple.JdbcClient
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

// exercises.ch09 は com.example.shop の外なので、起動するアプリのクラスを指定する。
@Import(TestcontainersConfiguration::class)
@SpringBootTest(classes = [ShopApplication::class])
@Transactional
class AllocatableProductQueryTest(
    @Autowired private val jdbcClient: JdbcClient,
    @Autowired private val productRepository: ProductRepository,
    @Autowired private val inventoryRepository: InventoryRepository,
) {

    @Test
    fun `在庫が 1 個以上の商品だけを返し、0 の商品は返さない`() {
        val marker = UUID.fromString("0199f3a8-0000-7000-8000-000000000004")
        val coffee = UUID.fromString("0199f3a8-0000-7000-8000-000000000005")
        val tea = UUID.fromString("0199f3a8-0000-7000-8000-000000000006")
        productRepository.save(Product(id = marker, name = "マーカー", price = 880, taxRate = 10))
        productRepository.save(Product(id = coffee, name = "コーヒー豆", price = 1280, taxRate = 8))
        productRepository.save(Product(id = tea, name = "緑茶", price = 540, taxRate = 8))
        inventoryRepository.save(Inventory(productId = marker, quantity = 1))
        inventoryRepository.save(Inventory(productId = coffee, quantity = 20))
        inventoryRepository.save(Inventory(productId = tea, quantity = 0))

        val products = AllocatableProductQuery(jdbcClient).findAllocatable()

        assertEquals(listOf(marker, coffee), products.map { it.id })
    }
}
