package eventdriven.transactions.infrastructure.env

import eventdriven.transactions.domain.events.{SettlementCode, TransactionClearingResultEvent, TransactionDecisionedEvent, TransactionEvent}
import eventdriven.transactions.domain.entity.account.AccountInfo
import eventdriven.transactions.domain.entity.decision.Decision
import eventdriven.transactions.infrastructure.store.{AccountInfoStoreInMemory, TransactionStoreInMemory}

import scala.collection.mutable

object local {
  private val esData = mutable.ListBuffer[TransactionEvent]()
  esData.append(TransactionDecisionedEvent(123, 12345678, "1", 1000, Decision.Approved, "", "1", 1678338526742L))
  esData.append(TransactionDecisionedEvent(123, 12345678, "2", 1099, Decision.Approved, "", "1", 1678338526743L))
  esData.append(TransactionDecisionedEvent(123, 12345678, "3", 2100, Decision.Approved, "", "1", 1678338526744L))
  esData.append(TransactionClearingResultEvent(123, "1", 1000, SettlementCode.CLEAN, 1678338526745L))
  esData.append(TransactionClearingResultEvent(123, "2", 1099, SettlementCode.CLEAN, 1678338526746L))
  esData.append(TransactionClearingResultEvent(123, "3", 2100, SettlementCode.CLEAN, 1678338526747L))
  private val es = new TransactionStoreInMemory(esData)

  private val accountInfoData = mutable.ListBuffer[AccountInfo]()
  accountInfoData.append(AccountInfo(123, 12345678, 50000, "80126", "CO"))
  private val accountInfoStore = new AccountInfoStoreInMemory(accountInfoData)


  def getEnv: Environment =
    Environment(accountInfoStore, es)
}
