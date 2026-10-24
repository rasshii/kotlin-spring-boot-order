package com.example.shop.web

import com.fasterxml.jackson.annotation.JsonInclude
import org.springframework.http.ProblemDetail

/**
 * 入力の誤り (invalid-request) の errors の 1 件。本文の中の誤りは [pointer] (JSON Pointer)、クエリやパスの値の
 * 誤りは [parameter] (名前) で位置を示し、どちらか一方だけを持つ。
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
data class RequestError(
    val pointer: String? = null,
    val parameter: String? = null,
    val detail: String,
)

/**
 * 入力の誤りの本文を作る。errors は位置と説明の順に並べる。Bean Validation は違反を順の無い集合で返すので、
 * 並べないと、同じリクエストでも応答の errors の順が実行ごとに変わる。
 */
fun invalidRequest(errors: List<RequestError>): ProblemDetail =
    ProblemType.INVALID_REQUEST.problem("入力の値の誤りが ${errors.size} 件あります").also {
        it.setProperty("errors", errors.sortedWith(compareBy<RequestError>({ it.pointer ?: it.parameter }, { it.detail })))
    }

/**
 * プロパティの名前と配列の位置を順に並べた [tokens] を、本文の中の位置を指す JSON Pointer にする。URI の fragment
 * の形 (#/lines/0/quantity) で書き、空の並びは本文の全体 (#) を指す。名前の ~ と / は RFC 6901 のとおり ~0 と ~1 にする。
 * 名前はリクエストのクラスのプロパティの名前 (英数字) なので、fragment に使えない文字の百分率符号化はしない。
 */
fun jsonPointer(tokens: List<String>): String =
    "#" + tokens.joinToString("") { "/" + it.replace("~", "~0").replace("/", "~1") }
