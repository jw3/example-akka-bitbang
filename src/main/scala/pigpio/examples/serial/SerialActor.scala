package pigpio.examples.serial

import akka.actor.{Actor, ActorSystem, Props}
import akka.stream.scaladsl.Flow
import pigpio.scaladsl._

import scala.collection.mutable.ListBuffer


object SerialActor {
  val BIT_T = 100

  def apply()(implicit sys: ActorSystem) = {
    sys.actorOf(Props(new SerialActor()))
  }

  /**
   * translates from GpioAlerts to serial bits
   * eliminates the need for the SerialActor to worry about timing to allow it to focus on bits alone
   *
   * - the level is flipped to translate from the GpioAlert representation of
   * `changed-to` to the state that is interesting to us which is `changed-from`
   */
  def bitify = Flow[GpioAlert].statefulMapConcat {
    var previous = 0L

    () => { a =>
      val diff = minimize(a.level, a.tick - previous)
      val ticks = diff / SerialActor.BIT_T
      previous = a.tick

      var bits = ListBuffer[Level]()
      for (t <- 0L until ticks) {
        bits += Level.flip(a.level)
      }

      bits.toList
    }
  }

  /**
   * shorten the string of events as much as possible when initialing the stream
   * to eliminate creating XXX,XXX,XXX,XXX number of events to start out
   */
  def minimize(l: Level, ticks: Long) = l match {
    case High => Math.min(ticks, 10 * BIT_T)
    case Low => Math.min(ticks, 9 * BIT_T)
  }
}

/**
 * Protocol
 * - 2x stop bits
 * - pull low
 * - start bit
 * - send bit
 * - wait
 * - repeat send bit for each char
 * - set high
 */
class SerialActor extends Actor {

  def stopBits(count: Int): Receive = {
    case High =>
      if (count == 19) context.become(startBit)
      else context.become(stopBits(count + 1))

    case Low =>
      context.become(stopBits(0))
  }

  def startBit: Receive = {
    case Low =>
      context.become(read(0, 0))

    case High =>
      context.become(stopBits(1))

    case _ => reset()
  }

  def read(char: Char, count: Int): Receive = {
    case bit: Level =>
      if (count < 8) {
        println(s"\t${bit.value}")
        val char2 = ((char << 1) | bit.value).toChar
        context.become(read(char2, count + 1))
      }
      else {
        emit(char)
        context.become(stopBits(bit.value))
      }

    case _ => reset()
  }

  def emit(char: Char) = println(char)

  def reset() = context.become(stopBits(0))

  def receive: Receive = stopBits(0)
}
