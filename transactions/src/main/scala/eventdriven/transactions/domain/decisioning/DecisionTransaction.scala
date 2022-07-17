package eventdriven.transactions.domain.decisioning

import eventdriven.transactions.domain.event.PreDecisionedAuth
import eventdriven.transactions.domain.model.account.AccountInfo
import eventdriven.transactions.domain.model.payment.PaymentSummary
import eventdriven.transactions.domain.model.transaction.{Decision, DecisionResult, TransactionSummary}

object DecisionTransaction {
  def apply(preAuth: PreDecisionedAuth, trxSummary: TransactionSummary, accountInfo: AccountInfo, payments: Option[PaymentSummary]): DecisionResult = {
    val requestedBalance = preAuth.amount + trxSummary.balance - payments.fold(0)(_.totalAmountInCents)

    if (requestedBalance > accountInfo.creditLimit)
      DecisionResult(Decision.Declined, Some("over credit limit"))
    else if (preAuth.countryCode != 1)
      DecisionResult(Decision.Declined, Some(s"not allowed country code: ${preAuth.countryCode}"))
    else if (preAuth.merchantCode == "AMZN")
      DecisionResult(Decision.Declined, Some(s"not allowed risky merchant: ${preAuth.merchantCode}"))
     else
      DecisionResult(Decision.Approved, None)
  }
}
