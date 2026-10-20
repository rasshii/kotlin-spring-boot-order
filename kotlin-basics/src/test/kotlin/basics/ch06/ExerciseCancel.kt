package basics.ch06.exercisecancel

// snippet:ch06-exercise-cancel begin
enum class OrderStatus { DRAFT, CONFIRMED, SHIPPED, CANCELLED }

class Order(val id: String, status: OrderStatus) {
    var status = status
        private set

    fun cancel() {
        require(status != OrderStatus.SHIPPED) { "発送済みの注文は取り消せません: $id" }
        status = OrderStatus.CANCELLED
    }
}
// snippet:ch06-exercise-cancel end
