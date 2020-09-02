import fs2.Stream
import org.scalatest.OptionValues
import org.scalatest.funsuite.AnyFunSuite

class EventParserTest extends AnyFunSuite with OptionValues {
  private val parser = StreamProcessor.eventParser

  test("parser rejects an invalid hex string") {
    val hexStream = Stream.emit("0xxyz")

    val event = hexStream.through(parser).compile.toList.unsafeRunSync().head

    assert(event.isInvalid)
  }

  test("parser accepts a valid hex string") {
    val hexStream = Stream.emits(List("0x781002", "0xf0101f"))

    val events = hexStream.through(parser).compile.toList.unsafeRunSync()

    assert(events.size == 2)
    assert(events.forall(_.isValid))
    assert(events.head.toOption.value == Event(pointsScored = 2, scorer = 0, team1Total = 2, team2Total = 0, elapsedSeconds = 15))
    assert(events(1).toOption.value == Event(pointsScored = 3, scorer = 1, team1Total = 2, team2Total = 3, elapsedSeconds = 30))
  }
}
