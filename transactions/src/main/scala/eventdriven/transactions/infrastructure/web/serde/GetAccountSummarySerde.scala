package eventdriven.transactions.infrastructure.web.serde

import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import eventdriven.transactions.domain.model.account.AccountSummary

object GetAccountSummarySerde {
  val mapper = JsonMapper.builder()
    .addModule(DefaultScalaModule)
    .build()

  def toJson(accountSummary: AccountSummary): String = {
    mapper.writeValueAsString(accountSummary)
  }

  case class AccountSummaryResponse(
                                     accountId: Int,
                                     creditLimit: Int,
                                     balance: Int,
                                     zipOrPostal: String,
                                     state: String
                                   )
}
