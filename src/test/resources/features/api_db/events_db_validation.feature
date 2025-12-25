@api @db @e2e @events
Feature: Event and Notification API with Database Validation

  As a QA Engineer
  I want to validate that events are correctly recorded in database
  So that audit trail and event replay work properly

  Background:
    Given the database is accessible
    And I have a registered user with email "event.user@example.com"

  @smoke @positive
  Scenario: API action triggers event record in database
    When I perform user profile update
    Then the API response status should be 200
    And the database should contain event record for action "USER_PROFILE_UPDATED"
    And the event payload should contain user ID
    And the event timestamp should be recent
    And the event status should be "PUBLISHED"

  @positive
  Scenario: Event deduplication prevents duplicates
    Given I have an event with correlation ID "CORR-123456"
    When I publish event with same correlation ID "CORR-123456"
    Then the API should acknowledge the event
    And only 1 event record should exist with correlation ID "CORR-123456"

  @positive
  Scenario: Failed event retry is recorded
    Given I have a failed event "EVT-789012"
    When the event retry job executes
    Then the retry count should increment in database
    And the retry timestamp should be updated
    And the event status should be "RETRY_PENDING"

  @positive
  Scenario: Correlation ID consistency across related events
    When I perform checkout process with correlation ID "ORDER-555"
    Then multiple events should be created
    And all events should have correlation ID "ORDER-555"
    And event sequence should be maintained

  @positive
  Scenario: Event payload compression is handled
    When I publish large event payload
    Then the API response status should be 201
    And the event should be stored compressed in database
    And the compression flag should be TRUE

  @negative
  Scenario: Invalid event schema is rejected
    When I publish event with invalid schema
    Then the API response status should be 400
    And no event record should be created in database

  @positive
  Scenario: Event expiration marks old events
    Given I have events older than 90 days
    When the event cleanup job runs
    Then old events should be marked "ARCHIVED"
    And archived events should not be queryable via API

  @concurrent
  Scenario: High-volume event ingestion
    When I publish 1000 events concurrently
    Then all events should be accepted
    And all 1000 events should exist in database
    And no events should be lost

  @positive
  Scenario: Event metadata is preserved
    When I publish event with metadata:
      | source      | mobile-app  |
      | version     | 2.5.1       |
      | environment | production  |
    Then the event metadata should be stored in database
    And all metadata fields should be queryable

  @positive
  Scenario: Dead letter queue for failed events
    Given I have a permanently failed event
    When max retry count is reached
    Then the event should move to dead letter queue
    And the DLQ timestamp should be set
    And the failure reason should be recorded

