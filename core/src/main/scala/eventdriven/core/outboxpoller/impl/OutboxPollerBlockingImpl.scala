package eventdriven.core.outboxpoller.impl

import org.slf4j.LoggerFactory
import eventdriven.core.outboxpoller.{OutboxEventStore, OutboxPoller, OutboxPublisher}

import scala.util.Try

class OutboxPollerBlockingImpl[E](store: OutboxEventStore[E], publisher: OutboxPublisher[E]) extends OutboxPoller {
  private val logger = LoggerFactory.getLogger(this.getClass)
  private val checkIntervalLoopInMillis = 10
  private val wakeUpInterval = 60 * 1000
  private val limit = 100
  private var lastCheckTimestamp: Long = 0
  private var whileLoop = true

  def run(): Unit = Try {
    while (whileLoop) {
      if (timeNow() - lastCheckTimestamp > wakeUpInterval) {
        logger.info("checking for events to publish.")
        (for {
          events <- store.getUnpublished(limit)
          _ = logger.info(s"found ${events.size} events")
          published = events.map { x =>
            publisher.publish(x)
            store.deleteEvent(x)
          }
        } yield published) match {
          case Right(published) =>
            if (published.size == limit) {
              logger.info("more events to publish, continuing...")
              lastCheckTimestamp = 0
            } else {
              logger.info(s"finished processing, will go to sleep for $wakeUpInterval.")
              lastCheckTimestamp = timeNow()
            }
          case Left(err) => {
            logger.error(err.getMessage)
            lastCheckTimestamp = 0
          }
        }
      }
      Thread.sleep(checkIntervalLoopInMillis)
    }
  }.toEither match {
    case Right(_) => logger.info(s"run finished")
    case Left(err) => logger.error(s"poller terminated due to ${err.getMessage}")
  }

  def poke(): Unit = {
    lastCheckTimestamp = 0
  }

  def stop(): Unit = whileLoop = false

  private def timeNow() = System.currentTimeMillis()
}
