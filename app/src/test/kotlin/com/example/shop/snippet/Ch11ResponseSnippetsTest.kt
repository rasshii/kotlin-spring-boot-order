package com.example.shop.snippet

import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test

/**
 * 第 11 章の curl の例。記事に載せた順に送る。
 * どの応答も既定のエラー応答で日時を含むので、記事の結果は照合せず、ステータスコードだけを確かめる。
 */
class Ch11ResponseSnippetsTest : ResponseSnippetTest() {

    @Test
    @Order(1)
    fun `空のメールアドレスと数量 0 の注文を作る`() =
        assertSnippetResponse("ch11-create-order-invalid-fields", status = 400)

    @Test
    @Order(2)
    fun `同じ商品の明細が重なる注文を作る`() =
        assertSnippetResponse("ch11-create-order-duplicate-product", status = 400)

    @Test
    @Order(3)
    fun `商品の ID が無い明細の注文を作る`() =
        assertSnippetResponse("ch11-exercise-create-order-missing-product-id", status = 400)
}
