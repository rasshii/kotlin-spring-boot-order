package com.example.shop.snippet

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.Arguments.arguments
import org.junit.jupiter.params.provider.MethodSource

/** curl のコマンドの読み替えが、curl の既定を写し、読者の curl と違う意味になる書き方を断ること。 */
class CurlCommandTest {

    @Test
    fun `-d があれば POST にし、フォームの Content-Type と Accept を付ける`() {
        val command = CurlCommand.parse("curl -s $WRITE_OUT http://localhost:8080/api/v1/orders -d 'a=1'")

        assertEquals("POST", command.method)
        assertEquals(
            listOf("Content-Type" to "application/x-www-form-urlencoded", "Accept" to "*/*"),
            command.headers,
        )
        assertEquals("a=1", command.body)
    }

    @Test
    fun `書いた Content-Type はそのまま使い、行の続きの中の本文は改行を含めて送る`() {
        val command = CurlCommand.parse(
            "curl -s $WRITE_OUT http://localhost:8080/api/v1/orders \\\n  -H 'Content-Type: application/json' \\\n  -d '{\n  \"a\": 1\n}'",
        )

        assertEquals(listOf("Content-Type" to "application/json", "Accept" to "*/*"), command.headers)
        assertEquals("{\n  \"a\": 1\n}", command.body)
    }

    @Test
    fun `-d も -X も無ければ GET`() {
        assertEquals("GET", CurlCommand.parse("curl -s $WRITE_OUT http://localhost:8080/api/v1/orders/1").method)
    }

    @Test
    fun `タブも語の区切りにし、末尾の改行は読み飛ばす`() {
        val command = CurlCommand.parse("curl\t-s $WRITE_OUT http://localhost:8080/api/v1/orders/1\n")

        assertEquals("http://localhost:8080/api/v1/orders/1", command.url)
    }

    @Test
    fun `ヘッダの値の途中の空白は受ける`() {
        val command = CurlCommand.parse(
            "curl -s $WRITE_OUT http://localhost:8080/api/v1/orders -H 'Content-Type: application/json; charset=UTF-8' -d '{}'",
        )

        assertEquals(listOf("Content-Type" to "application/json; charset=UTF-8", "Accept" to "*/*"), command.headers)
    }

    // 表示名に入力を出す。同じ理由で断る例が並ぶので、理由だけでは、どの例が落ちたかを報告から読めない。
    @ParameterizedTest(name = "{1}: {0}")
    @MethodSource("rejected")
    fun `読者の curl と違う意味になる書き方と、扱わない書き方は、理由を添えて例外にする`(text: String, reason: String) {
        val message = assertThrows<IllegalArgumentException> { CurlCommand.parse(text) }.message

        assertTrue(message != null && reason in message) { "期待した理由「$reason」と違う: $message" }
    }

