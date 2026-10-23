package exercises.ch08

import org.flywaydb.core.Flyway
import org.flywaydb.core.api.FlywayException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.postgresql.PostgreSQLContainer
import org.testcontainers.utility.DockerImageName
import java.sql.DriverManager
import java.sql.SQLException

/**
 * 第 8 章の練習問題 1: 初期データの入った products に列を足すマイグレーションを、本体のマイグレーションの後に当てる。
 * 2 つのテストは別の schema を使い、互いの結果に影響しない。
 */
@Testcontainers
class AddProductDescriptionMigrationTest {

    companion object {
        @Container
        @JvmStatic
        val postgres = PostgreSQLContainer(DockerImageName.parse("postgres:18"))
    }

    private fun flyway(schema: String, exerciseLocation: String): Flyway =
        Flyway.configure()
            .dataSource(postgres.jdbcUrl, postgres.username, postgres.password)
            .schemas(schema)
            .locations("classpath:db/migration", "classpath:db/seed", exerciseLocation)
            .load()

    @Test
    fun `null を許す列なら、行のある products に足せる`() {
        flyway("answer", "classpath:exercises/ch08/answer").migrate()

        DriverManager.getConnection(postgres.jdbcUrl, postgres.username, postgres.password).use { connection ->
            val nullable = connection.prepareStatement(
                """
                select is_nullable from information_schema.columns
                where table_schema = 'answer' and table_name = 'products' and column_name = 'description'
                """.trimIndent(),
            ).executeQuery().use { result -> if (result.next()) result.getString(1) else null }
            val rows = connection.prepareStatement("select count(*) from answer.products")
                .executeQuery().use { result -> result.next(); result.getInt(1) }

            assertEquals("YES", nullable)
            assertEquals(6, rows)
        }
    }

    @Test
    fun `null を許さない列は、行のある products に足せない`() {
        val exception = assertThrows<FlywayException> {
            flyway("notnull", "classpath:exercises/ch08/notnull").migrate()
        }

        // 23502 は PostgreSQL の not_null_violation。SQL の綴りの誤りなど、ほかの理由の失敗と見分ける。
        val sqlState = generateSequence<Throwable>(exception) { it.cause }
            .filterIsInstance<SQLException>()
            .firstOrNull()
            ?.sqlState
        assertEquals("23502", sqlState)
    }
}
