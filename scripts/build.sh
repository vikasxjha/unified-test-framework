#!/bin/bash
set -e
cd /Users/vikasjha/unified-test-framework
echo "Starting Maven build..."
mvn clean test 2>&1 | tail -100
echo "Build completed!"

