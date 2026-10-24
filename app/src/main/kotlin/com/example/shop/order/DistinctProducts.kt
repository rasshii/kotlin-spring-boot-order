package com.example.shop.order

import jakarta.validation.Constraint
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import jakarta.validation.Payload
import kotlin.reflect.KClass

/**
 * 注文明細のリストが、同じ商品の明細を 2 行以上持たないこと。同じ商品の明細が 2 行あると、どちらの数量で注文する
 * のかが決まらない。
 *
 * リストそのものに付けるので、違反の位置がリストの項目 (lines) になり、応答で本文の位置 (#/lines) として示せる。
 */
@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [DistinctProductsValidator::class])
annotation class DistinctProducts(
    val message: String = "同じ商品の注文明細が重なっています",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = [],
)

class DistinctProductsValidator : ConstraintValidator<DistinctProducts, List<CreateOrderLineRequest>> {
    // Bean Validation の制約は null を正しいとみなす決まり。null の誤りは @NotEmpty が出す。
    override fun isValid(value: List<CreateOrderLineRequest>?, context: ConstraintValidatorContext): Boolean =
        value == null || value.distinctBy { it.productId }.size == value.size
}
