#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$ROOT_DIR"

mkdir -p out

javac -cp "lib/postgresql-42.7.10.jar" -d out $(find src -name "*.java")
java -cp "out:lib/postgresql-42.7.10.jar" Main
