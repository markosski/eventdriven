package eventdriven.transactions.domain.decisioning

import eventdriven.transactions.domain.model.transaction.{PreDecisionedTransactionRequest, TransactionBalance}
import eventdriven.transactions.domain.model.account.AccountInfo
import eventdriven.transactions.domain.model.decision.{Decision, DecisionResult}

trait Rule {
  val version: String
  def run(preAuth: PreDecisionedTransactionRequest, trxSummary: TransactionBalance, accountInfo: AccountInfo): DecisionResult
}

class Rule1 extends Rule {
  val version = "1"

  override def run(preAuth: PreDecisionedTransactionRequest, trxSummary: TransactionBalance, accountInfo: AccountInfo): DecisionResult = {
    val requestedBalance = preAuth.amount + trxSummary.balance

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

object Rules {
  private val rule1 = new Rule1
  val current: Rule = rule1
}