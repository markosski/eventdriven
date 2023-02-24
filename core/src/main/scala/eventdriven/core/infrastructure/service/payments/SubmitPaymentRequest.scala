package eventdriven.core.infrastructure.service.payments

import eventdriven.core.util.json.mapper

case class SubmitPaymentRequest(amount: Int, source: String)

object SubmitPaymentRequest {
  def fromJson(payload: String): SubmitPaymentRequest = {
    mapper.readValue[SubmitPaymentRequest](payload)
  }
}
