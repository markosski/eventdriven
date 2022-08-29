//package eventdriven.transactions.infrastructure.store
//
//import eventdriven.transactions.domain.model.TransactionEvent._
//import eventdriven.transactions.domain.model.TransactionEvent
//import eventdriven.transactions.domain.projection.{TransactionSummary, TransactionSummaryProjection}
//
//import scala.collection.mutable.ListBuffer
//
//class InMemoryTransactionStoreTest extends munit.FunSuite {
//  test("project AccountState") {
//    val data = new ListBuffer[TransactionEvent]()
//
//    val store = new TransactionStoreInMemory(new ListBuffer[TransactionEvent]())
//    store.append(TransactionApproved(1, "123", 10, 10))
//    store.append(TransactionDeclined(1, "124", 10, "risky merchant", 11))
//    store.append(TransactionApproved(1, "125", 15, 12))
//    store.append(TransactionReversed(1, "125", 15, 13))
//    store.append(PaymentMade(1, "200", 5, 14))
//    val events = store.get(1)
//
//    assertEquals(AccountStateProjection(events), Some(TransactionSummary(1, 5)))
//
//    store.append(PaymentReturned(1, "201", 5, "no funds", 15))
//
//    val eventsWithReturnedPayment = store.get(1)
//    assertEquals(AccountStateProjection(eventsWithReturnedPayment), Some(TransactionSummary(1, 10)))
//  }
//}
