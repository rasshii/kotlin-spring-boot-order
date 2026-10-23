package com.example.shop.snippet

import com.example.shop.TestcontainersConfiguration
import org.flywaydb.core.Flyway
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestMethodOrder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles

/**
 * 記事の curl の例を、起動したサーバーに送って、応答を記事の結果 (src/test/resources/snippets/<名前>.out) と比べる。
 *
 * 記事の側のフェンスと .curl・.out の一致は、tech-notes の突き合わせのスクリプトが確かめる。
 * このテストと合わせて、記事の結果が、記事の curl を送った応答であることになる。
 *
 * 読者の手元と同じ条件で動かす。
 * - MockMvc でなく実際のサーバー (RANDOM_PORT) に HTTP で送る。エラーページへの転送はサーブレットコンテナが行うので、
 *   MockMvc では読者の bootRun と応答が違う
 * - bootRun と同じ dev の profile で動かし、同じ初期データを入れる
 * - クラスの初めに、DB を初期データだけの状態に戻し、時計と ID の採番を最初の値に戻す。context はクラスをまたいで
 *   使い回されるので、bean の状態も明示的に戻す
 * - クラスの中は、記事に載せた順に送る (@Order で順を決める。JUnit の既定の順は宣言の順ではない)
 */
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    // DB を戻すために Flyway の clean を使う。本番の設定では無効のまま。
    properties = ["spring.flyway.clean-disabled=false"],
)
@ActiveProfiles("dev")
@Import(TestcontainersConfiguration::class, FixedValuesConfiguration::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
abstract class ResponseSnippetTest {

    @LocalServerPort
    private var port = 0

    @Autowired
    private lateinit var flyway: Flyway

    @Autowired
    private lateinit var clock: SequenceClock

    @Autowired
    private lateinit var ids: SequenceIdGenerator

    @BeforeAll
    fun resetState() {
        flyway.clean()
        flyway.migrate()
        clock.reset()
        ids.reset()
    }

    /**
     * snippets/<[name]>.curl を送り、応答を snippets/<[name]>.out と比べる。
     *
     * 応答に固定できない値が入る例は、記事の結果のフェンスを skip にして .out を置かない。そのときは [status] で
     * ステータスコードを確かめる。
     */
    protected fun assertSnippetResponse(name: String, status: Int? = null) {
        val command = CurlCommand.parse(resource("$name.curl") ?: error("snippets/$name.curl が無い"))
        val response = command.send("http://localhost:$port")
        val expected = resource("$name.out")
        if (expected == null) {
            requireNotNull(status) { "snippets/$name.out が無いときは、ステータスコードを渡す" }
        } else {
            assertEquals(expected.normalized(), command.render(response).normalized(), "$name の応答が snippets/$name.out と違う")
        }
        if (status != null) {
            assertEquals(status, response.statusCode(), "$name のステータスコード")
        }
    }

    private fun resource(path: String): String? =
        javaClass.getResource("/snippets/$path")?.readText(Charsets.UTF_8)

    /** 突き合わせのスクリプトと同じく、行末の空白と前後の空行と共通の字下げを除く。 */
    private fun String.normalized(): String {
        val lines = replace("\r\n", "\n").lines().map { it.trimEnd() }
            .dropWhile { it.isEmpty() }
            .dropLastWhile { it.isEmpty() }
        val indent = lines.filter { it.isNotEmpty() }.minOfOrNull { line -> line.takeWhile { it == ' ' || it == '\t' }.length } ?: 0
        return lines.joinToString("\n") { it.drop(indent) }
    }
}
