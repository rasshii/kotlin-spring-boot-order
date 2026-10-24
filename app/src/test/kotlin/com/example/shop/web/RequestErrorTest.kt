package com.example.shop.web

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class RequestErrorTest {

    @Test
    fun `名前と位置を JSON Pointer にし、空の並びは本文の全体を指す`() {
        assertEquals("#/lines/0/quantity", jsonPointer(listOf("lines", "0", "quantity")))
        assertEquals("#", jsonPointer(emptyList()))
        assertEquals("#/a~1b/c~0d", jsonPointer(listOf("a/b", "c~d")))
    }

    /** Bean Validation は違反を順の無い集合で返すので、渡した順に頼らず、位置と説明の順に並べる。 */
    @Test
    fun `errors は、位置と説明の順に並ぶ`() {
        val problem = invalidRequest(
            listOf(
                RequestError(pointer = "#/lines/0/quantity", detail = "数量は 1 以上にしてください"),
                RequestError(pointer = "#/contactEmail", detail = "連絡先メールアドレスを入れてください"),
                RequestError(pointer = "#/contactEmail", detail = "連絡先メールアドレスの形が正しくありません"),
            ),
        )

        assertEquals(
            listOf(
                RequestError(pointer = "#/contactEmail", detail = "連絡先メールアドレスの形が正しくありません"),
                RequestError(pointer = "#/contactEmail", detail = "連絡先メールアドレスを入れてください"),
                RequestError(pointer = "#/lines/0/quantity", detail = "数量は 1 以上にしてください"),
            ),
            problem.properties?.get("errors"),
        )
        assertEquals("入力の値の誤りが 3 件あります", problem.detail)
    }
}
