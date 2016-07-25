package pigpio.examples.serial

import akka.Done
import akka.actor.ActorSystem
import akka.stream.scaladsl.{Sink, Source}
import akka.stream.{ActorMaterializer, OverflowStrategy}
import akka.testkit.{ImplicitSender, TestKit}
import org.scalatest.{Matchers, WordSpecLike}
import pigpio.scaladsl.GpioAlert


class StreamSpec extends TestKit(ActorSystem()) with WordSpecLike with ImplicitSender with Matchers {
  implicit val mat = ActorMaterializer()

  val producer = SerialStringProducer("hello akka!")
  val source = Source.actorRef[GpioAlert](100, OverflowStrategy.fail)
               .via(SerialActor.bitify)
               .to(Sink.actorRef(testActor, Done))
               .run()


  "this" should {
    "work" in {
      producer.tell(Loop, source)
    }
  }
}
