package com.example.shop.web

import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.http.ResponseEntity
import java.net.URI

private const val DOCUMENT = "https://notes.rewheel.dev/docs/kotlin-spring-boot-api/error-responses"

/**
 * このアプリが定める問題の種類 (RFC 9457 の problem type)。クライアントは type の URI で種類を見分ける。
 *
 * type の URI は、ガイドの第 15 章の、問題ごとの節を指す。RFC 9457 の 4 節は、type の URI が問題の解き方を
 * 説明する文書を返すことを求める (SHOULD) ので、節には問題の意味・ステータスコード・拡張の項目・クライアントの対処を書く。
 * URI の末尾の名前は、節の見出しの id と同じにする。
 */
enum class ProblemType(anchor: String, val status: HttpStatus, val title: String) {
    INVALID_REQUEST("invalid-request", HttpStatus.BAD_REQUEST, "入力の値に誤りがあります"),
    PRODUCT_NOT_FOUND("product-not-found", HttpStatus.UNPROCESSABLE_CONTENT, "注文した商品がありません"),
    INSUFFICIENT_INVENTORY("insufficient-inventory", HttpStatus.CONFLICT, "在庫が足りません"),
    ORDER_CANCELLED("order-cancelled", HttpStatus.CONFLICT, "取消の注文は取り消せません"),
    CONCURRENT_UPDATE("concurrent-update", HttpStatus.CONFLICT, "ほかの更新と重なりました"),
    ;

    val uri: URI = URI.create("$DOCUMENT#$anchor")

    /** この種類の問題の本文を作る。[detail] は、この発生に固有の説明。 */
    fun problem(detail: String): ProblemDetail =
        ProblemDetail.forStatusAndDetail(status, detail).also {
            it.type = uri
            it.title = title
        }
}

/** 本文の status と同じステータスコードの応答にする (RFC 9457 は、2 つを同じにすることを求める)。 */
fun ProblemDetail.toResponseEntity(): ResponseEntity<ProblemDetail> = ResponseEntity.of(this).build()
