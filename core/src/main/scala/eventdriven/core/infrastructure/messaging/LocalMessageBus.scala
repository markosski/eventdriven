//package eventdriven.core.infrastructure.messaging
//
//import java.util.concurrent.{LinkedBlockingQueue, TimeUnit}
//import scala.collection.mutable
//import scala.util.Try
//
//object LocalMessageBus {
//  val queues = mutable.Map[String, LinkedBlockingQueue[Any]]()
//
//  def createIfNotExists(topic: String): Unit = {
//    LocalMessageBus.queues.get(topic) match {
//      case Some(_) => ()
//      case None => LocalMessageBus.queues.put(topic, new LinkedBlockingQueue[Any])
//    }
//  }
//}
//
//class LocalEventDispatcher[E](topic: String) extends EventDispatcher[E] {
//  LocalMessageBus.createIfNotExists(topic)
//
//  override def publish(event: E): Either[Throwable, Unit] = {
//    Try(LocalMessageBus.queues(topic).put(event)).toEither
//  }
//}
//
//class LocalEventListener[E](topic: String, poisonPillValue: E) extends EventListener[E] {
//  LocalMessageBus.createIfNotExists(topic)
//
//  override def take: Option[E] = {
//    Option(LocalMessageBus.queues(topic).take().asInstanceOf[E]) match {
//      case Some(s) if s == poisonPill => None
//      case a => a
//    }
//  }
//
//
//  def poisonPill: E = poisonPillValue
//}
