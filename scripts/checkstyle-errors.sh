#!/usr/bin/env bash
# Run from AITravelPlannerBackEnd: ./scripts/checkstyle-errors.sh
# Captures full checkstyle output to target/checkstyle-full.txt
# Then prints only [ERROR] lines so you can paste to share.
set -e
cd "$(dirname "$0")/.."
mkdir -p target
echo "Running checkstyle (may take a minute)..."
./mvnw checkstyle:check 2>&1 | tee target/checkstyle-full.txt || true
echo ""
echo "=== All ERROR lines (paste this to share) ==="
grep '\[ERROR\]' target/checkstyle-full.txt || true
echo ""
echo "Full log saved to target/checkstyle-full.txt"
