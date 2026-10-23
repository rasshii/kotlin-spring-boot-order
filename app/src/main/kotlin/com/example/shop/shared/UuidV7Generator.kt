package com.example.shop.shared

import org.springframework.stereotype.Component
import java.util.UUID
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlin.uuid.toJavaUuid

/**
 * 時刻の順に並ぶ UUID (version 7) を作る。主キーの索引への追加が末尾に寄るので、ランダムな version 4 より
 * 索引のページが散らばりにくい。
 *
 * Kotlin の Uuid は実験的 API なので、opt-in をこのクラスに閉じ込め、外へは java.util.UUID で渡す。
 * Java の UUID が version 7 を作れるのは Java 26 から。
 */
@Component
@OptIn(ExperimentalUuidApi::class)
class UuidV7Generator : IdGenerator {
    override fun newId(): UUID = Uuid.generateV7().toJavaUuid()
}
