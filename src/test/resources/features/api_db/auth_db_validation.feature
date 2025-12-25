@api @db @e2e @auth
Feature: Auth API with Database Validation

  As a QA Engineer
  I want to validate that auth APIs correctly persist data to the database
  So that user authentication state is reliable

  Background:
    Given the database is accessible
    And I clean up any existing test user data

  @smoke @positive
  Scenario: User creation via API creates record in database
    When I create a new user via API with:
      | email    | test.user@example.com |
      | phone    | +14155551234          |
      | name     | Test User             |
      | country  | US                    |
    Then the API response status should be 201
    And the response should contain field "userId"
    And the database should contain user with email "test.user@example.com"
    And the user status in database should be "PENDING"
    And the user phone in database should be "+14155551234"

  @positive
  Scenario: User login generates auth token in database
    Given a registered user exists with email "login.test@example.com"
    When I send login request for user "login.test@example.com"
    Then the API response status should be 200
    And the response should contain field "accessToken"
    And the database should contain auth token for user "login.test@example.com"
    And the token expiry should be set in the future

  @negative
  Scenario: Duplicate user creation does not modify database
    Given a registered user exists with email "duplicate@example.com"
    When I attempt to create user with email "duplicate@example.com"
    Then the API response status should be 409
    And the error message should contain "already exists"
    And the user count in database for "duplicate@example.com" should be 1

  @negative
  Scenario: Invalid credentials do not create database records
    When I send login request with invalid credentials:
      | email    | nonexistent@example.com |
      | password | wrongpassword           |
    Then the API response status should be 401
    And no auth token should exist in database for "nonexistent@example.com"

  @positive
  Scenario: User status change is persisted correctly
    Given a registered user exists with email "status.test@example.com"
    And the user status in database is "ACTIVE"
    When I update user status to "BLOCKED" via API
    Then the API response status should be 200
    And the user status in database should be "BLOCKED"
    And the status change timestamp should be recent

  @negative @boundary
  Scenario: User creation with missing required fields
    When I create a user with missing required field "email"
    Then the API response status should be 400
    And no user record should be created in database

  @idempotency
  Scenario: Idempotent user creation with correlation ID
    Given I have a unique correlation ID
    When I create user "idempotent@example.com" with correlation ID
    And I create user "idempotent@example.com" with same correlation ID
    Then both API calls should return status 201
    And only 1 user record should exist in database for "idempotent@example.com"

