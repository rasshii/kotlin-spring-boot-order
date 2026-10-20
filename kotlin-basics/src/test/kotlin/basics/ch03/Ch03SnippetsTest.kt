package basics.ch03

import basics.assertSnippetOutput
import org.junit.jupiter.api.Test

class Ch03SnippetsTest {
    @Test
    fun lambdaSyntax() = assertSnippetOutput("ch03-lambda-syntax") {
        // snippet:ch03-lambda-syntax begin
        val addTax: (Int) -> Int = { price -> price * 110 / 100 }
        val describe: (String, Int) -> String = { name, quantity -> "$name × $quantity" }
        val quantityOnly: (String, Int) -> Int = { _, quantity -> quantity }
        val thanks: () -> Unit = { println("ご注文ありがとうございます") }

        println(addTax(1000))
        println(describe("ノート", 3))
        println(quantityOnly("ノート", 3))
        thanks()
        // snippet:ch03-lambda-syntax end
    }

    @Test
    fun lambdaIt() = assertSnippetOutput("ch03-lambda-it") {
        // snippet:ch03-lambda-it begin
        val withTax = { price: Int -> price * 110 / 100 }
        val isFreeShipping: (Int) -> Boolean = { it >= 5000 }
        val shippingLabel: (Int) -> String = { subtotal ->
            val fee = if (subtotal >= 5000) 0 else 500
            "小計 $subtotal 円、送料 $fee 円"
        }

        println(withTax(2000))
        println(isFreeShipping(4999))
        println(shippingLabel(3000))
        // snippet:ch03-lambda-it end
    }

    @Test
    fun closure() = assertSnippetOutput("ch03-closure") {
        // snippet:ch03-closure begin
        var placedCount = 0
        val place: (String) -> Unit = { orderId ->
            placedCount++
            println("$orderId を受け付けました")
        }

        place("A-1")
        place("A-2")
        println(placedCount)
        // snippet:ch03-closure end
    }

    @Test
    fun labeledReturn() = assertSnippetOutput("ch03-labeled-return") {
        // snippet:ch03-labeled-return begin
        val quantities = listOf(3, 0, 2)
        quantities.forEach { quantity ->
            if (quantity == 0) return@forEach
            println("数量 $quantity")
        }
        println("確認を終えました")
        // snippet:ch03-labeled-return end
    }

    @Test
    fun customLabel() = assertSnippetOutput("ch03-custom-label") {
        // snippet:ch03-custom-label begin
        val quantities = listOf(3, 0, 2)
        quantities.forEach check@{ quantity ->
            if (quantity == 0) return@check
            println("ラベル: $quantity")
        }
        quantities.forEach(fun(quantity) {
            if (quantity == 0) return
            println("無名関数: $quantity")
        })
        // snippet:ch03-custom-label end
    }

    @Test
    fun samConversion() = assertSnippetOutput("ch03-sam-conversion") {
        // snippet:ch03-sam-conversion begin
        val notice = Runnable { println("在庫の引き当てを始めます") }
        notice.run()
        // snippet:ch03-sam-conversion end
    }
}
