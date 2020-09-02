import akka.actor.typed.{ActorRef, Scheduler}
import akka.actor.typed.scaladsl.AskPattern._
import akka.util.Timeout
import cats.effect.{ContextShift, IO}

import scala.concurrent.duration._

/**
 * Exposes methods to query the current state.
 */
sealed trait Queries {
  def getLastEvent: IO[Option[Event]]
  def getLastNEvents(n: Int): IO[List[Event]]
  def getAllEvents: IO[List[Event]]
}

object Queries {
  implicit val timeout: Timeout = 5.seconds
  def apply(eventActor: ActorRef[Request])(implicit cs: ContextShift[IO], scheduler: Scheduler): Queries = new Queries {
    override def getLastEvent: IO[Option[Event]] =
      IO.fromFuture(IO(eventActor.ask[Single](ref => GetLastEvent(ref)))).map(_.event)
    override def getLastNEvents(n: Int): IO[List[Event]] =
      IO.fromFuture(IO(eventActor.ask[Multi](ref => GetLastNEvents(n, ref)))).map(_.events)
    override def getAllEvents: IO[List[Event]] =
      IO.fromFuture(IO(eventActor.ask[Multi](ref => GetAllEvents(ref)))).map(_.events)
  }
}
