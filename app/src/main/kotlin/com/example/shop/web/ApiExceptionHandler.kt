package com.example.shop.web

import org.springframework.beans.TypeMismatchException
import org.springframework.dao.ConcurrencyFailureException
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import org.springframework.http.ProblemDetail
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.context.request.WebRequest
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler
import tools.jackson.databind.exc.InvalidNullException
import tools.jackson.databind.exc.MismatchedInputException
import java.net.URI

/**
 * 例外を Problem Details (RFC 9457) の応答に変える。
 *
 * Spring MVC の例外は、継承した ResponseEntityExceptionHandler が受ける。そのうち、入力の誤りの 3 つ (検証の違反・
 * 本文を読めない失敗・型の変換の失敗) は、同じ形 (invalid-request) にそろえる。ほかの 4xx は Spring の既定の本文の
 * まま返す。業務の失敗 (sealed の結果) は例外でないので、ここでなくコントローラが Problem Details にする。
 */
@RestControllerAdvice
class ApiExceptionHandler(private val trackingIds: TrackingIdGenerator) : ResponseEntityExceptionHandler() {

    override fun handleMethodArgumentNotValid(
        ex: MethodArgumentNotValidException,
        headers: HttpHeaders,
        status: HttpStatusCode,
        request: WebRequest,
    ): ResponseEntity<Any>? {
        // クラスに付けた制約の違反は項目を持たないので、本文の全体 (#) を指す。
        val errors = ex.bindingResult.allErrors.map { error ->
            val pointer = if (error is FieldError) jsonPointer(error.field.split('.', '[', ']').filter { it.isNotEmpty() }) else "#"
            RequestError(pointer = pointer, detail = error.defaultMessage.orEmpty())
        }
        return handleExceptionInternal(ex, invalidRequest(errors), headers, status, request)
    }

    /**
     * Kotlin の非 null の項目が JSON に無いときや、値の型が合わないときは、検証の前に Jackson の読み取りで落ちる。
     * Jackson の例外が持つパスから、検証の違反と同じ JSON Pointer を作る。JSON として読めないときは、本文の全体を指す。
     */
    override fun handleHttpMessageNotReadable(
        ex: HttpMessageNotReadableException,
        headers: HttpHeaders,
        status: HttpStatusCode,
        request: WebRequest,
    ): ResponseEntity<Any>? {
        val mismatch = generateSequence<Throwable>(ex) { it.cause }.filterIsInstance<MismatchedInputException>().firstOrNull()
        val error = if (mismatch == null) {
            RequestError(pointer = "#", detail = "本文を JSON として読めません")
        } else {
            RequestError(
                pointer = jsonPointer(mismatch.path.map { it.propertyName ?: it.index.toString() }),
                detail = if (mismatch is InvalidNullException) "値を入れてください" else "値の形が正しくありません",
            )
        }
        return handleExceptionInternal(ex, invalidRequest(listOf(error)), headers, status, request)
    }

    /** クエリやパスの値を、引数の型 (Int や UUID) に変えられなかったとき。 */
    override fun handleTypeMismatch(
        ex: TypeMismatchException,
        headers: HttpHeaders,
        status: HttpStatusCode,
        request: WebRequest,
    ): ResponseEntity<Any>? {
        // @RequestParam と @PathVariable の変換の失敗は、引数の名前を持つ。名前の無いものは Spring の既定の本文にする。
        val parameter = ex.propertyName ?: return super.handleTypeMismatch(ex, headers, status, request)
        val body = invalidRequest(listOf(RequestError(parameter = parameter, detail = "値の形が正しくありません")))
        return handleExceptionInternal(ex, body, headers, status, request)
    }

    /**
     * ほかの処理と同じ行の更新が重なった。読んでから書くまでのあいだにほかの処理が行を更新した楽観ロックの失敗
     * (OptimisticLockingFailureException) と、互いの行のロックを待ち合ったトランザクションの片方をデータベースが
     * 止めたデッドロック (PostgreSQL の 40P01。Spring の既定の変換は PessimisticLockingFailureException にする) は、
     * どちらも ConcurrencyFailureException の子。今の状態との衝突で、送り直せば通りうるので 409 にする。例外の
     * メッセージは、テーブルの名前 (楽観ロックの失敗は ID と版、デッドロックは SQL の文も) を含み、データベースの
     * 作りを見せるので、本文に入れない。
     */
    @ExceptionHandler(ConcurrencyFailureException::class)
    fun handleConcurrencyFailure(ex: ConcurrencyFailureException, request: WebRequest): ResponseEntity<Any>? {
        val body = ProblemType.CONCURRENT_UPDATE.problem("ほかの処理と同じデータの更新が重なりました。もう一度送ってください。")
        return handleExceptionInternal(ex, body, HttpHeaders(), HttpStatus.CONFLICT, request)
    }

    /** ほかのどの handler も受けなかった例外。本文と追跡 ID は [handleExceptionInternal] で作る。 */
    @ExceptionHandler(Exception::class)
    fun handleUnexpected(ex: Exception, request: WebRequest): ResponseEntity<Any>? =
        handleExceptionInternal(ex, null, HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR, request)

    /**
     * どの例外の応答も、最後にここを通る。500 は、Spring MVC の例外の一部 (応答の書き出しの失敗など) も
     * ResponseEntityExceptionHandler が返すので、ここで 500 をまとめて扱う。
     *
     * 500 の本文には、例外の中身を入れない。追跡 ID を作り、例外と一緒にログに書き、本文の instance に入れる。
     * 問い合わせを受けたら、追跡 ID でログを探す。
     */
    override fun handleExceptionInternal(
        ex: Exception,
        body: Any?,
        headers: HttpHeaders,
        statusCode: HttpStatusCode,
        request: WebRequest,
    ): ResponseEntity<Any>? {
        if (statusCode.value() != HttpStatus.INTERNAL_SERVER_ERROR.value()) {
            return super.handleExceptionInternal(ex, body, headers, statusCode, request)
        }
        val trackingId = trackingIds.newTrackingId()
        logger.error("想定していない例外 (追跡 ID: $trackingId)", ex)
        val problem = ProblemDetail.forStatusAndDetail(
            statusCode,
            "サーバーで想定していないエラーが起きました。問い合わせのときは instance の値を伝えてください。",
        ).also { it.instance = URI.create("urn:uuid:$trackingId") }
        return super.handleExceptionInternal(ex, problem, headers, statusCode, request)
    }
}
