package pigpio.examples.serial

import akka.actor.ActorSystem

import scala.concurrent.Await
import scala.concurrent.duration.Duration

// use a mock bitbang stream
object MockDriver extends App {
  implicit val sys = ActorSystem()

  val consumer = SerialActor()
  val producer = SerialStringProducer("hello, world!")
  producer.tell(Loop, consumer)

  Await.ready(sys.whenTerminated, Duration.Inf)
}
