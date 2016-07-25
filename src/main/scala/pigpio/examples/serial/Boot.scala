package pigpio.examples.serial

import akka.{Done, stream}
import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, OverflowStrategy, javadsl}
import akka.stream.scaladsl.{Sink, Source}
import com.typesafe.scalalogging.LazyLogging
import pigpio.scaladsl.GpioPin.Listen
import pigpio.scaladsl._

import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.util.{Failure, Success}


object Boot extends App with LazyLogging {
  implicit val system = ActorSystem()
  implicit val mat = ActorMaterializer()

  DefaultInitializer.gpioInitialise() match {
    case Success(Init(lib, ver)) =>
      logger.debug("initialized pigpio V{}", ver.toString)
      run(lib)

    case Failure(ex) =>
      logger.error("failed to initialize pigpio", ex)
      system.terminate()
  }

  Await.ready(system.whenTerminated, Duration.Inf)
  logger.debug("gpio terminating")
  DefaultInitializer.gpioTerminate()
  logger.debug("application terminating")

  def run(implicit lpigpio: PigpioLibrary) = {
    val consumer = SerialActor()
    val producer = GpioPin(UserGpio(1))

    val source = Source.actorRef[GpioAlert](1000, OverflowStrategy.fail)
                 .via(SerialActor.bitify)
                 .to(Sink.actorRef(consumer, Done))
                 .run()

    // connect the producer to the stream source
    producer.tell(Listen(), source)
    producer ! InputPin
  }
}
