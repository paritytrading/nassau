# Upgrading to Nassau 1.0.0

Nassau 1.0.0 contains major API changes. See below for details.

- `Clock` has been moved from the `com.paritytrading.nassau.time` package to
  the `com.paritytrading.nassau` package.

- `MoldUDP64Client#receive()` and `MoldUDP64Client#receiveResponse()` now
  return a Boolean value indicating whether data was received or not.

- Direct access to message fields in `SoupBinTCP.LoginRequest`,
  `SoupBinTCP.LoginAccepted`, and `SoupBinTCP.LoginRejected` has been
  replaced with getters and setters. Replace instances of direct
  access e.g. to the `username` field in `SoupBinTCP.LoginRequest` with the
  corresponding getters and setters, such as `getUsername(String)` and
  `setUsername(String)`.

- `SystemClock` does not exist anymore. Use the
  `System::currentTimeMillis` method reference instead.


