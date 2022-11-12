package eventdriven.core.outboxpoller

trait OutboxPoller {
  def run(): Unit
  def poke(): Unit
}
