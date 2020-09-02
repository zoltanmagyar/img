Solution
======
The program has been implemented in Scala using the `FS2` and `Akka Actors` libraries. The project can be built by running `sbt test`. The program can also be run from the command line. It takes a file name as a single argument.

Design
======
The `StreamProcessor` reads the data stream from an input file and parses the raw data into a data structure called `Event`. The parsed events are forwarded to the `EventActor` which maintains the current match state and determines if an incoming event is valid. If an event is valid the state is updated with the new information. After the actor responds the valid events are emitted downstream.
