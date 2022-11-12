package eventdriven.transactions.domain.model

import eventdriven.transactions.util.json.mapper

object account {
  case class AccountInfo(accountId: Int, cardNumber: Long, creditLimit: Int, zipOrPostal: String, state: String)
}
