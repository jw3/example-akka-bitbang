package pigpio.examples.serial

import akka.actor.ActorSystem

import scala.concurrent.Await
import scala.concurrent.duration.Duration


object foo2 extends App {
  implicit val sys = ActorSystem()

  val str = "he"
  str.foreach { ch =>
    println(s"$ch\t${ch.toInt.toBinaryString.reverse.padTo(8, '0').reverse}")
  }

  val p = SerialStringProducer(str)
  p ! Start

  Await.ready(sys.whenTerminated, Duration.Inf)
}
