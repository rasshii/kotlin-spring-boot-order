package basics.ch06.exercisequantity

// snippet:ch06-exercise-quantity begin
fun parseQuantity(text: String): Int? {
    val quantity = text.toIntOrNull() ?: return null
    if (quantity !in 1..99) return null
    return quantity
}
// snippet:ch06-exercise-quantity end
