package exercises.ch15

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException

/** 第 15 章の練習問題 2: ステータスコードを注釈で決めた例外。 */
@ResponseStatus(HttpStatus.NOT_FOUND)
class AnnotatedNotFoundException : RuntimeException("注釈で 404 を指定した例外")

/** 第 15 章の練習問題 2: 2 つの書き方で 404 を返そうとするコントローラ。 */
@RestController
class ResponseStatusController {

    @GetMapping("/exercises/ch15/annotated")
    fun annotated(): String = throw AnnotatedNotFoundException()

    @GetMapping("/exercises/ch15/response-status-exception")
    fun responseStatusException(): String = throw ResponseStatusException(HttpStatus.NOT_FOUND, "ResponseStatusException で 404 を指定した")
}
