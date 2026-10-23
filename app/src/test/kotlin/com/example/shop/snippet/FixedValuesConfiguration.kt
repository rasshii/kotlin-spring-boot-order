package com.example.shop.snippet

import com.example.shop.shared.IdGenerator
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset
import java.util.UUID
import java.util.concurrent.atomic.AtomicLong

/**
 * 呼ばれるたびに [step] ずつ進んだ時刻を返す時計。記事に載せる応答の日時を、テストの実行のたびに同じにする。
 * 1 つの値だけを返すと、同じテストで作った 2 件の注文の日時が同じになり、並びの例を作れない。
 */
class SequenceClock(private val start: Instant, private val step: Duration) : Clock() {
    private val calls = AtomicLong()

    override fun instant(): Instant = start.plus(step.multipliedBy(calls.getAndIncrement()))

    override fun getZone(): ZoneId = ZoneOffset.UTC

    override fun withZone(zone: ZoneId): Clock = throw UnsupportedOperationException("タイムゾーンを変えた時計は作らない")

    fun reset() = calls.set(0)
}

/**
 * 呼ばれるたびに末尾の番号を 1 ずつ増やした ID を返す。1 つの ID だけを返すと、2 件目の保存で主キーが重なる。
 */
class SequenceIdGenerator(private val prefix: String) : IdGenerator {
    private val calls = AtomicLong()

    // 既定のロケールで数字を書く format は使わない (ASCII の数字にならないロケールがある)。
    override fun newId(): UUID = UUID.fromString(prefix + calls.incrementAndGet().toString().padStart(12, '0'))

    fun reset() = calls.set(0)
}

/** 本体の時計と ID の採番を、決まった並びを返すものに差し替える。どちらも @Primary なので、本体の bean より優先される。 */
@TestConfiguration(proxyBeanMethods = false)
class FixedValuesConfiguration {

    @Bean
    @Primary
    fun sequenceClock() = SequenceClock(start = Instant.parse("2026-10-24T01:00:00Z"), step = Duration.ofMinutes(1))

    @Bean
    @Primary
    fun sequenceIdGenerator() = SequenceIdGenerator(prefix = "019a1c00-0000-7000-8000-")
}
