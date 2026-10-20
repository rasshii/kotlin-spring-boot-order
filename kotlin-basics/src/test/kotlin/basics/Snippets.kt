package basics

import org.junit.jupiter.api.Assertions.assertEquals
import java.io.ByteArrayOutputStream
import java.io.PrintStream

/**
 * [block] が標準出力に書いた内容を返す。
 *
 * `println` は `System.out` に書くので、実行のあいだだけ `System.out` を差し替えて受け取る。
 */
fun captureStdout(block: () -> Unit): String {
    val original = System.out
    val buffer = ByteArrayOutputStream()
    System.setOut(PrintStream(buffer, true, Charsets.UTF_8))
    try {
        block()
    } finally {
        System.out.flush()
        System.setOut(original)
    }
    return buffer.toString(Charsets.UTF_8).replace("\r\n", "\n")
}

/**
 * [block] の標準出力が、記事に載せた実行結果 (`src/test/resources/snippets/<name>.out`) と一致することを確かめる。
 *
 * 記事の側の `text` フェンスと `.out` の一致は、tech-notes の突き合わせのスクリプトが確かめる。
 * このテストと合わせて、記事の実行結果がコード例の出力であることになる。
 */
fun assertSnippetOutput(name: String, block: () -> Unit) {
    val resource = "/snippets/$name.out"
    val expected = object {}.javaClass.getResource(resource)
        ?.readText(Charsets.UTF_8)
        ?: error("$resource が無い")
    assertEquals(expected.normalized(), captureStdout(block).normalized(), "$name の出力が $resource と違う")
}

/** 行末の空白と末尾の空行を除く。記事のフェンスは行末の空白を保てないので、そろえてから比べる。 */
private fun String.normalized(): String =
    lines().joinToString("\n") { it.trimEnd() }.trimEnd('\n')
