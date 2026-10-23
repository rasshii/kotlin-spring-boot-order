package com.example.shop.shared

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Clock
import java.time.Duration
import java.time.temporal.ChronoUnit

@Configuration(proxyBeanMethods = false)
class ClockConfiguration {

    /**
     * 現在の日時の出所。日時が要るクラスは Instant.now() を呼ばずにこの bean を受け取る。
     *
     * PostgreSQL の timestamp with time zone はマイクロ秒までしか持たないので、時計もマイクロ秒で刻む。
     * ナノ秒まで返す時計のままだと、保存する前の値 (作成の応答に返す値) と、読み戻した値が食い違う。
     */
    @Bean
    fun clock(): Clock = Clock.tick(Clock.systemUTC(), Duration.of(1, ChronoUnit.MICROS))
}
