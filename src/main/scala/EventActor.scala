import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors

object EventActor {
  def apply(): Behavior[Request] = apply(List.empty)

  def apply(events: List[Event]): Behavior[Request] = Behaviors.receiveMessage {
    case GetLastEvent(replyTo) =>
      replyTo ! Single(events.headOption)
      Behaviors.same
    case GetLastNEvents(n, replyTo) =>
      replyTo ! Multi(events.take(n))
      Behaviors.same
    case GetAllEvents(replyTo) =>
      replyTo ! Multi(events)
      Behaviors.same
    case Update(event, replyTo) =>
      val currentState = events.headOption.getOrElse(Event.zero)
      val accept =
        event.elapsedSeconds > currentState.elapsedSeconds && (
          (event.scorer == 0 && event.team1Total == currentState.team1Total + event.pointsScored) ||
          (event.scorer == 1 && event.team2Total == currentState.team2Total + event.pointsScored)
        )
      if (accept) {
        replyTo ! Single(Some(event))
        apply(event :: events)
      } else {
        // invalid data received
        replyTo ! Single(None)
        Behaviors.same
      }
  }
}
