import java.nio.file.Paths

import akka.actor.typed.{ActorRef, Scheduler}
import akka.actor.typed.scaladsl.AskPattern._
import akka.util.Timeout
import cats.data.Validated
import cats.data.Validated.{Invalid, Valid}
import cats.effect.{Blocker, ContextShift, IO}
import cats.implicits._
import fs2._

import scala.concurrent.duration._
import scala.util.{Failure, Success, Try}

class StreamProcessor(private val eventActor: ActorRef[Request]) {
  implicit val timeout: Timeout = 5.seconds

  /**
   * Produces an FS2 Stream of the validated events for any downstream consumers.
   *
   * Data is read from a file but this method can be changed to consume data from a TCP socket or some other source.
   */
  def processFile(
      fileName: String
  )(
      implicit cs: ContextShift[IO],
      scheduler: Scheduler
  ): Stream[IO, Event] = Stream.resource(Blocker[IO]).flatMap {
    blocker =>
      io.file.readAll[IO](Paths.get(fileName), blocker, 4096)
        .through(text.utf8Decode)
        .through(text.lines)
        .filter(_.nonEmpty)
        .through(StreamProcessor.eventParser)
        .evalMap {
          case Valid(event) =>
            IO.fromFuture(IO(eventActor.ask[Single](ref => Update(event, ref)))).map(_.event)
          case Invalid(e) =>
            IO(println(e)) >> IO.pure(None)
        }
        .collect { case Some(event) => event }
  }
}

object StreamProcessor {
  /**
   * FS2 Pipe to parse the raw data into an Event data structure.
   */
  val eventParser: Pipe[IO, String, Validated[String, Event]] =
    _.map { hex =>
      Try(Integer.parseInt(hex.drop(2), 16)) match {
        case Failure(e) =>
          s"failed to parse '$hex': ${e.getMessage}".invalid
        case Success(int) =>
          def binaryToDecimal(binary: String): Validated[String, Int] =
            Try(Integer.parseInt(binary, 2)) match {
              case Failure(e) =>
                s"failed to parse '$binary': ${e.getMessage}".invalid
              case Success(int) =>
                int.valid
            }
          val bits = int.toBinaryString.reverse.padTo(32, '0')
          val pointsScored = binaryToDecimal(bits.slice(0, 2).reverse)
          val scorer = binaryToDecimal(bits.slice(2, 3).reverse)
          val team2Total = binaryToDecimal(bits.slice(3, 11).reverse)
          val team1Total = binaryToDecimal(bits.slice(11, 19).reverse)
          val elapsedSeconds = binaryToDecimal(bits.slice(19, 31).reverse)
          (pointsScored, scorer, team2Total, team1Total, elapsedSeconds).mapN(Event.apply)
      }
    }
}
