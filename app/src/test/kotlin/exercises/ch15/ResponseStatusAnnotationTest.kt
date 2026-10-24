package exercises.ch15

import com.example.shop.ShopApplication
import com.example.shop.TestcontainersConfiguration
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.test.web.servlet.client.RestTestClient

/**
 * 第 15 章の練習問題 2 の解答例が述べる挙動: @ResponseStatus を付けた例外は、最後の受け手の
 * @ExceptionHandler(Exception::class) が先に受けて 500 になる。ResponseStatusException は
 * ResponseEntityExceptionHandler が受けて、指定したステータスになる。
 */
@Import(TestcontainersConfiguration::class, ResponseStatusController::class)
@SpringBootTest(classes = [ShopApplication::class], webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureRestTestClient
class ResponseStatusAnnotationTest(@Autowired private val client: RestTestClient) {

    @Test
    fun `@ResponseStatus を付けた例外は 500 になる`() {
        client.get().uri("/exercises/ch15/annotated")
            .exchange()
            .expectStatus().isEqualTo(500)
    }

    @Test
    fun `ResponseStatusException は指定した 404 になる`() {
        client.get().uri("/exercises/ch15/response-status-exception")
            .exchange()
            .expectStatus().isNotFound()
            .expectBody().jsonPath("$.detail").isEqualTo("ResponseStatusException で 404 を指定した")
    }
}
