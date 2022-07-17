package eventdriven.transactions.infrastructure.store

import eventdriven.core.infrastructure.store.EventStore
import eventdriven.transactions.domain.event.transaction.TransactionEvent

import scala.collection.mutable
import scala.util.Try


class TransactionStoreInMemory(data: mutable.ListBuffer[TransactionEvent]) extends EventStore[TransactionEvent] {
  override def get(entityId: Int): Either[Throwable, List[TransactionEvent]] = {
    Try(data.filter(_.accountId == entityId).toList).toEither
  }

  override def append(transaction: TransactionEvent): Either[Throwable, Unit] = {
    Right(data.append(transaction))
  }
}