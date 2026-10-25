package exercises.ch16

import com.example.shop.ShopApplication
import com.example.shop.TestcontainersConfiguration
import com.example.shop.product.ProductRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.data.jdbc.test.autoconfigure.DataJdbcTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.ContextConfiguration

/**
 * 第 16 章の練習問題 2 の解答例が述べる挙動: dev の profile を付けないテストには、開発の初期データが入らない。
 * Flyway が当てるのは既定の場所 (db/migration) だけで、初期データ (db/seed) は application-dev.yaml が足す。
 */
@DataJdbcTest
@ContextConfiguration(classes = [ShopApplication::class])
@Import(TestcontainersConfiguration::class)
class SliceWithoutSeedTest(@Autowired private val productRepository: ProductRepository) {

    @Test
    fun `dev の profile を付けないテストの DB には、初期データの商品が無い`() {
        assertEquals(0, productRepository.count())
    }
}
