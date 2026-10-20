package basics.ch03.extensionresolution

import basics.assertSnippetOutput
import org.junit.jupiter.api.Test

// snippet:ch03-extension-resolution begin
data class Product(val name: String, val price: Int) {
    fun describe(): String = "メンバー: $name"
}

fun Product.describe(): String = "拡張: $name"

fun Product?.nameOrDefault(): String = this?.name ?: "商品なし"

fun findProduct(name: String): Product? =
    if (name == "ノート") Product("ノート", 500) else null

fun main() {
    val note = Product("ノート", 500)
    println(note.describe())
    println(findProduct("ノート").nameOrDefault())
    println(findProduct("消しゴム").nameOrDefault())
}
// snippet:ch03-extension-resolution end

class ExtensionResolutionTest {
    @Test
    fun extensionResolution() = assertSnippetOutput("ch03-extension-resolution") { main() }
}
