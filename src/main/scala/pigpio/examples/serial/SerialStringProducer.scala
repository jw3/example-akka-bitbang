package pigpio.examples.serial

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import pigpio.scaladsl.GpioAlert

case object Start
case object Stop
case object Loop

/**
 * Protocol
 * - 2x high stop bits
 * - pull low
 * - start bit
 * - send bit
 * - wait
 * - repeat send bit for each char
 * - set high
 *
 */
class SerialStringProducer(str: String) extends Actor {
  import SerialActor.BIT_T
  var ticks = 0

  def started(ref: ActorRef, looping: Boolean): Receive = {
    str.foreach(txchar(_, ref))

    val e = if (looping) Start else Stop
    self ! e

    {
      case Stop => context.become(stopped)
      case Start => context.become(started(ref, looping))
    }
  }

  def stopped: Receive = {
    ticks = 0

    {
      case Start => context.become(started(sender(), false))
      case Loop => context.become(started(sender(), true))
    }
  }

  def receive: Receive = stopped


  def txchar(ch: Char, ref: ActorRef) {
    tx(1, BIT_T * 2, ref)
    tx(0, BIT_T, ref)

    for (i <- 0 to 7) {
      tx(bit((ch.toInt << i) & 0x80), BIT_T, ref)
    }
  }

  def tx(v: Int, t: Int, ref: ActorRef) = {
    //tickby(t)
    //println(s"GpioAlert(1, $v, $ticks)  [t+$t]")
    ref ! GpioAlert(1, v, tickby(t))
  }

  def tickby(t: Int) = {
    ticks += t
    ticks
  }

  def bit(int: Int) = int match {
    case 0 => 0
    case _ => 1
  }
}


object SerialStringProducer {
  def apply(str: String)(implicit sys: ActorSystem) = {
    sys.actorOf(Props(new SerialStringProducer(str)))
  }
}
