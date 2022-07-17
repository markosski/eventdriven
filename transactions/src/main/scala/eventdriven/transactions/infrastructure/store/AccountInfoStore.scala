package eventdriven.transactions.infrastructure.store

import eventdriven.transactions.domain.model.account.AccountInfo

trait AccountInfoStore {
  def get(accountId: Int): Option[AccountInfo]
}
