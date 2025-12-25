@api @db @e2e @lookup
Feature: Phone Lookup API with Database Validation

  As a QA Engineer
  I want to validate that lookup APIs correctly record search history
  So that usage analytics and spam detection work properly

  Background:
    Given the database is accessible
    And I have a registered user with email "lookup.user@example.com"

  @smoke @positive
  Scenario: Phone lookup creates history record in database
    When I perform phone lookup for number "+14155552222"
    Then the API response status should be 200
    And the database should contain lookup history for number "+14155552222"
    And the lookup history should be linked to user "lookup.user@example.com"
    And the lookup timestamp should be recent

  @positive
  Scenario: Repeated lookup increments hit counter
    Given I have performed lookup for number "+14155553333"
    And the lookup count in database is 1
    When I perform lookup for number "+14155553333" again
    Then the API response status should be 200
    And the lookup count in database should be 2
    And the last lookup timestamp should be updated

  @negative
  Scenario: Invalid phone format does not create database record
    When I perform lookup for invalid number "invalid-phone"
    Then the API response status should be 400
    And no lookup history should exist for "invalid-phone"

  @positive
  Scenario: Spam-marked number sets correct database flag
    Given phone number "+14155554444" is marked as spam
    When I perform lookup for number "+14155554444"
    Then the API response status should be 200
    And the response should indicate spam status
    And the database should show spam flag TRUE for "+14155554444"

  @anonymous
  Scenario: Anonymous lookup has no user linkage in database
    When I perform anonymous lookup for number "+14155555555"
    Then the API response status should be 200
    And the database should contain lookup history for "+14155555555"
    And the lookup history user_id should be NULL

  @concurrent
  Scenario: Concurrent lookups correctly increment counter
    Given phone number "+14155556666" has lookup count 0
    When I perform 5 concurrent lookups for "+14155556666"
    Then all API responses should be successful
    And the lookup count in database should be 5

  @rate-limit
  Scenario: Rate limit updates database counter
    When I perform 100 lookups within 1 minute
    Then the last lookup should be rate-limited
    And the rate limit counter in database should reflect attempts

  @positive
  Scenario: Lookup with enriched data persists all fields
    When I perform enriched lookup for "+14155557777"
    Then the API response should contain carrier information
    And the database should persist carrier data for "+14155557777"
    And the database should persist location data
    And the enrichment timestamp should be set

