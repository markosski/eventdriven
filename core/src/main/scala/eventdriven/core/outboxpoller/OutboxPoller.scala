package eventdriven.core.outboxpoller

trait OutboxPoller {
  def run(): Unit
  def stop(): Unit
  def poke(): Unit
}
