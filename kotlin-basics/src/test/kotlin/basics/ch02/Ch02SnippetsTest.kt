package basics.ch02

import basics.assertSnippetOutput
import org.junit.jupiter.api.Test

class Ch02SnippetsTest {
    @Test
    fun valAndVar() = assertSnippetOutput("ch02-val-var") {
        // snippet:ch02-val-var begin
        val orderNumber = 1001
        var quantity = 1
        quantity = quantity + 2
        quantity += 1
        println(orderNumber)
        println(quantity)
        // snippet:ch02-val-var end
    }

    @Test
    fun basicTypes() = assertSnippetOutput("ch02-basic-types") {
        // snippet:ch02-basic-types begin
        val quantity = 3
        val orderNumber = 10_000_000_000
        val weightKg = 2.5
        val discountRate = 0.1f
        val grade = 'A'
        val isAvailable = true
        val productName: String = "ノート"
        println(quantity::class.simpleName)
        println(orderNumber::class.simpleName)
        println(weightKg::class.simpleName)
        println(discountRate::class.simpleName)
        println(grade::class.simpleName)
        println(isAvailable::class.simpleName)
        println(productName::class.simpleName)
        // snippet:ch02-basic-types end
    }

    @Test
    fun numberConversion() = assertSnippetOutput("ch02-number-conversion") {
        // snippet:ch02-number-conversion begin
        val unitPrice = 1_200
        val quantity = 3
        val total: Long = (unitPrice * quantity).toLong()
        println(total)
        println(total.toDouble() / quantity)
        println(7 / 2)
        println(7 % 2)
        println(7 / 2.0)
        // snippet:ch02-number-conversion end
    }

    @Test
    fun stringTemplate() = assertSnippetOutput("ch02-string-template") {
        // snippet:ch02-string-template begin
        val productName = "ノート"
        val unitPrice = 180
        val quantity = 3
        println("商品: $productName")
        println("${productName}を${quantity}冊")
        println("合計: ${unitPrice * quantity} 円")
        println("商品名は \"$productName\"")
        println("\$productName は変数の名前")
        // snippet:ch02-string-template end
    }

    @Test
    fun rawString() = assertSnippetOutput("ch02-raw-string") {
        // snippet:ch02-raw-string begin
        val orderNumber = 1001
        val message = """
            注文番号: $orderNumber
            保存先: C:\shop\orders
        """.trimIndent()
        println(message)
        // snippet:ch02-raw-string end
    }

    @Test
    fun multiDollar() = assertSnippetOutput("ch02-multi-dollar") {
        // snippet:ch02-multi-dollar begin
        val minPrice = 1000
        val filter = $$"""{"price": {"$gte": $$minPrice}}"""
        println(filter)
        // snippet:ch02-multi-dollar end
    }

    @Test
    fun equality() = assertSnippetOutput("ch02-equality") {
        // snippet:ch02-equality begin
        val code = "P-001"
        val built = StringBuilder("P-").append("001").toString()
        println(code == built)
        println(code === built)
        println(code != built)
        // snippet:ch02-equality end
    }

    @Test
    fun safeCall() = assertSnippetOutput("ch02-safe-call") {
        // snippet:ch02-safe-call begin
        val productNames = mapOf("P-001" to "ボールペン", "P-002" to "ノート")
        val found: String? = productNames["P-002"]
        val missing: String? = productNames["P-999"]
        println(found?.length)
        println(missing?.length)
        println(missing?.trim()?.length)
        // snippet:ch02-safe-call end
    }

    @Test
    fun elvis() = assertSnippetOutput("ch02-elvis") {
        // snippet:ch02-elvis begin
        val productNames = mapOf("P-001" to "ボールペン", "P-002" to "ノート")
        val name = productNames["P-999"] ?: "不明な商品"
        val quantity = "三".toIntOrNull() ?: 1
        println(name)
        println(quantity)
        // snippet:ch02-elvis end
    }

    @Test
    fun notNullAssertion() = assertSnippetOutput("ch02-not-null") {
        // snippet:ch02-not-null begin
        val unitPrice: Int? = "1200".toIntOrNull()
        val total = unitPrice!! * 2
        println(total)
        // snippet:ch02-not-null end
    }

