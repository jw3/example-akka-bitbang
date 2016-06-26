package pigpio.examples.serial

import akka.Done
import akka.actor.ActorSystem
import akka.stream._
import akka.stream.scaladsl.{Sink, Source}
import pigpio.examples.serial.SerialActor.bitify
import pigpio.scaladsl.GpioAlert

import scala.concurrent.Await
import scala.concurrent.duration.Duration

// use a mock bitbang stream
object MockDriver extends App {
  implicit val sys = ActorSystem()
  implicit val mat = ActorMaterializer()

  val consumer = SerialActor()
  val producer = SerialStringProducer("hello akka!")

  val source = Source.actorRef[GpioAlert](100, OverflowStrategy.fail)
               .via(bitify)
               .to(Sink.actorRef(consumer, Done))
               .run()

  // connect the producer to the stream source
  producer.tell(Loop, source)

  Await.ready(sys.whenTerminated, Duration.Inf)
}
