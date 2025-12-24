#!/usr/bin/env bash
set -e

#############################################
# Unified Test Framework – Pre-Commit Hook
#############################################

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

PROJECT_ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$PROJECT_ROOT"

echo "========================================"
echo "🔍 Running pre-commit checks"
echo "========================================"

# ---------- CHECK JAVA ----------
if ! command -v java >/dev/null 2>&1; then
  echo -e "${RED}❌ Java is not installed${NC}"
  exit 1
fi

# ---------- CHECK MAVEN ----------
if ! command -v mvn >/dev/null 2>&1; then
  echo -e "${RED}❌ Maven is not installed${NC}"
  exit 1
fi

# ---------- CHECK FORMATTING (OPTIONAL) ----------
if command -v google-java-format >/dev/null 2>&1; then
  echo "🎨 Running Java formatting check"
  git diff --cached --name-only --diff-filter=ACM \
    | grep '\.java$' \
    | xargs -r google-java-format --dry-run
else
  echo -e "${YELLOW}⚠️ google-java-format not found (skipping format check)${NC}"
fi

# ---------- CHECKSTYLE ----------
echo "📏 Running Checkstyle"
mvn -q checkstyle:check || {
  echo -e "${RED}❌ Checkstyle violations found${NC}"
  exit 1
}

# ---------- UNIT TESTS (FAST ONLY) ----------
echo "🧪 Running fast tests (@smoke, @api)"
mvn -q test \
  -Denv=QA \
  -Dcucumber.filter.tags="@smoke or @api" \
  -DskipSlowTests=true || {
    echo -e "${RED}❌ Fast tests failed${NC}"
    exit 1
  }

# ---------- SECURITY GUARD ----------
echo "🔐 Checking for secrets"
if git diff --cached | grep -E "(password=|secret=|apiKey=|Authorization:)"; then
  echo -e "${RED}❌ Potential secret detected in commit${NC}"
  exit 1
fi

# ---------- FILE SIZE CHECK ----------
echo "📦 Checking file sizes"
MAX_SIZE=5242880 # 5MB

git diff --cached --name-only | while read -r file; do
  if [[ -f "$file" ]]; then
    size=$(stat -f%z "$file" 2>/dev/null || stat -c%s "$file")
    if [[ "$size" -gt "$MAX_SIZE" ]]; then
      echo -e "${RED}❌ File too large: $file ($size bytes)${NC}"
      exit 1
    fi
  fi
done

# ---------- FINAL RESULT ----------
echo "========================================"
echo -e "${GREEN}✅ Pre-commit checks passed${NC}"
echo "========================================"
