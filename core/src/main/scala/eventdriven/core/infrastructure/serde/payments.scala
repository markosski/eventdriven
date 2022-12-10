package eventdriven.core.infrastructure.serde

import eventdriven.core.util.json.mapper

object payments {
  case class SubmitPaymentResponse(paymentId: String)

  case class SubmitPaymentRequest(amount: Int, source: String)
  object SubmitPaymentRequest {
    def fromJson(payload: String): SubmitPaymentRequest = {
      mapper.readValue[SubmitPaymentRequest](payload)
    }
  }
}
