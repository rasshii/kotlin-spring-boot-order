package com.example.shop.product

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ProductSortTest {

    /**
     * 同じ値の行の順を、ORDER BY の主キーで決める。PostgreSQL が並びの索引の順に読むあいだは、主キーが無くても
     * 同じ値の行が ID の順に返るので、一覧を読むテストでは気づけない。
     */
    @Test
    fun `どの並びも、最後に主キーで並べる`() {
        ProductSort.entries.forEach { sort ->
            assertTrue(sort.orderBy.endsWith("p.id"), "${sort.name} の ORDER BY が主キーで終わらない: ${sort.orderBy}")
        }
    }
}
