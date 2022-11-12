package eventdriven.core.outboxpoller.impl

import com.sun.org.slf4j.internal.LoggerFactory
import eventdriven.core.infrastructure.messaging.{EventEnvelope, EventPublisher}
import eventdriven.core.outboxpoller.OutboxPublisher

class OutboxPublisherImpl[E](publisher: EventPublisher[EventEnvelope[E]]) extends OutboxPublisher[EventEnvelope[E]] {
  private val logger = LoggerFactory.getLogger(this.getClass)

  def publish(event: EventEnvelope[E]): Either[Throwable, Unit] = {
    logger.warn(s"Publishing event $event")
    publisher.publish(event.id, event, event.eventType)
  }
}
