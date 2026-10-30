#!/usr/bin/env bash
# Compile and run the project with the plain JDK (no Maven needed).
# Usage:  ./scripts/run-without-maven.sh [mode] [--xml file] [--xsd file]
#         modes: validate | xpath | rules | suite | interactive | help   (default: everything)
set -euo pipefail
cd "$(dirname "$0")/.."

echo "[1/3] Compiling..."
mkdir -p target/classes
javac -encoding UTF-8 -d target/classes src/main/java/com/fss/soa/tp1/*.java

echo "[2/3] Copying resources (xml, xsd, invalid samples)..."
cp -r src/main/resources/* target/classes/

echo "[3/3] Running..."
java -cp target/classes com.fss.soa.tp1.Main "$@"
