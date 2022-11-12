package eventdriven.accounts.infrastructure.env

import eventdriven.accounts.usecase.store.AccountStore
import eventdriven.core.outboxpoller.{OutboxPoller}

case class Environment(
                        accountStore: AccountStore,
                        outboxPoller: OutboxPoller,
                      )
