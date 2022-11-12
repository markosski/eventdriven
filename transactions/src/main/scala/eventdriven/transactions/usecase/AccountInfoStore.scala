package eventdriven.transactions.usecase

import eventdriven.transactions.domain.model.account.AccountInfo

trait AccountInfoStore {
  def get(accountId: Int): Option[AccountInfo]

  def getByCardNumber(cardNumber: Long): Option[AccountInfo]

  def save(accountInfo: AccountInfo): Unit
}
