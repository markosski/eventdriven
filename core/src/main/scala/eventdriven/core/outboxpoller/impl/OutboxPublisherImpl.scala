package eventdriven.core.outboxpoller.impl

import com.sun.org.slf4j.internal.LoggerFactory
import eventdriven.core.infrastructure.messaging.{EventEnvelope, EventEnvelopeMap, EventPublisher}
import eventdriven.core.outboxpoller.OutboxPublisher

class OutboxPublisherImpl(publisher: EventPublisher[EventEnvelopeMap]) extends OutboxPublisher[EventEnvelopeMap] {
  private val logger = LoggerFactory.getLogger(this.getClass)

  def publish(event: EventEnvelopeMap): Either[Throwable, Unit] = {
    logger.warn(s"Publishing event $event")
    publisher.publish(event.id, event, event.eventType)
  }
}
