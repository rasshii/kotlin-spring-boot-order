package com.example.shop

import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.context.annotation.Bean
import org.testcontainers.postgresql.PostgreSQLContainer
import org.testcontainers.utility.DockerImageName

/**
 * テストで使う PostgreSQL をコンテナで立てる。
 *
 * `@ServiceConnection` を付けた Bean から、Spring Boot が接続先の URL・ユーザー・パスワードを読み取るので、
 * テスト用の接続設定を設定ファイルに書かなくてよい。
 */
@TestConfiguration(proxyBeanMethods = false)
class TestcontainersConfiguration {

    /** context ごとに 1 つ立てる。context の数だけコンテナが立ち、DB はほかの context と分かれる。 */
    @Bean
    @ServiceConnection
    fun postgresContainer(): PostgreSQLContainer = PostgreSQLContainer(DockerImageName.parse(POSTGRES_IMAGE))

    companion object {
        /**
         * テストで立てる PostgreSQL の image。latest にすると、新しいメジャー版が出た時点でテストの前提が黙って変わる
         * ので、メジャー版で固定する。app/compose.yaml の image も同じ版にそろえる (YAML からはこの定数を参照できない)。
         */
        const val POSTGRES_IMAGE = "postgres:18"
    }
}
