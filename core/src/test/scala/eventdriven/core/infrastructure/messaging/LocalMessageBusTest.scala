package eventdriven.core.infrastructure.messaging

import java.io.File
import java.net.URL
import java.nio.file.{FileSystems, Path, Paths}
import java.nio.file.StandardWatchEventKinds._
import scala.jdk.CollectionConverters._


class LocalMessageBusTest extends munit.FunSuite {
  test("project AccountState") {
    val dispatcherA = new LocalEventDispatcher[String]("topicA")
    val dispatcherB = new LocalEventDispatcher[String]("topicB")
    val listenerA = new LocalEventListener[String]("topicA", "")
    val listenerB = new LocalEventListener[String]("topicB", "")

    dispatcherA.publish("Hello from A dispatcher, message 1")
    dispatcherB.publish("Hello from B dispatcher, message 1")
    dispatcherA.publish("Hello from A dispatcher, message 2")
    dispatcherB.publish("Hello from B dispatcher, message 2")
    dispatcherA.publish("")
    dispatcherB.publish("")

    var continueA = true
    var continueB = true
    while(continueA && continueB) {
      listenerA.take match {
        case Some(s) => println(s)
        case None => continueA = false
      }

      listenerB.take match {
        case Some(s) => println(s)
        case None => continueB = false
      }
    }
  }

  test("test") {
    val watcher = FileSystems.getDefault().newWatchService()
    val dir = Paths.get(new URL("file:/tmp/").toURI)
    val key = dir.register(watcher,
      ENTRY_MODIFY)

    while(true) {
      val events = key.pollEvents()
      val file = events.asScala.map(x => x.context().asInstanceOf[Path])
        .filter(_.endsWith("temp.txt"))
        .toList

      if (file.nonEmpty) {
        println("changed")
      }
    }
  }
}
