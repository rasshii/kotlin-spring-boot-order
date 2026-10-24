package com.example.shop.web

import java.util.UUID

/** 500 の応答ごとに追跡 ID を作る。テストでは、決まった値を返すものに差し替える。 */
fun interface TrackingIdGenerator {
    fun newTrackingId(): UUID
}
