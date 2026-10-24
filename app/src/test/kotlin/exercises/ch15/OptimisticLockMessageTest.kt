package exercises.ch15

import com.example.shop.ShopApplication
import com.example.shop.TestcontainersConfiguration
import com.example.shop.inventory.Inventory
import com.example.shop.inventory.InventoryRepository
import com.example.shop.product.Product
import com.example.shop.product.ProductRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.dao.OptimisticLockingFailureException
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

/**
 * 第 15 章の練習問題 1 の解答例が述べる挙動: 読んだ後にほかの保存で版が進んだ在庫を保存すると、楽観ロックの
 * 失敗になる。例外のメッセージは、行の ID・読んだときの版・テーブルの名前を含む。
 */
@Import(TestcontainersConfiguration::class)
@SpringBootTest(classes = [ShopApplication::class])
@Transactional
class OptimisticLockMessageTest(
    @Autowired private val productRepository: ProductRepository,
    @Autowired private val inventoryRepository: InventoryRepository,
) {
    private val marker = UUID.fromString("0199f3a8-0000-7000-8000-000000000004")

    @Test
    fun `古い版の在庫を保存すると、楽観ロックの失敗のメッセージに ID と版とテーブルの名前が入る`() {
        productRepository.save(Product(id = marker, name = "マーカー", price = 880, taxRate = 10))
        inventoryRepository.save(Inventory(productId = marker, quantity = 3))
        val read = inventoryRepository.findById(marker).orElseThrow()

        // 同じ在庫を読んだ 2 つの注文のうち、先の注文が 1 本を引き当てて保存する。
        inventoryRepository.save(read.allocate(1))
        // 後の注文は、先の保存の前に読んだ在庫 (版 0) のまま保存しようとする。
        val exception = assertThrows<OptimisticLockingFailureException> { inventoryRepository.save(read.allocate(2)) }

        assertEquals(
            "Failed to update versioned entity with id '$marker' (version '0') in table [\"inventories\"]; " +
                "Was the entity updated or deleted concurrently?",
            exception.message,
        )
    }
}
