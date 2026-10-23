package com.example.shop.snippet

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.Arguments.arguments
import org.junit.jupiter.params.provider.MethodSource
import java.io.File
import java.net.InetAddress
import java.net.ServerSocket
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit

/**
 * [CurlCommand] が parse して [CurlCommand.send] で送るヘッダが、curl が同じコマンドから送るヘッダと同じであること。
 * 違ってよいのは、Host の値 (このテストは 127.0.0.1 の空いているポートに送る) と User-Agent の値と、本文の無い GET の
 * ほかのリクエストに付く `Content-Length: 0` だけ ([CurlCommand.send] の説明)。
 *
 * 期待するヘッダは、parse を使わずに、コマンドの文字列と curl の既定の規則から組む。組めるのは、README の書き方の
 * うち、オプションと値を半角空白 1 つで区切り、`-H` と `-d` の値を単一引用符で囲んだ .curl だけ (`-X` の値は引用符で
 * 囲まない。snippets の .curl はこの形で書く)。ほかの形で書いた `-H`・`-d`・`-X` は期待に入らない。
 * 受けたヘッダは名前でまとめずに 1 行ずつ数えるので、同じ名前の行が重なって届いても見える。行の順は比べない
 * (HttpClient と curl は書く順が違う)。
 */
class CurlCommandSendTest {

    @ParameterizedTest(name = "{0}")
    @MethodSource("commands")
    fun `送るヘッダは、curl が同じコマンドから送るものと同じ`(label: String, text: String) {
        val sent = capture(CurlCommand.parse(text))

        assertEquals(curlHeaders(text), sent, "$label: 送ったヘッダの行")
    }

    /**
     * curl が [text] から送るヘッダの行を、小文字の名前と値の組にして並べる。本文の無い GET のほかのリクエストには、
     * テストだけが付ける `Content-Length: 0` を入れる。
     */
    private fun curlHeaders(text: String): List<Pair<String, String>> {
        val written = HEADER_OPTION.findAll(text)
            .map { it.groupValues[1] }
            .map { it.substringBefore(": ").lowercase() to it.substringAfter(": ") }
            .toList()
        val names = written.map { it.first }
        val body = BODY_OPTION.find(text)?.groupValues?.get(1)
        val method = METHOD_OPTION.find(text)?.groupValues?.get(1) ?: if (body != null) "POST" else "GET"
        val headers = written + listOf("host" to VARYING_VALUE, "user-agent" to VARYING_VALUE) + buildList {
            if (body != null && "content-type" !in names) add("content-type" to "application/x-www-form-urlencoded")
            if ("accept" !in names) add("accept" to "*/*")
            when {
                body != null -> add("content-length" to body.toByteArray(Charsets.UTF_8).size.toString())
                method != "GET" -> add("content-length" to "0")
            }
        }
        return headers.sortedWith(LINE_ORDER)
    }

    /** [command] を、要求を読んで空の応答を返すソケットに送り、受けたヘッダの行を [curlHeaders] と同じ形で返す。 */
    private fun capture(command: CurlCommand): List<Pair<String, String>> =
        ServerSocket(0, 1, InetAddress.getByName("127.0.0.1")).use { server ->
            server.soTimeout = 30_000
            val received = CompletableFuture.supplyAsync {
                server.accept().use { socket ->
                    // 1 バイトを 1 文字として読む。下の skip は文字の数で進むので、Content-Length のバイト数とそろえる。
                    val input = socket.getInputStream().bufferedReader(Charsets.ISO_8859_1)
                    val lines = generateSequence { input.readLine() }.takeWhile { it.isNotEmpty() }.toList()
                    val headers = lines.drop(1).map { line ->
                        line.substringBefore(':').lowercase() to line.substringAfter(':').trim()
                    }
                    // 本文を読み切ってから返す。受けたデータを読み残したまま閉じると、TCP が RST で接続を切ることがある
                    // (RFC 1122 の 4.2.2.13)。
                    var rest = headers.firstOrNull { it.first == "content-length" }?.second?.toLong() ?: 0
                    while (rest > 0) {
                        val skipped = input.skip(rest)
                        check(skipped > 0) { "本文が Content-Length より短い" }
                        rest -= skipped
                    }
                    socket.getOutputStream().write("HTTP/1.1 204 No Content\r\nConnection: close\r\n\r\n".toByteArray())
                    headers
                }
            }
            command.send("http://127.0.0.1:${server.localPort}")
            received.get(30, TimeUnit.SECONDS)
                .map { (name, value) -> name to if (name in VARYING) VARYING_VALUE else value }
                .sortedWith(LINE_ORDER)
        }

    companion object {
        /** 値が環境で変わるヘッダ。名前だけを比べる。 */
        private val VARYING = setOf("host", "user-agent")
        private const val VARYING_VALUE = "<環境で変わる>"

        private val LINE_ORDER = compareBy<Pair<String, String>>({ it.first }, { it.second })

        private val HEADER_OPTION = Regex("""-H '([^']*)'""")
        private val BODY_OPTION = Regex("""-d '([^']*)'""")
        private val METHOD_OPTION = Regex("""-X (\S+)""")

        private const val WRITE_OUT = "-w '\\n%{http_code}\\n'"

        /** snippets の .curl すべてと、本文とヘッダの形ごとの例。 */
        @JvmStatic
        fun commands(): List<Arguments> {
            val snippets = File("src/test/resources/snippets").listFiles { file -> file.extension == "curl" }.orEmpty()
            require(snippets.isNotEmpty()) { "src/test/resources/snippets に .curl が無い" }
            return snippets.sorted().map { arguments(it.name, it.readText(Charsets.UTF_8)) } + listOf(
                arguments("本文の無い POST", "curl -s $WRITE_OUT -X POST http://localhost:8080/api/v1/orders/1/cancel"),
                arguments("フォームの本文", "curl -s $WRITE_OUT http://localhost:8080/api/v1/orders -d 'a=1'"),
                arguments("Accept を書いた GET", "curl -s $WRITE_OUT http://localhost:8080/api/v1/orders/1 -H 'Accept: application/json'"),
                // curl の既定と同じ値を書いた例。既定をもう 1 行足す誤りは、行を名前でまとめると見えない。
                arguments("既定と同じ Accept を書いた GET", "curl -s $WRITE_OUT http://localhost:8080/api/v1/orders/1 -H 'Accept: */*'"),
                arguments(
                    "値の途中に空白のあるヘッダ",
                    "curl -s $WRITE_OUT http://localhost:8080/api/v1/orders -H 'Content-Type: application/json; charset=UTF-8' -d '{}'",
                ),
            )
        }
    }
}
