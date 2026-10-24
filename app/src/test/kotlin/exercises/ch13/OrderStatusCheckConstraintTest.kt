package exercises.ch13

import com.example.shop.ShopApplication
import com.example.shop.TestcontainersConfiguration
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.jdbc.core.simple.JdbcClient
import org.springframework.transaction.annotation.Transactional
import java.sql.SQLException
import java.util.UUID

/**
 * 第 13 章の練習問題 1 の解答例が述べる挙動: orders.status の check 制約は、PENDING と CANCELLED のほかの状態の注文を
 * データベースが断る。Kotlin の enum に状態を足しても、この制約はコンパイルでは気づけない。
 */
@Import(TestcontainersConfiguration::class)
@SpringBootTest(classes = [ShopApplication::class])
@Transactional
class OrderStatusCheckConstraintTest(@Autowired private val jdbcClient: JdbcClient) {

    private fun insertOrder(status: String) =
        jdbcClient.sql(
            "insert into orders (id, contact_email, status, ordered_at, version) values (:id, 'taro@example.com', :status, now(), 0)",
        )
            .param("id", UUID.fromString("019a1c00-0000-7000-8000-000000000001"))
            .param("status", status)
            .update()

    @Test
    fun `制約に無い状態の注文は、データベースが check 制約で断る`() {
        val exception = assertThrows<DataIntegrityViolationException> { insertOrder("SHIPPED") }

        // 23514 は PostgreSQL の check_violation。列の長さの誤りなど、ほかの理由の失敗と見分ける。
        val sqlState = generateSequence<Throwable>(exception) { it.cause }
            .filterIsInstance<SQLException>()
            .firstOrNull()
            ?.sqlState
        assertEquals("23514", sqlState)
    }
}
