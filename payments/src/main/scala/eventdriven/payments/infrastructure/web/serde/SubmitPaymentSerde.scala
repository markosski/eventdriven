package eventdriven.payments.infrastructure.web.serde

import eventdriven.core.util.json.mapper

case class SubmitPaymentSerde(amount: Int, source: String)

object SubmitPaymentSerde {
  def fromJson(payload: String): SubmitPaymentSerde = {
    mapper.readValue[SubmitPaymentSerde](payload)
  }
}
