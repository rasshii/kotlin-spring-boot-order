package basics.ch06.exercisecancelanswer

// snippet:ch06-exercise-cancel-answer begin
enum class OrderStatus { DRAFT, CONFIRMED, SHIPPED, CANCELLED }

class Order(val id: String, status: OrderStatus) {
    var status = status
        private set

    val canCancel: Boolean
        get() = status == OrderStatus.DRAFT || status == OrderStatus.CONFIRMED

    fun cancel() {
        check(canCancel) { "取り消せない状態の注文です: $id は $status" }
        status = OrderStatus.CANCELLED
    }
}
// snippet:ch06-exercise-cancel-answer end
