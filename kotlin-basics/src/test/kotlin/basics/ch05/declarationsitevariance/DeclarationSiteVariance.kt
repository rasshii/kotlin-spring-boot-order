package basics.ch05.declarationsitevariance

import basics.assertSnippetOutput
import basics.ch05.orderdata.Product
import basics.ch05.orderdata.notebook
import basics.ch05.orderdata.pen
import org.junit.jupiter.api.Test

// snippet:ch05-declaration-site-variance begin
interface Catalog<out T> {
    fun all(): List<T>
}

fun interface Validator<in T> {
    fun validate(value: T): Boolean
}

class ProductCatalog : Catalog<Product> {
    override fun all(): List<Product> = listOf(notebook, pen)
}

fun countAll(catalog: Catalog<Any>): Int = catalog.all().size

fun main() {
    println(countAll(ProductCatalog()))

    val notBlank: Validator<CharSequence> = Validator { it.isNotBlank() }
    val nameValidator: Validator<String> = notBlank
    println(nameValidator.validate("ノート"))
    println(nameValidator.validate(" "))
}
// snippet:ch05-declaration-site-variance end

class DeclarationSiteVarianceTest {
    @Test
    fun variance() = assertSnippetOutput("ch05-declaration-site-variance") { main() }
}
