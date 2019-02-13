# Nassau Utilities

Nassau Utilities contains utility methods for working with NASDAQ transport
protocols.

## Features

Nassau Utilities contains the following utility methods:

- `SoupBinTCP.receive()` for receiving all messages in a SoupBinTCP session
- `MoldUDP64.receive()` for receiving all messages in a MoldUDP64 session
- `BinaryFILE.read()` for reading all messages in a BinaryFILE file

## Dependencies

Nassau Utilities depends on the following libraries:

- Nassau Core

## Download

Add a Maven dependency to Nassau Utilities:

```xml
<dependency>
  <groupId>com.paritytrading.nassau</groupId>
  <artifactId>nassau-util</artifactId>
  <version><!-- latest release --></version>
</dependency>
```

See the [latest release][] on GitHub.

  [latest release]: https://github.com/paritytrading/nassau/releases/latest

## License

Released under Apache License, Version 2.0.
