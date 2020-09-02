import akka.actor.testkit.typed.scaladsl.{BehaviorTestKit, TestInbox}
import org.scalatest.funsuite.AnyFunSuite

class EventActorTest extends AnyFunSuite {
  test("all events are processed if the data stream is consistent") {
    val testkit = BehaviorTestKit(EventActor())
    val inboxSingle = TestInbox[Single]()
    val events = List(
      Event(pointsScored = 2, scorer = 0, team1Total = 2, team2Total = 0, elapsedSeconds = 16),
      Event(pointsScored = 2, scorer = 1, team1Total = 2, team2Total = 2, elapsedSeconds = 31),
      Event(pointsScored = 3, scorer = 1, team1Total = 2, team2Total = 5, elapsedSeconds = 59),
      Event(pointsScored = 2, scorer = 0, team1Total = 4, team2Total = 5, elapsedSeconds = 73),
      Event(pointsScored = 2, scorer = 1, team1Total = 4, team2Total = 7, elapsedSeconds = 92),
      Event(pointsScored = 2, scorer = 1, team1Total = 4, team2Total = 9, elapsedSeconds = 105),
      Event(pointsScored = 3, scorer = 0, team1Total = 7, team2Total = 9, elapsedSeconds = 119),
      Event(pointsScored = 2, scorer = 1, team1Total = 7, team2Total = 11, elapsedSeconds = 141),
      Event(pointsScored = 1, scorer = 0, team1Total = 8, team2Total = 11, elapsedSeconds = 166),
      Event(pointsScored = 2, scorer = 1, team1Total = 8, team2Total = 13, elapsedSeconds = 200),
      Event(pointsScored = 2, scorer = 0, team1Total = 10, team2Total = 13, elapsedSeconds = 219),
      Event(pointsScored = 2, scorer = 0, team1Total = 12, team2Total = 13, elapsedSeconds = 236),
      Event(pointsScored = 2, scorer = 1, team1Total = 12, team2Total = 15, elapsedSeconds = 263),
      Event(pointsScored = 2, scorer = 0, team1Total = 14, team2Total = 15, elapsedSeconds = 285),
      Event(pointsScored = 2, scorer = 1, team1Total = 14, team2Total = 17, elapsedSeconds = 294),
      Event(pointsScored = 2, scorer = 1, team1Total = 14, team2Total = 19, elapsedSeconds = 318),
      Event(pointsScored = 1, scorer = 1, team1Total = 14, team2Total = 20, elapsedSeconds = 346),
      Event(pointsScored = 2, scorer = 0, team1Total = 16, team2Total = 20, elapsedSeconds = 367),
      Event(pointsScored = 2, scorer = 1, team1Total = 16, team2Total = 22, elapsedSeconds = 383),
      Event(pointsScored = 2, scorer = 1, team1Total = 16, team2Total = 24, elapsedSeconds = 402),
      Event(pointsScored = 2, scorer = 0, team1Total = 18, team2Total = 24, elapsedSeconds = 420),
      Event(pointsScored = 2, scorer = 1, team1Total = 18, team2Total = 26, elapsedSeconds = 442),
      Event(pointsScored = 3, scorer = 0, team1Total = 21, team2Total = 26, elapsedSeconds = 474),
      Event(pointsScored = 2, scorer = 1, team1Total = 21, team2Total = 28, elapsedSeconds = 499),
      Event(pointsScored = 2, scorer = 0, team1Total = 23, team2Total = 28, elapsedSeconds = 533),
      Event(pointsScored = 1, scorer = 1, team1Total = 23, team2Total = 29, elapsedSeconds = 559),
      Event(pointsScored = 2, scorer = 0, team1Total = 25, team2Total = 29, elapsedSeconds = 581),
      Event(pointsScored = 2, scorer = 0, team1Total = 27, team2Total = 29, elapsedSeconds = 598)
    )
    events.foreach(e => testkit.run(Update(e, inboxSingle.ref)))

    val inboxMulti = TestInbox[Multi]()
    testkit.run(GetAllEvents(inboxMulti.ref))

    val processedEvents = inboxMulti.receiveMessage().events

    assert(processedEvents == events.reverse)
  }

