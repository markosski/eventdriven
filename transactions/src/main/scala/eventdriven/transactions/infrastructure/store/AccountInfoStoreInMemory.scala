package eventdriven.transactions.infrastructure.store

import eventdriven.transactions.domain.model.account.AccountInfo

import scala.collection.mutable

class AccountInfoStoreInMemory(data: mutable.ListBuffer[AccountInfo]) extends AccountInfoStore {
  def get(accountId: Int): Option[AccountInfo] = {
    data.find(_.accountId == accountId)
  }

  def getByCardNumber(cardNumber: Long): Option[AccountInfo] = {
    data.find(_.cardNumber == cardNumber)
  }

  def save(accountInfo: AccountInfo) = {
    val index = data.indexWhere(_.accountId == accountInfo.accountId)
    data.remove(index)
    data.append(accountInfo)
  }
}
