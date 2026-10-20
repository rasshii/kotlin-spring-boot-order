// snippet:ch05-reified-required begin
fun <T : Any> typeNameOf(): String = T::class.simpleName ?: "名前なし"
// snippet:ch05-reified-required end