  test("invalid/inconsistent events are not processed") {
    val testkit = BehaviorTestKit(EventActor())
    val inboxSingle = TestInbox[Single]()
    val events = List(
      Event(pointsScored = 2, scorer = 0, team1Total = 2, team2Total = 0, elapsedSeconds = 15),
      Event(pointsScored = 2, scorer = 1, team1Total = 2, team2Total = 2, elapsedSeconds = 28),
      Event(pointsScored = 0, scorer = 1, team1Total = 2, team2Total = 2, elapsedSeconds = 33),
      Event(pointsScored = 3, scorer = 1, team1Total = 2, team2Total = 5, elapsedSeconds = 60),
      Event(pointsScored = 2, scorer = 0, team1Total = 4, team2Total = 5, elapsedSeconds = 75),
      Event(pointsScored = 2, scorer = 1, team1Total = 4, team2Total = 7, elapsedSeconds = 97),
      Event(pointsScored = 2, scorer = 1, team1Total = 4, team2Total = 9, elapsedSeconds = 113),
      Event(pointsScored = 2, scorer = 1, team1Total = 4, team2Total = 9, elapsedSeconds = 113),
      Event(pointsScored = 3, scorer = 0, team1Total = 7, team2Total = 9, elapsedSeconds = 122),
      Event(pointsScored = 2, scorer = 1, team1Total = 7, team2Total = 11, elapsedSeconds = 143),
      Event(pointsScored = 2, scorer = 1, team1Total = 8, team2Total = 13, elapsedSeconds = 195),
      Event(pointsScored = 1, scorer = 0, team1Total = 8, team2Total = 11, elapsedSeconds = 168),
      Event(pointsScored = 2, scorer = 0, team1Total = 10, team2Total = 13, elapsedSeconds = 215),
      Event(pointsScored = 0, scorer = 1, team1Total = 14, team2Total = 13, elapsedSeconds = 234),
      Event(pointsScored = 2, scorer = 1, team1Total = 10, team2Total = 15, elapsedSeconds = 251),
      Event(pointsScored = 2, scorer = 1, team1Total = 12, team2Total = 17, elapsedSeconds = 295),
      Event(pointsScored = 2, scorer = 0, team1Total = 12, team2Total = 15, elapsedSeconds = 279),
      Event(pointsScored = 2, scorer = 1, team1Total = 12, team2Total = 19, elapsedSeconds = 322),
      Event(pointsScored = 2, scorer = 0, team1Total = 14, team2Total = 20, elapsedSeconds = 369),
      Event(pointsScored = 2, scorer = 1, team1Total = 14, team2Total = 22, elapsedSeconds = 393),
      Event(pointsScored = 2, scorer = 1, team1Total = 14, team2Total = 24, elapsedSeconds = 408),
      Event(pointsScored = 3, scorer = 0, team1Total = 232, team2Total = 234, elapsedSeconds = 1500),
      Event(pointsScored = 2, scorer = 0, team1Total = 16, team2Total = 24, elapsedSeconds = 426),
      Event(pointsScored = 2, scorer = 1, team1Total = 16, team2Total = 26, elapsedSeconds = 446),
      Event(pointsScored = 3, scorer = 0, team1Total = 19, team2Total = 26, elapsedSeconds = 456),
      Event(pointsScored = 2, scorer = 1, team1Total = 19, team2Total = 30, elapsedSeconds = 472),
      Event(pointsScored = 2, scorer = 0, team1Total = 21, team2Total = 28, elapsedSeconds = 505),
      Event(pointsScored = 1, scorer = 1, team1Total = 21, team2Total = 29, elapsedSeconds = 533),
      Event(pointsScored = 2, scorer = 0, team1Total = 23, team2Total = 29, elapsedSeconds = 560),
      Event(pointsScored = 2, scorer = 0, team1Total = 25, team2Total = 29, elapsedSeconds = 579)
    )
    events.foreach(e => testkit.run(Update(e, inboxSingle.ref)))

    val inboxMulti = TestInbox[Multi]()
    testkit.run(GetAllEvents(inboxMulti.ref))

    val processedEvents = inboxMulti.receiveMessage().events
    val invalidEvents = events.diff(processedEvents)

    assert(invalidEvents == List(
      Event(pointsScored = 2, scorer = 1, team1Total = 4, team2Total = 9, elapsedSeconds = 113),
      Event(pointsScored = 1, scorer = 0, team1Total = 8, team2Total = 11, elapsedSeconds = 168),
      Event(pointsScored = 2, scorer = 0, team1Total = 12, team2Total = 15, elapsedSeconds = 279),
      Event(pointsScored = 3, scorer = 0, team1Total = 232, team2Total = 234, elapsedSeconds = 1500),
      Event(pointsScored = 2, scorer = 1, team1Total = 19, team2Total = 30, elapsedSeconds = 472)
    ))
  }
}
