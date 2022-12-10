package eventdriven.payments.infrastructure.env

import eventdriven.core.infrastructure.messaging.EventPublisher
import eventdriven.payments.usecases.service.TransactionService
import eventdriven.payments.usecases.store.PaymentStore

case class Environment(
                        paymentStore: PaymentStore,
                        eventPublisher: EventPublisher[String],
                        transactionService: TransactionService
                      )
