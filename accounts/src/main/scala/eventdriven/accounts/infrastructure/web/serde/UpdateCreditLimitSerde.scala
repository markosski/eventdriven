package eventdriven.accounts.infrastructure.web.serde

import eventdriven.core.util.json.mapper

object UpdateCreditLimitSerde {
  case class UpdateCreditLimitPayload(oldCreditLimit: Int, newCreditLimit: Int)

  def fromJson(payload: String): UpdateCreditLimitPayload = {
    mapper.readValue[UpdateCreditLimitPayload](payload)
  }
}
