@smoke
@critical
Feature: Smoke Tests – Core Platform Health

  Smoke tests validate that the most critical user journeys
  and backend services are up and functioning.
  These tests must:
  - Run fast
  - Be stable
  - Fail only on real production issues

  Background:
    Given I am running in "qa" environment
    And checkpoint "Smoke test execution started"

  # ---------------------------------------------------------
  # WEB – CORE LANDING & LOGIN
  # ---------------------------------------------------------

  @web
  Scenario: Web home page loads successfully
    Given I open the web application
    Then the home page should load successfully
    And the page title should contain "Truecaller"
    And checkpoint "Web home page loaded"

  @web
  Scenario: Web login page is accessible
    Given I open the login page
    Then the login page should be displayed
    And checkpoint "Web login page accessible"

  # ---------------------------------------------------------
  # API – CORE SERVICE HEALTH
  # ---------------------------------------------------------

  @api
  Scenario: Auth API is healthy
    Given the Auth API is available
    When I send a health check request to Auth API
    Then the API response status should be 200
    And checkpoint "Auth API healthy"

  @api
  Scenario: Search API responds successfully
    Given the Search API is available
    When I search for phone number "9999999999"
    Then the API response status should be 200
    And checkpoint "Search API healthy"

  # ---------------------------------------------------------
  # EVENT / KAFKA – CRITICAL EVENTS
  # ---------------------------------------------------------

  @events
  Scenario: Lookup event is published successfully
    Given event consumption is enabled
    When a phone lookup is performed via API
    Then a "LOOKUP_EVENT" should be published
    And checkpoint "Lookup event validated"

  # ---------------------------------------------------------
  # SECURITY – BASIC GATES
  # ---------------------------------------------------------

  @security
  Scenario: Security headers are present on web response
    Given I open the web application
    Then the response should contain mandatory security headers
    And checkpoint "Security headers validated"

  # ---------------------------------------------------------
  # ACCESSIBILITY – BASIC CHECK
  # ---------------------------------------------------------

  @a11y
  Scenario: Home page has no critical accessibility violations
    Given I open the web application
    Then the home page should have no critical accessibility violations
    And checkpoint "Accessibility baseline passed"

  # ---------------------------------------------------------
  # PERFORMANCE – LIGHTWEIGHT SLA
  # ---------------------------------------------------------

  @performance
  Scenario: Search API responds within acceptable SLA
    Given the Search API is available
    When I search for phone number "8888888888"
    Then the response time should be under 300 milliseconds
    And checkpoint "Search API performance validated"

  # ---------------------------------------------------------
  # MOBILE – APP LAUNCH
  # ---------------------------------------------------------

  @mobile
  Scenario: Mobile app launches successfully
    Given the mobile application is installed
    When I launch the mobile application
    Then the home screen should be displayed
    And checkpoint "Mobile app launch validated"
