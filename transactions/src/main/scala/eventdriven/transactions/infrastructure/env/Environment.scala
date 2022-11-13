package eventdriven.transactions.infrastructure.env

import eventdriven.core.domain.events.TransactionEvent
import eventdriven.core.infrastructure.store.EventStore
import eventdriven.transactions.usecase.store.AccountInfoStore

case class Environment(
                      accountInfoStore: AccountInfoStore,
                      transactionStore: EventStore[TransactionEvent]
                      )
