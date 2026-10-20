fun main() {
    // snippet:ch02-unsafe-call begin
    val productNames = mapOf("P-001" to "ボールペン", "P-002" to "ノート")
    val name: String? = productNames["P-001"]
    println(name.length)
    // snippet:ch02-unsafe-call end
}
