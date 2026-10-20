package basics.ch03.returninlambda

// snippet:ch03-return-in-lambda begin
fun eachQuantity(quantities: List<Int>, action: (Int) -> Unit) {
    for (quantity in quantities) {
        action(quantity)
    }
}

fun hasZero(quantities: List<Int>): Boolean {
    eachQuantity(quantities) { quantity ->
        if (quantity == 0) return true
    }
    return false
}
// snippet:ch03-return-in-lambda end
