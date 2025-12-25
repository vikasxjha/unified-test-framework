@api @db @e2e @billing
Feature: Billing and Subscription API with Database Validation

  As a QA Engineer
  I want to validate that billing transactions are correctly persisted
  So that payment processing and subscription state is reliable

  Background:
    Given the database is accessible
    And I have a registered user with email "billing.user@example.com"

  @smoke @positive
  Scenario: Create checkout creates transaction record
    When I create checkout for plan "PREMIUM_MONTHLY"
    Then the API response status should be 201
    And the response should contain field "checkoutId"
    And the response should contain field "amount"
    And the database should contain transaction for checkout ID
    And the transaction status should be "PENDING"
    And the transaction amount should match API response

  @positive
  Scenario: Payment success activates subscription in database
    Given I have a pending checkout with ID "CHK-123456"
    When I process payment successfully for checkout "CHK-123456"
    Then the API response status should be 200
    And the transaction status in database should be "COMPLETED"
    And the subscription status in database should be "ACTIVE"
    And the subscription start date should be today
    And the subscription end date should be 30 days from now

  @negative
  Scenario: Payment failure rolls back subscription
    Given I have a pending checkout with ID "CHK-789012"
    When I process payment with failure for checkout "CHK-789012"
    Then the API response status should be 402
    And the transaction status in database should be "FAILED"
    And no active subscription should exist in database

  @positive
  Scenario: Subscription expiry updates database status
    Given I have an active subscription that expires today
    When the subscription expiry job runs
    Then the subscription status in database should be "EXPIRED"
    And the expiry timestamp should be set
    And the user should have no active subscription

  @positive
  Scenario: Subscription upgrade closes old subscription
    Given I have an active "PREMIUM_MONTHLY" subscription
    When I upgrade to plan "PREMIUM_ANNUAL"
    Then the API response status should be 200
    And the old subscription status should be "CLOSED"
    And the new subscription status should be "ACTIVE"
    And the closure reason should be "UPGRADED"

  @negative @idempotency
  Scenario: Duplicate payment processing is idempotent
    Given I have a completed transaction "TXN-111222"
    When I attempt to process payment for "TXN-111222" again
    Then the API response status should be 200
    And the transaction status should remain "COMPLETED"
    And no duplicate subscription should be created

  @positive
  Scenario: Partial refund creates refund record
    Given I have a completed payment of $99.99
    When I process partial refund of $49.99
    Then the API response status should be 200
    And the refund record should exist in database
    And the refund amount should be $49.99
    And the original transaction should be marked "PARTIALLY_REFUNDED"

  @negative @concurrent
  Scenario: Concurrent subscription purchases prevent duplicates
    When I attempt 3 concurrent subscription purchases
    Then only 1 purchase should succeed
    And only 1 active subscription should exist in database

  @boundary
  Scenario: Zero-amount transaction handling
    When I create checkout with amount 0
    Then the API response status should be 400
    And no transaction should be created in database

  @positive
  Scenario: Subscription renewal extends end date
    Given I have an active subscription ending in 3 days
    When I renew subscription via API
    Then the API response status should be 200
    And the subscription end date should extend by 30 days
    And a new transaction should be recorded

