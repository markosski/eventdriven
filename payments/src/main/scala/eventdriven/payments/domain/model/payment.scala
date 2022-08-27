package eventdriven.payments.domain.model

object payment {
    case class PaymentProcessed(accountId: Int, paymentId: String, amountInCents: Int)
}
