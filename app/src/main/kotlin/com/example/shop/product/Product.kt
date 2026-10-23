package com.example.shop.product

import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Version
import org.springframework.data.relational.core.mapping.Table
import java.util.UUID

/** 注文できる品物の 1 種類。価格は消費税を含む円の整数、税率はパーセント。 */
@Table("products")
data class Product(
    @Id val id: UUID,
    val name: String,
    val price: Int,
    val taxRate: Int,
    @Version val version: Long? = null,
)
