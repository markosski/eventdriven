package eventdriven.transactions.infrastructure.env

import eventdriven.transactions.domain.events.{SettlementCode, TransactionClearingResultEvent, TransactionDecisionedEvent, TransactionEvent}
import eventdriven.transactions.domain.entity.account.AccountInfo
import eventdriven.transactions.infrastructure.store.{AccountInfoStoreInMemory, TransactionStoreInMemory}

import scala.collection.mutable

object local {
  private val esData = mutable.ListBuffer[TransactionEvent]()
  esData.append(TransactionDecisionedEvent(123, 12345678, "1", 1000, "Approved", "", "1", 1001))
  esData.append(TransactionDecisionedEvent(123, 12345678, "2", 1099, "Approved", "", "1", 1002))
  esData.append(TransactionDecisionedEvent(123, 12345678, "3", 2100, "Approved", "", "1", 1003))
  esData.append(TransactionClearingResultEvent(123, "1", 1000, SettlementCode.CLEAN, 1010))
  esData.append(TransactionClearingResultEvent(123, "2", 1099, SettlementCode.CLEAN, 1011))
  esData.append(TransactionClearingResultEvent(123, "3", 2100, SettlementCode.CLEAN, 1012))
  private val es = new TransactionStoreInMemory(esData)

  private val accountInfoData = mutable.ListBuffer[AccountInfo]()
  accountInfoData.append(AccountInfo(123, 12345678, 50000, "80126", "CO"))
  private val accountInfoStore = new AccountInfoStoreInMemory(accountInfoData)


  def getEnv: Environment =
    Environment(accountInfoStore, es)
}
