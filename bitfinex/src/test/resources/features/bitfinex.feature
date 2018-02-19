Feature: Test Bitfinex

  Background:
    Given Open http://en.wikipedia.org
    And Do login

  Scenario: direct search article
    Given a web browser is on the Google page
    When the search phrase "test" is entered
    And User enters Credentials to LogIn
      | name       | value |
      | testuser_1 | 123   |
      | testuser_2 | 54    |
    Then results for "test" are shown