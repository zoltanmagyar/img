import akka.actor.typed.ActorRef

sealed trait Request

final case class GetLastEvent(
    replyTo: ActorRef[Single]
) extends Request

final case class GetLastNEvents(
    n: Int,
    replyTo: ActorRef[Multi]
) extends Request

final case class GetAllEvents(
    replyTo: ActorRef[Multi]
) extends Request

final case class Update(
    event: Event,
    replyTo: ActorRef[Single]
) extends Request
