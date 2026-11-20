#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT"
mkdir -p .build
find src/main/java -name '*.java' -print0 | xargs -0 javac --release 17 -encoding UTF-8 -d .build
java -cp .build edu.portfolio.coursesearch.Main "$@"
