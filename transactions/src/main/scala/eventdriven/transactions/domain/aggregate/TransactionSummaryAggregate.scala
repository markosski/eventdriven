package eventdriven.transactions.domain.aggregate

import eventdriven.core.domain.Aggregate
import eventdriven.core.infrastructure.store.EventStore
import eventdriven.transactions.domain.decisioning.Rules
import eventdriven.transactions.domain.event.transaction.{PreDecisionedTransactionRequest, TransactionDecisioned, TransactionEvent}
import eventdriven.transactions.domain.model.account.AccountInfo
import eventdriven.transactions.domain.model.decision.{Decision, DecisionResult}
import eventdriven.transactions.domain.model.payment.PaymentSummary
import eventdriven.transactions.domain.model.transaction.TransactionSummary
import eventdriven.transactions.infrastructure.store.{AccountInfoStore, PaymentSummaryStore}

object TransactionSummaryAggregate {
  def init(aggregateId: Int)
          (es: EventStore[TransactionEvent],
           accountInfoStore: AccountInfoStore,
           paymentStore: PaymentSummaryStore): Either[Throwable, TransactionSummaryAggregate] = for {
    events <- es.get(aggregateId)
    acctInfo <- accountInfoStore.get(aggregateId).toRight(new Exception("account info does not exist"))
    paymentSummary = paymentStore.get(aggregateId)
  } yield new TransactionSummaryAggregate(events, acctInfo, paymentSummary)
}

class TransactionSummaryAggregate(events: List[TransactionEvent], accountInfo: AccountInfo, paymentSummary: Option[PaymentSummary]) extends Aggregate[Int, TransactionSummary, TransactionEvent] {
  override def buildState: Option[TransactionSummary] = {
    if (events.isEmpty) None
    else {
      val state = events
        .foldLeft(TransactionSummary(events.head.accountId, 0)) {
          (state, trx) => trx match {
            case TransactionDecisioned(_, _, _, amt, "Approved", _, _, _) => state.copy(balance = state.balance + amt)
            case _ => state
          }
        }
      Some(state)
    }
  }

  def handle(preAuth: PreDecisionedTransactionRequest): Either[Throwable, TransactionEvent] = {
    val decision = for {
      trxSummary <- buildState
    } yield Rules.current.run(preAuth, trxSummary, accountInfo, paymentSummary)

    decision match {
      case None => Left(new Exception("could not decision, missing account data"))
      case Some(DecisionResult(Decision.Approved, version, _)) => Right(TransactionDecisioned(accountInfo.accountId, preAuth.cardNumber, "", preAuth.amount, "Approved", "", version, 1234))
      case Some(DecisionResult(Decision.Declined, version, Some(reason))) => Right(TransactionDecisioned(accountInfo.accountId, preAuth.cardNumber ,"", preAuth.amount, "Declined", reason, version, 1234))
      case _ => Left(new Exception("unsupported decision result"))
    }
  }
}
