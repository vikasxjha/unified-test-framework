#!/usr/bin/env bash
set -e

#############################################
# Unified Test Framework – Test Generator
#############################################

if [[ $# -lt 2 ]]; then
  echo "Usage:"
  echo "  ./scripts/generate-test.sh <TestName> <type>"
  echo ""
  echo "Types:"
  echo "  e2e | api | mobile | perf | security | contract | admin"
  echo ""
  echo "Example:"
  echo "  ./scripts/generate-test.sh UserUpgradeToPremium e2e"
  exit 1
fi

TEST_NAME=$1
TYPE=$2

PROJECT_ROOT="$(cd "$(dirname "$0")/.." && pwd)"
JAVA_BASE="$PROJECT_ROOT/src/test/java/com/company/qa/unified"
FEATURE_BASE="$PROJECT_ROOT/src/test/java/features"

SNAKE_CASE=$(echo "$TEST_NAME" | sed 's/\([a-z]\)\([A-Z]\)/\1_\2/g' | tr '[:upper:]' '[:lower:]')
CLASS_NAME="${TEST_NAME}${TYPE^}Test"
FEATURE_FILE="${SNAKE_CASE}.feature"
TIMESTAMP=$(date +"%Y-%m-%d")

echo "========================================"
echo "🧪 Generating test scaffold"
echo "Test Name : $TEST_NAME"
echo "Type      : $TYPE"
echo "========================================"

# ---------- DIRECTORIES ----------
mkdir -p "$FEATURE_BASE"
mkdir -p "$JAVA_BASE/stepdefs"
mkdir -p "$JAVA_BASE/$TYPE"

# ---------- FEATURE FILE ----------
FEATURE_PATH="$FEATURE_BASE/$FEATURE_FILE"

if [[ ! -f "$FEATURE_PATH" ]]; then
cat <<EOF > "$FEATURE_PATH"
@$TYPE
@$TEST_NAME
Feature: $TEST_NAME

  As a user
  I want to validate $TEST_NAME
  So that the system behaves correctly

  Scenario: $TEST_NAME happy path
    Given the system is ready
    When the user performs $TEST_NAME
    Then the expected outcome should occur
EOF
  echo "📄 Created feature: $FEATURE_FILE"
else
  echo "⚠️ Feature already exists: $FEATURE_FILE"
fi

# ---------- STEP DEFINITIONS ----------
STEPDEF_PATH="$JAVA_BASE/stepdefs/${TEST_NAME}Steps.java"

if [[ ! -f "$STEPDEF_PATH" ]]; then
cat <<EOF > "$STEPDEF_PATH"
package com.company.qa.unified.stepdefs;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;

public class ${TEST_NAME}Steps {

    @Given("the system is ready")
    public void systemIsReady() {
        // TODO: Setup preconditions
    }

    @When("the user performs $TEST_NAME")
    public void userPerformsAction() {
        // TODO: Call APIs / Pages / Screens
    }

    @Then("the expected outcome should occur")
    public void verifyOutcome() {
        // TODO: Assertions
    }
}
EOF
  echo "🧩 Created stepdefs: ${TEST_NAME}Steps.java"
else
  echo "⚠️ StepDefs already exist"
fi

# ---------- TEST CLASS ----------
TEST_PATH="$JAVA_BASE/$TYPE/$CLASS_NAME.java"

if [[ ! -f "$TEST_PATH" ]]; then
cat <<EOF > "$TEST_PATH"
package com.company.qa.unified.$TYPE;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("$TYPE")
@Tag("$TEST_NAME")
public class $CLASS_NAME {

    @Test
    void ${TEST_NAME}Flow() {
        // Execution controlled via Cucumber
    }
}
EOF
  echo "🧪 Created test class: $CLASS_NAME"
else
  echo "⚠️ Test class already exists"
fi

# ---------- OPTIONAL SUGGESTIONS ----------
echo ""
echo "✨ Optional next steps:"
echo " - Link APIs from: src/main/java/.../pages/api"
echo " - Add mobile screens if type=mobile"
echo " - Add Kafka/event validation if applicable"
echo " - Add metrics/assertions"

echo "========================================"
echo "✅ Test scaffold generated successfully"
echo "========================================"
