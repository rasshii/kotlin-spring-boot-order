package com.example.shop.product

import com.example.shop.web.RequestError
import com.example.shop.web.invalidRequest
import com.example.shop.web.toResponseEntity
import org.springframework.http.ProblemDetail
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

/** 商品の一覧の応答。[nextCursor] は続きのページの位置で、最後のページでは null。 */
data class ProductListResponse(
    val items: List<ProductResponse>,
    val nextCursor: String?,
) {
    companion object {
        fun from(page: ProductPage): ProductListResponse =
            ProductListResponse(page.items.map(ProductResponse::from), page.next?.encode())
    }
}

/** 一覧の 1 件の商品。価格は消費税を含む円の整数、税率はパーセント。 */
data class ProductResponse(
    val id: UUID,
    val name: String,
    val price: Int,
    val taxRate: Int,
    val inventoryQuantity: Int,
) {
    companion object {
        fun from(summary: ProductSummary): ProductResponse = ProductResponse(
            id = summary.id,
            name = summary.name,
            price = summary.price,
            taxRate = summary.taxRate,
            inventoryQuantity = summary.inventoryQuantity,
        )
    }
}

@RestController
@RequestMapping("/api/v1/products")
class ProductController(
    private val query: ProductSummaryQuery,
    private val listing: ListingProperties,
) {

    /**
     * 商品の一覧。並び (sort)・件数 (limit)・続きの位置 (cursor)・引き当てられる在庫のある商品だけにするか (allocatable) を受ける。
     * 許可リストに無い並び、範囲の外の件数、読めない続きの位置は、丸めずに 400 (入力の誤り) で断る。
     */
    @GetMapping
    fun list(
        @RequestParam(defaultValue = "id") sort: String,
        @RequestParam limit: Int?,
        @RequestParam cursor: String?,
        @RequestParam(defaultValue = "false") allocatable: Boolean,
    ): ResponseEntity<*> {
        val productSort = ProductSort.fromParameter(sort)
            ?: return invalidParameter("sort", "${ProductSort.entries.joinToString(" か ") { it.parameter }} を指定してください")
        val size = limit ?: listing.defaultLimit
        if (size !in 1..listing.maxLimit) {
            return invalidParameter("limit", "1 から ${listing.maxLimit} までの数を指定してください")
        }
        val after = cursor?.let {
            ProductCursor.decode(productSort, it)
                ?: return invalidParameter("cursor", "同じ並びの前の応答の nextCursor をそのまま指定してください")
        }
        return ResponseEntity.ok(ProductListResponse.from(query.findPage(productSort, after, allocatable, size)))
    }

    private fun invalidParameter(parameter: String, detail: String): ResponseEntity<ProblemDetail> =
        invalidRequest(listOf(RequestError(parameter = parameter, detail = detail))).toResponseEntity()
}
