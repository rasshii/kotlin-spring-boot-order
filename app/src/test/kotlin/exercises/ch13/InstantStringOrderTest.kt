package exercises.ch13

import com.example.shop.ShopApplication
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.json.JsonTest
import org.springframework.test.context.ContextConfiguration
import tools.jackson.databind.json.JsonMapper
import java.time.Instant

/** 第 13 章の練習問題 2 の解答例が述べる挙動: orderedAt を文字列のまま並べると、時刻の順にならないことがある。 */
@JsonTest
@ContextConfiguration(classes = [ShopApplication::class])
class InstantStringOrderTest(@Autowired private val jsonMapper: JsonMapper) {

    @Test
    fun `秒ちょうどの時刻は小数部を持たず、同じ秒の 0_5 秒後の時刻より文字列では後ろに並ぶ`() {
        val onTheSecond = jsonMapper.writeValueAsString(Instant.parse("2026-10-24T01:00:00Z"))
        val halfSecondLater = jsonMapper.writeValueAsString(Instant.parse("2026-10-24T01:00:00.500Z"))

        assertEquals("\"2026-10-24T01:00:00Z\"", onTheSecond)
        assertEquals("\"2026-10-24T01:00:00.500Z\"", halfSecondLater)
        // Z (U+005A) は . (U+002E) より後ろの文字なので、早い時刻のほうが文字列では大きくなる。
        assertTrue(onTheSecond > halfSecondLater)
    }
}
