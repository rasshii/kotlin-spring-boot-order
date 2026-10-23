package com.example.shop.snippet

import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration

/**
 * 記事に載せた curl のコマンド (src/test/resources/snippets/<名前>.curl) を、テストが送るリクエストに読み替えたもの。
 *
 * 読者が打つのと同じリクエストを送るために、curl の既定の振る舞いを写す。
 * - `-d` があり `-X` が無ければ POST にする
 * - `-d` があり Content-Type のヘッダが無ければ `application/x-www-form-urlencoded` を付ける
 * - Accept のヘッダが無ければ、curl と同じく、どの形式でも受け取る値を付ける
 * このほかに HttpClient が足すヘッダは、HTTP の接続に要るもの (Host と、本文の長さの Content-Length) と User-Agent
 * だけで、これは curl も自分で付ける (User-Agent の値は curl と違う)。curl と違うのは、本文の無い GET のほかの
 * リクエスト (このガイドの例では、12 章の取り消しの POST だけ) に `Content-Length: 0` が付くこと ([send] の説明)。
 * 送ったヘッダがこのとおりであることは CurlCommandSendTest が確かめる。
 *
 * 読めない書き方は、黙って読み飛ばさずに例外にする。読者のシェルや curl が、テストと違う意味に取る書き方
 * (引用符の外のシェルの特殊文字・行の続きでない改行・全角空白、URL の中の curl の glob の文字、ASCII でない
 * ヘッダの値) も例外にする。
 */
