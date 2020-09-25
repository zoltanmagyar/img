import java.nio.file.Paths

import akka.actor.typed.{ActorRef, Scheduler}
import akka.actor.typed.scaladsl.AskPattern._
import akka.util.Timeout
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
          case Success(event) =>
            IO.fromFuture(IO(eventActor.ask[Single](ref => Update(event, ref)))).map(_.event)
          case Failure(e) =>
            IO(println(e.getMessage)) >> IO.pure(None)
        }
        .collect { case Some(event) => event }
  }
}

object StreamProcessor {
  /**
   * FS2 Pipe to parse the raw data into an Event data structure.
   */
  val eventParser: Pipe[IO, String, Try[Event]] =
    _.map { hex =>
      val trimmed = if (hex.startsWith("0x")) hex.drop(2) else hex
      Try(Integer.parseInt(trimmed, 16)).map { binary =>
        val pointsScored = binary & 3
        val scorer = (binary >> 2) & 1
        val team2Total = (binary >> 3) & 255
        val team1Total = (binary >> 11) & 255
        val elapsedSeconds = (binary >> 19) & 4095
        Event(pointsScored, scorer, team2Total, team1Total, elapsedSeconds)
      }
    }
}
