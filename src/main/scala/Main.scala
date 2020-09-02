import akka.actor.typed.ActorSystem
import cats.effect.{ExitCode, IO, IOApp}

/**
 * Entry point for the program. It accepts a file name as a single argument.
 */
object Main extends IOApp {
  override def run(args: List[String]): IO[ExitCode] =
    for {
      system <- IO(ActorSystem(EventActor(), "eventActor"))
      _ <- new StreamProcessor(system).processFile(args.head)(contextShift, system.scheduler).compile.drain
      _ <- IO(system.terminate())
    } yield {
      ExitCode.Success
    }
}
