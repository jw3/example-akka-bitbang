package pigpio.examples.serial

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestKit}
import org.scalatest.{Matchers, WordSpecLike}
import pigpio.scaladsl.{High, Level, Levels, Low}


class SerialActorSpec extends TestKit(ActorSystem()) with WordSpecLike with ImplicitSender with Matchers {
  val startseq = List(High, High, Low)
  val teststring = "hello, world" // UUID.randomUUID.toString


//  val bits = teststring.foldLeft(List[Level])((b, ch) => b += ch.toInt.toBinaryString.map {
//    case "0" => Low
//    case _ => High
//  }).mkString


  //"sending in correct bits should work"

 // "sending incorrect bits should not"


}