    companion object {
        private const val WRITE_OUT = "-w '\\n%{http_code}\\n'"

        @JvmStatic
        fun rejected(): List<Arguments> = listOf(
            // 引用符の外のシェルの特殊文字。& は curl を裏で動かし、? は zsh が glob として読む。
            arguments("curl -s $WRITE_OUT http://localhost:8080/api/v1/products?limit=2&after=x", "引用符の外の ?"),
            arguments("curl -s $WRITE_OUT http://localhost:8080/api/v1/orders&", "引用符の外の &"),
            arguments("curl -s $WRITE_OUT http://localhost:8080/api/v1/orders; echo", "引用符の外の ;"),
            arguments("curl -s $WRITE_OUT http://localhost:8080/api/v1/orders | jq", "引用符の外の |"),
            arguments("curl -s $WRITE_OUT http://localhost:8080/api/v1/orders/\$ID", "引用符の外の $"),
            arguments("curl -s $WRITE_OUT \"http://localhost:8080/api/v1/orders\"", "引用符の外の \""),
            // 行の続きにならない \ (後ろに空白がある) と、\ の無い改行。シェルは改行でコマンドを終える。
            arguments("curl -s $WRITE_OUT \\ \n http://localhost:8080/api/v1/orders", "引用符の外の \\"),
            arguments(
                "curl -s $WRITE_OUT http://localhost:8080/api/v1/orders\n  -H 'Content-Type: application/json'",
                "行の続きの \\ が無い改行",
            ),
            // シェルが語の区切りにしない空白 (全角空白、改行しない空白)。
            arguments("curl -s　$WRITE_OUT http://localhost:8080/api/v1/orders", "U+3000"),
            arguments("curl -s $WRITE_OUT http://localhost:8080/api/v1/orders", "U+00A0"),
            // 引用符の中でも、URL の [ ] { } は curl が glob として読む。
            arguments("curl -s $WRITE_OUT 'http://localhost:8080/api/v1/products?ids=[1-2]'", "glob"),
            // 扱わないオプション、扱わない -w の書き方、-w の無い例。
            arguments("curl -s -i $WRITE_OUT http://localhost:8080/api/v1/orders", "扱えないオプション: -i"),
            arguments("curl -s -w '\\t%{http_code}\\n' http://localhost:8080/api/v1/orders", "扱えない -w の書き方"),
            arguments("curl -s -w '%%%{http_code}\\n' http://localhost:8080/api/v1/orders", "扱えない -w の書き方"),
            arguments(
                "curl -s -w '%{time_total} %{http_code}\\n' http://localhost:8080/api/v1/orders",
                "扱えない -w の書き方",
            ),
            arguments("curl -s http://localhost:8080/api/v1/orders", "-w に %{http_code} が無い"),
            // 本文をファイルから読む -d、2 つ目の -d。
            arguments("curl -s $WRITE_OUT http://localhost:8080/api/v1/orders -d @order.json", "-d @ファイル"),
            arguments("curl -s $WRITE_OUT http://localhost:8080/api/v1/orders -d 'a=1' -d 'b=2'", "-d が 2 つ以上ある"),
            // URL が記事の接続先でない、2 つある、無い。
            arguments("curl -s $WRITE_OUT http://localhost:9090/api/v1/orders", "URL は http://localhost:8080 で始める"),
            arguments(
                "curl -s $WRITE_OUT http://localhost:8080/api/v1/orders http://localhost:8080/api/v1/products",
                "URL が 2 つある",
            ),
            arguments("curl -s $WRITE_OUT", "URL が無い"),
            // コマンドの形の誤り。
            arguments("wget http://localhost:8080/api/v1/orders", "curl で始まっていない"),
            arguments("curl -s -w '\\n%{http_code}\\n http://localhost:8080/api/v1/orders", "単一引用符が閉じていない"),
            arguments("curl -s $WRITE_OUT http://localhost:8080/api/v1/orders -H 'Accept'", "ヘッダの形が違う"),
            // curl は値の空なヘッダを送らず、名前の後の空白もそのまま送る。
            arguments("curl -s $WRITE_OUT http://localhost:8080/api/v1/orders -H 'Accept:'", "ヘッダの形が違う"),
            arguments(
                "curl -s $WRITE_OUT http://localhost:8080/api/v1/orders -H 'Content-Type : application/json'",
                "ヘッダの形が違う",
            ),
            // 値は正しく、コロンの後の空白が無いだけの形。「空白 1 つ」の規則だけで断る。
            arguments("curl -s $WRITE_OUT http://localhost:8080/api/v1/orders -H 'Accept:*/*'", "ヘッダの形が違う"),
            // 「名前: 」までは正しく、値の規則だけを 1 つずつ外した例。curl は `Accept: ` も送らない。
            // ASCII でない文字を、curl は UTF-8 のまま送り、HttpClient は é を ? に変えて送る (理由は CurlCommand の HEADER)。
            arguments("curl -s $WRITE_OUT http://localhost:8080/api/v1/orders -H 'Accept: '", "ヘッダの形が違う"),
            arguments("curl -s $WRITE_OUT http://localhost:8080/api/v1/orders -H 'Accept:  */*'", "ヘッダの形が違う"),
            arguments("curl -s $WRITE_OUT http://localhost:8080/api/v1/orders -H 'Accept: */* '", "ヘッダの形が違う"),
            arguments("curl -s $WRITE_OUT http://localhost:8080/api/v1/orders -H 'X-Name: café'", "ヘッダの形が違う"),
            arguments("curl -s $WRITE_OUT http://localhost:8080/api/v1/orders -H", "-H の値が無い"),
        )
    }
}
