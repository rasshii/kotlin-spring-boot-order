package exercises.ch16

import com.example.shop.order.OrderRepository
import io.mockk.MockKException
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.UUID

/** 第 16 章の練習問題 1 の解答例が述べる挙動: MockK の mock は、振る舞いを決めていない関数を呼ぶと例外を投げる。 */
class StrictMockTest {

    @Test
    fun `振る舞いを決めていない関数を呼ぶと、MockKException になる`() {
        val orderRepository = mockk<OrderRepository>()

        val exception = assertThrows<MockKException> {
            orderRepository.findById(UUID.fromString("019a1c00-0000-7000-8000-000000000001"))
        }

        assertTrue(exception.message.orEmpty().startsWith("no answer found for"))
    }
}
