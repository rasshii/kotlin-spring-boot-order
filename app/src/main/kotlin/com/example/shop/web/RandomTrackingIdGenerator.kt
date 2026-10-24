package com.example.shop.web

import org.springframework.stereotype.Component
import java.util.UUID

/**
 * ランダムな UUID (version 4) を追跡 ID にする。追跡 ID は、応答とログから同じ失敗を探すためだけに使い、並べたり
 * 索引に入れたりしないので、集約の ID のように時刻の順に並ぶ形にしなくてよい。
 */
@Component
class RandomTrackingIdGenerator : TrackingIdGenerator {
    override fun newTrackingId(): UUID = UUID.randomUUID()
}
