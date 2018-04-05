Feature: Test Bitfinex

  Background:
    Given system started and connected to Bitfinex

  Scenario: Subscribe to raw order book
    When system received the Info message:
      | version | 1.1 |
    Then the Subscribe to Channel message for BTCUSD should be sent out

  Scenario: Parsing Snapshot
    When system received the Subscribed message:
      | chanId | 1234   |
      | prec   | R0     |
      | freq   | F0     |
      | len    | 25     |
      | pair   | BTCUSD |
    And system received the Snapshot message for channel 1234:
      | OrderId    | Price          | Amount      |
      | 8520947032 | 11417          | 0.5461123   |
      | 8521019041 | 11416          | 0.48303707  |
      | 8521019090 | 11416          | 0.5         |
      | 8521018698 | 11415          | 3.20257256  |
      | 8521019761 | 11418.98512422 | -0.75       |
      | 8521011724 | 11419          | -0.6641     |
      | 8521020379 | 11419.3566868  | -0.8742     |
      | 8520977065 | 11420          | -0.38256056 |
    Then the orderbook for channel 1234 should be as:
      | Level | Price          | Quantity   |
      | ASK_4 | 11420          | 0.38256056 |
      | ASK_3 | 11419.3566868  | 0.8742     |
      | ASK_2 | 11419          | 0.6641     |
      | ASK_1 | 11418.98512422 | 0.75       |
      | BID_1 | 11417          | 0.5461123  |
      | BID_2 | 11416          | 0.98303707 |
      | BID_3 | 11415          | 3.20257256 |