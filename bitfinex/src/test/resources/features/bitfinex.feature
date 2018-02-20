Feature: Test Bitfinex

  Background:
    Given System started and connected to Bitfinex

  Scenario: Subscribe to raw order book
    When System received the following Info message:
      | version | 1.1 |
    Then The Subscribe to Channel message for BTCUSD should be sent out