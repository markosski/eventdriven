package eventdriven.transactions.domain

import eventdriven.core.integration.service.transactions.AuthorizationDecisionRequest
import eventdriven.transactions.domain.entity.account.AccountInfo
import eventdriven.transactions.domain.entity.decision.{Decision, DecisionResult}
import eventdriven.transactions.domain.entity.transaction.TransactionBalance

trait Rule {
  val version: String
  def run(preAuth: AuthorizationDecisionRequest, trxSummary: TransactionBalance, accountInfo: AccountInfo): DecisionResult
}

class Rule1 extends Rule {
  val version = "1"

  override def run(preAuth: AuthorizationDecisionRequest, trxSummary: TransactionBalance, accountInfo: AccountInfo): DecisionResult = {
    val requestedBalance = preAuth.amount + trxSummary.balance + trxSummary.pending

    if (requestedBalance > accountInfo.creditLimit)
      DecisionResult(Decision.Declined, version, Some("over credit limit"))
    else if (preAuth.countryCode != 1)
      DecisionResult(Decision.Declined, version, Some(s"not allowed country code: ${preAuth.countryCode}"))
    else if (preAuth.merchantCode == "AMZN")
      DecisionResult(Decision.Declined, version, Some(s"not allowed risky merchant: ${preAuth.merchantCode}"))
    else
      DecisionResult(Decision.Approved, version, None)
  }
}

object rules {
  val current: Rule = new Rule1
}