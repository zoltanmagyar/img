sealed trait Response

final case class Single(
    event: Option[Event]
) extends Response

final case class Multi(
    events: List[Event]
) extends Response
