#!/usr/bin/env bash
set -e

#############################################
# Unified Test Framework – Test Runner
#############################################

# ---------- DEFAULTS ----------
ENV=${ENV:-QA}
TAGS=${TAGS:-"@smoke"}
THREADS=${THREADS:-4}
BROWSER=${BROWSER:-chromium}
HEADLESS=${HEADLESS:-true}
MOBILE=${MOBILE:-false}
REPORTS=${REPORTS:-true}

PROJECT_ROOT="$(cd "$(dirname "$0")/.." && pwd)"
TIMESTAMP=$(date +"%Y%m%d_%H%M%S")

echo "========================================"
echo "🚀 Unified Test Framework Runner"
echo "========================================"
echo "Environment  : $ENV"
echo "Tags         : $TAGS"
echo "Threads      : $THREADS"
echo "Browser      : $BROWSER"
echo "Headless     : $HEADLESS"
echo "Mobile Tests : $MOBILE"
echo "========================================"

cd "$PROJECT_ROOT"

# ---------- EXPORT JVM PROPS ----------
export MAVEN_OPTS="-Xms512m -Xmx2048m"

MVN_ARGS=(
  "-Denv=$ENV"
  "-Dbrowser=$BROWSER"
  "-Dheadless=$HEADLESS"
  "-Dthreads=$THREADS"
  "-Dcucumber.filter.tags=$TAGS"
  "-Dtimestamp=$TIMESTAMP"
)

# ---------- MOBILE CONTROL ----------
if [[ "$MOBILE" == "true" ]]; then
  echo "📱 Mobile tests ENABLED"
  MVN_ARGS+=("-Dmobile=true")
else
  echo "🖥 Mobile tests DISABLED"
  MVN_ARGS+=("-Dmobile=false")
fi

# ---------- EXECUTION ----------
echo "▶️ Running Maven tests..."
mvn clean test "${MVN_ARGS[@]}"

# ---------- REPORTING ----------
if [[ "$REPORTS" == "true" ]]; then
  echo "📊 Generating reports..."

  if command -v allure >/dev/null 2>&1; then
    allure generate target/allure-results \
      --clean \
      -o reports/allure-$TIMESTAMP
    echo "✅ Allure report generated: reports/allure-$TIMESTAMP"
  else
    echo "⚠️ Allure not installed. Skipping Allure report."
  fi
fi

echo "========================================"
echo "✅ Test execution completed successfully"
echo "========================================"
