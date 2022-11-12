package eventdriven.core.outboxpoller.impl

import com.sun.org.slf4j.internal.LoggerFactory
import eventdriven.core.infrastructure.messaging.EventEnvelopeMap
import eventdriven.core.outboxpoller.{OutboxEventStore, OutboxPoller, OutboxPublisher}

import scala.util.Try

class OutboxPollerImpl(store: OutboxEventStore[EventEnvelopeMap], publisher: OutboxPublisher[EventEnvelopeMap]) extends OutboxPoller {
  private val logger = LoggerFactory.getLogger(this.getClass)
  private val checkIntervalLoopInMillis = 10
  private val wakeUpInterval = 60 * 1000
  private val limit = 100
  private var lastCheckTimestamp: Long = 0

  def run(): Unit = Try {
    while (true) {
      if (timeNow() - lastCheckTimestamp > wakeUpInterval) {
        logger.warn("checking for events to publish.")
        (for {
          events <- store.getUnpublished(limit)
          _ = logger.warn(s"found ${events.size} events")
          published = events.map { x =>
            publisher.publish(x)
            store.deleteEvent(x)
          }
        } yield published) match {
          case Right(published) =>
            if (published.size == limit) {
              logger.warn("more events to publish, continuing...")
              lastCheckTimestamp = 0
            } else {
              logger.warn(s"finished processing, will go to sleep for $wakeUpInterval.")
              lastCheckTimestamp = timeNow()
            }
          case Left(err) => {
            logger.warn(err.getMessage)
            lastCheckTimestamp = 0
          }
        }
      }
      Thread.sleep(checkIntervalLoopInMillis)
    }
  }.toEither match {
    case Right(r) => logger.warn(s"run finished")
    case Left(err) => logger.warn(s"run errored out with ${err.getMessage}")
  }

  def poke(): Unit = {
    lastCheckTimestamp = 0
  }

  private def timeNow() = System.currentTimeMillis()
}
