package basics.ch05.usesitevariance

import basics.assertSnippetOutput
import org.junit.jupiter.api.Test

// snippet:ch05-use-site-variance begin
fun copyNames(from: MutableList<out CharSequence>, to: MutableList<in String>) {
    for (name in from) {
        to.add(name.toString())
    }
}

fun main() {
    val source: MutableList<String> = mutableListOf("ノート", "ペン")
    val target: MutableList<Any> = mutableListOf()
    copyNames(source, target)
    println(target)
}
// snippet:ch05-use-site-variance end

class UseSiteVarianceTest {
    @Test
    fun copy() = assertSnippetOutput("ch05-use-site-variance") { main() }
}
