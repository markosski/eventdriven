package eventdriven.core.integration.service.accounts

import eventdriven.core.util.json.mapper

case class UpdateCreditLimitRequest(newCreditLimit: Int)

object UpdateCreditLimitRequest {
  def fromJson(payload: String): UpdateCreditLimitRequest = {
    mapper.readValue[UpdateCreditLimitRequest](payload)
  }
}
