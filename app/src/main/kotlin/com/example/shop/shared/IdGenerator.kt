package com.example.shop.shared

import java.util.UUID

/** 集約の ID を作る。テストでは、決まった並びの ID を返すものに差し替える。 */
fun interface IdGenerator {
    fun newId(): UUID
}
