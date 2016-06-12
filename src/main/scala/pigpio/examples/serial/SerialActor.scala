package pigpio.examples.serial

import akka.actor.{Actor, ActorSystem, Props}
import pigpio.scaladsl.{GpioAlert, High, Low}


object SerialActor {
  val BIT_T = 93

  def apply()(implicit sys: ActorSystem) = {
    sys.actorOf(Props(new SerialActor()))
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
  import SerialActor.BIT_T

  def waitingOnTwoStopBits: Receive = {
    var last = 0L

    {
      case a: GpioAlert if a.level == High =>
        val diff = a.tick - last
        if (diff == BIT_T * 2) context.become(startBit(a.tick))
        else reset()

      case a: GpioAlert =>
        last = a.tick
    }
  }

  def startBit(tick: Long): Receive = {
    case a: GpioAlert if a.level == Low =>
      val diff = a.tick - tick
      if (diff == BIT_T) context.become(read(a.tick, 0, 0))

    case _ => reset()
  }

  def read(tick: Long, char: Char, count: Int): Receive = {
    case a: GpioAlert =>
      val diff = a.tick - tick
      if (diff == BIT_T) {
        println(s"\t${a.level.value}")
        val char2 = ((char << 1) | a.level.value).toChar
        context.become(read(a.tick, char2, count + 1))
      }
      else if (diff == BIT_T * 2) {
        emit(char)
        context.become(startBit(a.tick))
      }
      else reset()

    case _ => reset()
  }

  def emit(char: Char) = println(char)

  def reset() = context.become(waitingOnTwoStopBits)

  def receive: Receive = waitingOnTwoStopBits
}
