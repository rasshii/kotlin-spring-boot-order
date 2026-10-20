package com.example.shop

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.jdbc.core.simple.JdbcClient

@Import(TestcontainersConfiguration::class)
@SpringBootTest
class ShopApplicationTests(@Autowired private val jdbcClient: JdbcClient) {

    /*
     * context が起動するだけのテストは、Flyway が依存から外れていても通る。Flyway がマイグレーションを
     * 当てたことは、履歴テーブルに V1 が成功として記録されていることで確かめる。
     * 本体の Flyway の starter が抜けたことに気づけるのは、テストの依存が Flyway の自動設定を持ち込まない
     * あいだだけ (app/build.gradle.kts の testImplementation の注記)。
     */
    @Test
    fun `起動時に Flyway が V1 を当てる`() {
        val results = jdbcClient
            .sql("select success from flyway_schema_history where version = '1'")
            .query(Boolean::class.java)
            .list()

        assertEquals(listOf(true), results)
    }
}
