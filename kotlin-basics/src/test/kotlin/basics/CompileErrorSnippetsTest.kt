package basics

import org.jetbrains.kotlin.cli.common.ExitCode
import org.jetbrains.kotlin.cli.common.messages.MessageRenderer
import org.jetbrains.kotlin.cli.jvm.K2JVMCompiler
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.PrintStream
import java.nio.file.Files

/**
 * compile エラーになるコード例 (`src/test/compile-errors/<名前>.kt`) を 1 ファイルずつコンパイラに掛け、
 * 記事に書いたとおりのエラーになることを確かめる。
 *
 * このディレクトリは source set に入れていない。入れると `./gradlew build` の compile がそこで止まり、
 * 他のテストが動かなくなる。
 */
class CompileErrorSnippetsTest {

    @TestFactory
    fun `compile エラーになる例`(): List<DynamicTest> {
        // ディレクトリを見失ったまま 0 件で通らないよう、無ければ失敗にする。
        check(SOURCE_DIR.isDirectory) { "${SOURCE_DIR.absolutePath} が無い" }
        val sources = SOURCE_DIR.listFiles { file -> file.extension == "kt" }
            .orEmpty()
            .sortedBy { it.name }
        return sources.map { source ->
            DynamicTest.dynamicTest(source.nameWithoutExtension) { assertCompileError(source) }
        }
    }

    private fun assertCompileError(source: File) {
        val name = source.nameWithoutExtension
        val resource = "/snippets/$name.err"
        val expected = javaClass.getResource(resource)?.readText(Charsets.UTF_8)
            ?: error("$resource が無い")

        val lines = source.readLines()
        val beginLine = lines.indexOfFirst { it.trim() == "// snippet:$name begin" } + 1
        val endLine = lines.indexOfFirst { it.trim() == "// snippet:$name end" } + 1
        assertTrue(beginLine in 1 until endLine, "$source に目印 $name の begin と end がそろっていない")

        val (exitCode, output) = compile(source)
        assertEquals(ExitCode.COMPILATION_ERROR, exitCode, "$name が compile エラーにならない:\n$output")

        /*
         * エラーが目印の外 (補助の宣言など) で出ていないことを確かめる。外で出ると、記事に載せたコードの
         * エラーかどうか分からなくなる。
         */
        val errorLines = ERROR_LINE.findAll(output).map { it.groupValues[1].toInt() }.toList()
        assertTrue(errorLines.isNotEmpty(), "$name のコンパイラの出力にエラーの行が無い:\n$output")
        assertTrue(
            errorLines.all { it in (beginLine + 1) until endLine },
            "$name のエラーが目印の外 (行 $errorLines) にある:\n$output",
        )

        expected.lines().map { it.trim() }.filter { it.isNotEmpty() }.forEach { line ->
            assertTrue(line in output, "$name のコンパイラの出力に「$line」が無い:\n$output")
        }
    }

    private fun compile(source: File): Pair<ExitCode, String> {
        val outputDir = Files.createTempDirectory("compile-error-snippet").toFile()
        val buffer = ByteArrayOutputStream()
        try {
            /*
             * 出力は Gradle と同じ形式で受ける。既定の形式 (PLAIN_RELATIVE_PATHS) はメッセージの頭の 1 文字を
             * 小文字にするので、記事に載せた Gradle の出力の文面と `.err` が合わなくなる。
             */
            val exitCode = K2JVMCompiler().exec(
                PrintStream(buffer, true, Charsets.UTF_8),
                MessageRenderer.GRADLE_STYLE,
                source.path,
                "-d", outputDir.path,
                "-no-stdlib",
                "-no-reflect",
                "-classpath", STDLIB_PATH,
            )
            return exitCode to buffer.toString(Charsets.UTF_8)
        } finally {
            outputDir.deleteRecursively()
        }
    }

    private companion object {
        /** Gradle はテストをサブプロジェクトのディレクトリで動かすので、相対パスで届く。 */
        val SOURCE_DIR = File("src/test/compile-errors")

        /** 例が使う標準ライブラリ。テストを動かしている JVM が読み込んだものをそのまま渡す。 */
        val STDLIB_PATH: String = File(KotlinVersion::class.java.protectionDomain.codeSource.location.toURI()).path

        /** Gradle の形式のエラー行 (`e: <ファイルの URI>:<行>:<桁> <本文>`) から行番号を取る。 */
        val ERROR_LINE = Regex("""^e: file:\S*?:(\d+):\d+ """, RegexOption.MULTILINE)
    }
}
