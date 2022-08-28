package eventdriven.transactions.domain.aggregate

import eventdriven.core.domain.Aggregate
import eventdriven.core.infrastructure.store.EventStore
import eventdriven.transactions.domain.decisioning.Rules
import eventdriven.transactions.domain.event.payment.{PaymentReturned, PaymentSubmitted}
import eventdriven.transactions.domain.event.transaction.{PreDecisionedTransactionRequest, TransactionDecisioned, TransactionEvent, TransactionPaymentApplied, TransactionPaymentReturned}
import eventdriven.transactions.domain.model.account.AccountInfo
import eventdriven.transactions.domain.model.decision.{Decision, DecisionResult}
import eventdriven.transactions.domain.model.transaction.TransactionSummary

object TransactionSummaryAggregate {
  def init(aggregateId: Int)
          (es: EventStore[TransactionEvent]): Either[Throwable, TransactionSummaryAggregate] = for {
    events <- es.get(aggregateId)
  } yield new TransactionSummaryAggregate(events)
}

class TransactionSummaryAggregate(events: List[TransactionEvent]) extends Aggregate[Int, TransactionSummary, TransactionEvent] {
  override def buildState: Option[TransactionSummary] = {
    if (events.isEmpty) None
    else {
      val state = events
        .foldLeft(TransactionSummary(events.head.accountId, 0)) {
          (state, trx) => trx match {
            case TransactionDecisioned(_, _, _, amt, "Approved", _, _, _) => state.copy(balance = state.balance + amt)
            case TransactionPaymentApplied(_, _, amount, _) => state.copy(balance = state.balance - amount)
            case TransactionPaymentReturned(_, _, amount, _) => state.copy(balance = state.balance + amount)
            case _ => state
          }
        }
      Some(state)
    }
  }

  def handle(preAuth: PreDecisionedTransactionRequest, accountInfo: AccountInfo): Either[Throwable, TransactionEvent] = {
    val decision = for {
      trxSummary <- buildState
    } yield Rules.current.run(preAuth, trxSummary, accountInfo)

    decision match {
      case None => Left(new Exception("could not decision, missing account data"))
      case Some(DecisionResult(Decision.Approved, version, _)) => Right(TransactionDecisioned(accountInfo.accountId, preAuth.cardNumber, "", preAuth.amount, "Approved", "", version, 1234))
      case Some(DecisionResult(Decision.Declined, version, Some(reason))) => Right(TransactionDecisioned(accountInfo.accountId, preAuth.cardNumber ,"", preAuth.amount, "Declined", reason, version, 1234))
      case _ => Left(new Exception("unsupported decision result"))
    }
  }

  def handle(payment: PaymentSubmitted): Either[Throwable, TransactionEvent] = {
    Right(TransactionPaymentApplied(payment.accountId, payment.paymentId, payment.amount, 1234))
  }

  def handle(payment: PaymentReturned): Either[Throwable, TransactionEvent] = {
    Right(TransactionPaymentApplied(payment.accountId, payment.paymentId, payment.amount, 1234))
  }
}
