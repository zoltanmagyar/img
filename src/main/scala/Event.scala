final case class Event(
    pointsScored: Int,
    scorer: Int,
    team2Total: Int,
    team1Total: Int,
    elapsedSeconds: Int
) {
  override def toString: String =
    s"Event(pointsScored=$pointsScored,scorer=$scorer,team1Total=$team1Total,team2Total=$team2Total,elapsedSeconds=$elapsedSeconds)"
}

object Event {
  val zero: Event = Event(0, 0, 0, 0, 0)
}