data class CurlCommand(
    val method: String,
    val url: String,
    val headers: List<Pair<String, String>>,
    val body: String?,
    val writeOut: String,
) {
    /** 応答を、curl が端末に出すのと同じ形の文字列にする。本文の後に `-w` の書式の結果が続く。 */
    fun render(response: HttpResponse<String>): String =
        response.body() + WRITE_OUT_TOKEN.replace(writeOut) { match ->
            val (name, header) = match.destructured
            when {
                match.value == "\\n" -> "\n"
                header.isNotEmpty() -> response.headers().allValues(header).joinToString(", ")
                name == "http_code" -> response.statusCode().toString().padStart(3, '0')
                name == "content_type" -> response.headers().firstValue("Content-Type").orElse("")
                else -> error("parse で断った -w の変数: ${match.value}")
            }
        }

    /**
     * [origin] (`http://localhost:<ポート>` など) のサーバーに送る。URL の頭の [ARTICLE_ORIGIN] を [origin] に置き換える。
     *
     * 足すヘッダを curl とそろえるために、HttpClient の使い方を次のように決める。
     * - HTTP/1.1 で送る。既定の HTTP/2 は、http の URL に upgrade のヘッダ (Connection・Upgrade・HTTP2-Settings) を足す
     * - 本文の無い GET は、本文の publisher を持たない `GET()` で作る。`method()` に `noBody()` を渡すと、メソッドに
     *   関係なく `Content-Length: 0` が付く。publisher を持たない形を作れるのは `GET()`・`DELETE()`・`HEAD()` だけで、
     *   このガイドの例に DELETE と HEAD は無いので、GET だけをこの形にする。ほかは本文が無くても `Content-Length: 0` が付く
     */
    fun send(origin: String): HttpResponse<String> {
        // parse が URL の頭を確かめている。置き換えずに送ると、手元で動いている bootRun に届いてしまう。
        check(url.startsWith(ARTICLE_ORIGIN)) { "URL の頭が $ARTICLE_ORIGIN でない: $url" }
        val request = HttpRequest.newBuilder(URI.create(origin + url.removePrefix(ARTICLE_ORIGIN)))
            .timeout(Duration.ofSeconds(30))
        if (method == "GET" && body == null) {
            request.GET()
        } else {
            request.method(method, body?.let { HttpRequest.BodyPublishers.ofString(it) } ?: HttpRequest.BodyPublishers.noBody())
        }
        headers.forEach { (name, value) -> request.header(name, value) }
        return CLIENT.send(request.build(), HttpResponse.BodyHandlers.ofString())
    }

    companion object {
        /** 記事の curl が送る先。テストは、起動したサーバーのポートに置き換えて送る。 */
        const val ARTICLE_ORIGIN = "http://localhost:8080"

        /** HTTP/1.1 にする理由は [send] の説明。 */
        private val CLIENT: HttpClient = HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1).build()

        /** -w の書式で扱う書き方。これ以外の `%` と `\` は parse で断る。 */
        private val WRITE_OUT_TOKEN = Regex("""%\{(http_code|content_type)}|%header\{([A-Za-z0-9-]+)}|\\n""")

        /** 引用符の外にあると、シェルが curl に渡す前に意味を変える文字。 */
        private const val SHELL_SPECIALS = "&?*[]\$;|<>(){}!#~`"

        /**
         * `-H` で受けるヘッダの形。値は ASCII の印字できる文字 (途中の空白は可) で、前後に空白を置かない。
         * ASCII でない文字を、curl は UTF-8 のバイトのまま送る。HttpClient は U+0080〜U+00FF を `?` に変えて送り、
         * それより上の文字では `header()` が例外を投げる。
         */
        private val HEADER = Regex("""([A-Za-z0-9-]+): ([!-~](?:[ -~]*[!-~])?)""")

        /** URL にあると、curl が glob として展開する文字。 */
        private const val CURL_GLOB = "[]{}"

        fun parse(text: String): CurlCommand {
            val words = split(text)
            require(words.firstOrNull() == "curl") { "curl で始まっていない: $text" }
            var method: String? = null
            var url: String? = null
            val headers = mutableListOf<Pair<String, String>>()
            var body: String? = null
            var writeOut: String? = null
            val rest = words.drop(1).iterator()
            fun value(option: String): String {
                require(rest.hasNext()) { "$option の値が無い" }
                return rest.next()
            }
            while (rest.hasNext()) {
                when (val word = rest.next()) {
                    "-s" -> Unit
                    "-X" -> method = value(word)
                    "-H" -> {
                        // curl は -H の文字列をそのまま送り、値の空な `名前:` と `名前: ` はそのヘッダを送らない指定として読む。
                        // 送るものを curl とそろえるために、名前・コロン・空白 1 つ・空でない値の形だけを受ける (値の文字は HEADER)。
                        val header = value(word)
                        val (name, headerValue) = requireNotNull(HEADER.matchEntire(header)) {
                            "ヘッダの形が違う (「名前: 値」の形で、値は空にせず、前後に空白を置かず、ASCII の文字だけにする): $header"
                        }.destructured
                        headers += name to headerValue
                    }
                    "-d" -> {
                        require(body == null) { "-d が 2 つ以上ある" }
                        body = value(word).also { require(!it.startsWith("@")) { "-d @ファイル は扱わない" } }
                    }
                    "-w" -> writeOut = value(word).also(::checkWriteOut)
                    else -> {
                        require(!word.startsWith("-")) { "扱えないオプション: $word" }
                        require(url == null) { "URL が 2 つある: $url と $word" }
                        require(word.startsWith(ARTICLE_ORIGIN)) { "URL は $ARTICLE_ORIGIN で始める: $word" }
                        require(word.none { it in CURL_GLOB }) { "URL に curl が glob として読む文字がある: $word" }
                        url = word
                    }
                }
            }
            requireNotNull(url) { "URL が無い: $text" }
            // 記事の結果からステータスコードを読めるように、どの例も最後にステータスコードを出す。
            require(writeOut?.contains("%{http_code}") == true) { "-w に %{http_code} が無い: $text" }
            if (body != null && headers.none { it.first.equals("Content-Type", ignoreCase = true) }) {
                headers += "Content-Type" to "application/x-www-form-urlencoded"
            }
            if (headers.none { it.first.equals("Accept", ignoreCase = true) }) {
                headers += "Accept" to "*/*"
            }
            return CurlCommand(
                method = method ?: if (body != null) "POST" else "GET",
                url = url,
                headers = headers,
                body = body,
                writeOut = writeOut,
            )
        }

        private fun checkWriteOut(template: String) {
            val rest = WRITE_OUT_TOKEN.replace(template, "")
            require('%' !in rest && '\\' !in rest) { "扱えない -w の書き方: $template" }
        }

        /**
         * シェルと同じく、半角空白とタブで語に分ける。単一引用符の中はそのまま 1 つの語にし、引用符の外の `\` の直後の
         * 改行は行の続きとして読み飛ばす。二重引用符と、それ以外の `\` と、引用符の外のシェルの特殊文字は扱わない。
         * 引用符の外の、行の続きでない改行の後に続きがあるものも扱わない。シェルはその改行でコマンドを終えるので、
         * 次の行は別のコマンドになる。全角空白などのほかの空白は、シェルが語の区切りにしないので扱わない。
         */
        private fun split(text: String): List<String> {
            val words = mutableListOf<String>()
            val word = StringBuilder()
            var inWord = false
            var i = 0
            while (i < text.length) {
                val c = text[i]
                when {
                    c == '\'' -> {
                        val end = text.indexOf('\'', i + 1)
                        require(end >= 0) { "単一引用符が閉じていない: $text" }
                        word.append(text, i + 1, end)
                        inWord = true
                        i = end
                    }
                    c == '\\' && text.getOrNull(i + 1) == '\n' -> i++
                    c == '\\' || c == '"' || c in SHELL_SPECIALS ->
                        throw IllegalArgumentException("引用符の外の $c は扱わない: $text")
                    c == '\n' -> {
                        require(text.substring(i).all { it in " \t\n" }) { "行の続きの \\ が無い改行の後に、続きがある: $text" }
                        break
                    }
                    c != ' ' && c != '\t' && (c.isWhitespace() || c.isISOControl()) ->
                        throw IllegalArgumentException("引用符の外の U+${"%04X".format(c.code)} は扱わない: $text")
                    c == ' ' || c == '\t' -> if (inWord) {
                        words += word.toString()
                        word.clear()
                        inWord = false
                    }
                    else -> {
                        word.append(c)
                        inWord = true
                    }
                }
                i++
            }
            if (inWord) words += word.toString()
            return words
        }
    }
}
