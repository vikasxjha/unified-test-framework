#!/usr/bin/env bash
set -e

#############################################
# Refactor E2E Tests to use BaseWebTest
#############################################

echo "🔄 Refactoring E2E tests to use UnifiedTestListener..."

PROJECT_ROOT="$(cd "$(dirname "$0")/.." && pwd)"
E2E_DIR="$PROJECT_ROOT/src/test/java/com/company/qa/unified/e2e"

# Find all E2E test files
find "$E2E_DIR" -name "*E2ETest.java" -type f | while read -r file; do

    # Check if file already extends BaseWebTest
    if grep -q "extends BaseWebTest" "$file"; then
        echo "✓ Already refactored: $(basename "$file")"
        continue
    fi

    echo "🔧 Refactoring: $(basename "$file")"

    # Create backup
    cp "$file" "$file.backup"

    # Perform refactoring using sed
    # 1. Add BaseWebTest import
    sed -i '' '/^package/a\
\
import com.company.qa.unified.base.BaseWebTest;
' "$file"

    # 2. Remove Playwright imports that are no longer needed
    sed -i '' '/^import com.microsoft.playwright.Playwright;/d' "$file"
    sed -i '' '/^import com.microsoft.playwright.Browser;/d' "$file"
    sed -i '' '/^import com.microsoft.playwright.BrowserType;/d' "$file"
    sed -i '' '/^import com.microsoft.playwright.Page;/d' "$file"
    sed -i '' '/^import com.microsoft.playwright.\*;/d' "$file"

    # 3. Add extends BaseWebTest to class declaration
    sed -i '' 's/^public class \(.*\)E2ETest {$/public class \1E2ETest extends BaseWebTest {/' "$file"

    # 4. Remove try-with-resources wrapper (basic pattern - manual review needed)
    # This is a simplified approach - complex cases need manual review

    echo "   ⚠️  Manual review recommended for: $(basename "$file")"

done

echo ""
echo "✅ Refactoring complete!"
echo "⚠️  Please review the changes and test compilation"
echo ""
echo "To test:"
echo "  mvn clean test-compile"
echo ""
echo "To restore backups if needed:"
echo "  find $E2E_DIR -name '*.backup' -exec bash -c 'mv \"\$0\" \"\${0%.backup}\"' {} \;"

