package basics.ch06.errorfunction

import basics.assertSnippetOutput
import org.junit.jupiter.api.Test

// snippet:ch06-error-function begin
val unitPrices = mapOf("P-001" to 1500, "P-002" to 800)

fun unitPriceOf(productId: String): Int =
    unitPrices[productId] ?: error("単価が登録されていません: $productId")
// snippet:ch06-error-function end

class ErrorFunctionTest {
    @Test
    fun errorUsage() = assertSnippetOutput("ch06-error-usage") {
        // snippet:ch06-error-usage begin
        println(unitPriceOf("P-002") * 3)
        try {
            unitPriceOf("P-999")
        } catch (e: IllegalStateException) {
            println("${e::class.simpleName}: ${e.message}")
        }
        // snippet:ch06-error-usage end
    }
}
