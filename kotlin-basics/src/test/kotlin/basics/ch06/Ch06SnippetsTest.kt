package basics.ch06

import basics.assertSnippetOutput
import org.junit.jupiter.api.Test

class Ch06SnippetsTest {
    @Test
    fun tryCatchFinally() = assertSnippetOutput("ch06-try-catch-finally") {
        // snippet:ch06-try-catch-finally begin
        for (input in listOf("3", "0", "2個")) {
            try {
                val quantity = input.toInt()
                if (quantity <= 0) {
                    throw IllegalArgumentException("数量は 1 以上にしてください")
                }
                println("数量: $quantity")
            } catch (e: NumberFormatException) {
                println("数字ではありません: $input")
            } catch (e: IllegalArgumentException) {
                println(e.message)
            } finally {
                println("確認済み: $input")
            }
        }
        // snippet:ch06-try-catch-finally end
    }

    @Test
    fun requireNotNullSnippet() = assertSnippetOutput("ch06-require-not-null") {
        // snippet:ch06-require-not-null begin
        val inventories = mapOf("P-001" to 5)

        val available = inventories["P-001"]
        require(available != null) { "在庫の行がありません: P-001" }
        println(available - 2)

        try {
            val missing = requireNotNull(inventories["P-002"]) { "在庫の行がありません: P-002" }
            println(missing - 2)
        } catch (e: IllegalArgumentException) {
            println(e.message)
        }
        // snippet:ch06-require-not-null end
    }

    @Test
    fun runCatchingSnippet() = assertSnippetOutput("ch06-run-catching") {
        // snippet:ch06-run-catching begin
        val parsed = runCatching { "3".toInt() }
        val failed = runCatching { "2個".toInt() }
        println(parsed.isSuccess)
        println(parsed.getOrNull())
        println(failed.isFailure)
        println(failed.getOrNull())
        println(failed.exceptionOrNull()?.let { it::class.simpleName })
        // snippet:ch06-run-catching end
    }

    @Test
    fun resultGetOr() = assertSnippetOutput("ch06-result-get-or") {
        // snippet:ch06-result-get-or begin
        val input = "2個"
        println(runCatching { input.toInt() }.getOrDefault(1))

        val quantity = runCatching { input.toInt() }.getOrElse { e ->
            println("数量として読めないので既定の数量にします: ${e::class.simpleName}")
            1
        }
        println(quantity)
        // snippet:ch06-result-get-or end
    }

    @Test
    fun resultFold() = assertSnippetOutput("ch06-result-fold") {
        // snippet:ch06-result-fold begin
        for (input in listOf("3", "2個")) {
            val message = runCatching { input.toInt() }.fold(
                onSuccess = { quantity -> "数量: $quantity" },
                onFailure = { e -> "数量として読めません: $input (${e::class.simpleName})" },
            )
            println(message)
        }
        // snippet:ch06-result-fold end
    }

    @Test
    fun resultMap() = assertSnippetOutput("ch06-result-map") {
        // snippet:ch06-result-map begin
        val unitPrice = 1500

        val subtotal = runCatching { "3".toInt() }
            .map { quantity -> quantity * unitPrice }
        println(subtotal.getOrNull())

        val fallback = runCatching { "2個".toInt() }
            .recover { 1 }
            .map { quantity -> quantity * unitPrice }
        println(fallback.getOrNull())
        // snippet:ch06-result-map end
    }

    @Test
    fun resultMapCatching() = assertSnippetOutput("ch06-result-map-catching") {
        // snippet:ch06-result-map-catching begin
        val parsed = runCatching { "0".toInt() }

        val caught = parsed.mapCatching { quantity ->
            require(quantity > 0) { "数量は 1 以上にしてください" }
            quantity
        }
        println(caught.exceptionOrNull()?.message)

        try {
            parsed.map { quantity ->
                require(quantity > 0) { "数量は 1 以上にしてください" }
                quantity
            }
        } catch (e: IllegalArgumentException) {
            println("map の外へ投げられました: ${e.message}")
        }
        // snippet:ch06-result-map-catching end
    }

    @Test
    fun runCatchingWide() = assertSnippetOutput("ch06-run-catching-wide") {
        // snippet:ch06-run-catching-wide begin
        val inventories = mapOf("P-001" to 5)
        val bug = runCatching { inventories["P-002"]!! - 2 }
        val unfinished = runCatching<Int> { TODO("在庫の引き当ては未実装") }
        println(bug.exceptionOrNull()?.let { it::class.simpleName })
        println(unfinished.exceptionOrNull()?.let { it::class.simpleName })
        // snippet:ch06-run-catching-wide end
    }

    @Test
    fun nullOr() = assertSnippetOutput("ch06-null-or") {
        // snippet:ch06-null-or begin
        val productNames = mapOf("P-001" to "ノート", "P-002" to "ボールペン")
        println("3".toIntOrNull())
        println("2個".toIntOrNull())
        println(productNames["P-999"])

        val quantity = "2個".toIntOrNull() ?: 1
        val name = productNames["P-999"] ?: "不明な商品"
        println("$name: $quantity 個")
        // snippet:ch06-null-or end
    }
}
