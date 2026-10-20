// snippet:ch04-sealed-when-missing begin
sealed interface Payment
data class CreditCard(val last4: String) : Payment
data class BankTransfer(val dueDays: Int) : Payment
data object CashOnDelivery : Payment
data class GiftCard(val code: String) : Payment

fun describe(payment: Payment): String = when (payment) {
    is CreditCard -> "カード払い (下 4 桁 ${payment.last4})"
    is BankTransfer -> "銀行振込 (期限 ${payment.dueDays} 日)"
    CashOnDelivery -> "代金引換"
}
// snippet:ch04-sealed-when-missing end
