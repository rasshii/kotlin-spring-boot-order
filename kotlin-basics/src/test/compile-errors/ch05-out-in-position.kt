// snippet:ch05-out-in-position begin
interface Catalog<out T> {
    fun all(): List<T>
    fun add(item: T)
}
// snippet:ch05-out-in-position end
