package eventdriven.transactions.usecase.store

import eventdriven.transactions.domain.entity.account.AccountInfo

trait AccountInfoStore {
  def get(accountId: Int): Option[AccountInfo]

  def getByCardNumber(cardNumber: Long): Option[AccountInfo]

  def save(accountInfo: AccountInfo): Unit
}
