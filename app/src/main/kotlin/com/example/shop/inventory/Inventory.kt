package com.example.shop.inventory

import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Version
import org.springframework.data.relational.core.mapping.Table
import java.util.UUID

/** 商品ごとに持つ、注文に引き当てられる数量。商品の ID を主キーにする。 */
@Table("inventories")
data class Inventory(
    @Id val productId: UUID,
    val quantity: Int,
    @Version val version: Long? = null,
)