    @Test
    fun smartCast() = assertSnippetOutput("ch02-smart-cast") {
        // snippet:ch02-smart-cast begin
        val quantity: Int? = "5".toIntOrNull()
        if (quantity != null) {
            println(quantity * 2)
        }
        if (quantity != null && quantity >= 5) {
            println("5 個以上の注文")
        }
        // snippet:ch02-smart-cast end
    }

    @Test
    fun cast() = assertSnippetOutput("ch02-cast") {
        // snippet:ch02-cast begin
        val attributes: Map<String, Any> = mapOf("code" to "P-001", "inventory" to 12)
        val code = attributes["code"] as String
        val inventory = attributes["inventory"] as? Int
        val weight = attributes["weight"] as? Double
        val wrong = attributes["code"] as? Int
        println(code.length)
        println(inventory)
        println(weight)
        println(wrong)
        // snippet:ch02-cast end
    }

    @Test
    fun ifExpression() = assertSnippetOutput("ch02-if-expression") {
        // snippet:ch02-if-expression begin
        val total = 4_800
        val shippingFee = if (total >= 5_000) 0 else 600
        println(shippingFee)

        val discount = if (total >= 3_000) {
            println("3,000 円以上なので割り引く")
            total / 10
        } else {
            0
        }
        println(discount)
        // snippet:ch02-if-expression end
    }

    @Test
    fun whenExpression() = assertSnippetOutput("ch02-when") {
        // snippet:ch02-when begin
        val status = "SHIPPED"
        val label = when (status) {
            "PENDING" -> "受付済み"
            "SHIPPED", "DELIVERED" -> "出荷済み"
            else -> "不明"
        }
        println(label)

        val quantity = 25
        val size = when (quantity) {
            in 1..9 -> "小口"
            in 10..99 -> "中口"
            else -> "大口"
        }
        println(size)

        val total = 4_800
        val shippingFee = when {
            total >= 5_000 -> 0
            quantity >= 20 -> 300
            else -> 600
        }
        println(shippingFee)
        // snippet:ch02-when end
    }

    @Test
    fun tryExpression() = assertSnippetOutput("ch02-try-expression") {
        // snippet:ch02-try-expression begin
        val input = "12個"
        val quantity = try {
            input.toInt()
        } catch (e: NumberFormatException) {
            0
        } finally {
            println("数量の読み取りを終えた")
        }
        println(quantity)
        // snippet:ch02-try-expression end
    }

    @Test
    fun ranges() = assertSnippetOutput("ch02-ranges") {
        // snippet:ch02-ranges begin
        println((1..5).toList())
        println((1..<5).toList())
        println((5 downTo 1).toList())
        println((0..10 step 5).toList())
        println(3 in 1..5)
        println(7 !in 1..5)
        // snippet:ch02-ranges end
    }

    @Test
    fun forLoop() = assertSnippetOutput("ch02-for-loop") {
        // snippet:ch02-for-loop begin
        val productCodes = listOf("P-001", "P-002", "P-003")
        for (code in productCodes) {
            println(code)
        }
        for (index in productCodes.indices) {
            println("${index + 1} 行目: ${productCodes[index]}")
        }
        // snippet:ch02-for-loop end
    }

    @Test
    fun whileLoop() = assertSnippetOutput("ch02-while") {
        // snippet:ch02-while begin
        var inventory = 10
        var shipped = 0
        while (inventory >= 3) {
            inventory -= 3
            shipped++
        }
        println(shipped)
        println(inventory)

        var attempts = 5
        do {
            attempts++
        } while (attempts < 3)
        println(attempts)
        // snippet:ch02-while end
    }

    @Test
    fun breakAndContinue() = assertSnippetOutput("ch02-break-continue") {
        // snippet:ch02-break-continue begin
        for (quantity in 1..10) {
            if (quantity % 2 == 0) continue
            if (quantity > 5) break
            println(quantity)
        }
        // snippet:ch02-break-continue end
    }
}
