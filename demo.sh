#!/bin/bash
# Compile (via Maven) and run the non-interactive demo of all scenarios
# (exit code 0 = every check passed).
set -e
cd "$(dirname "$0")"

MVN_CMD=mvn
if ! command -v mvn >/dev/null 2>&1; then
    MVN_CMD="$HOME/Downloads/apache-maven-3.10.0-bin/apache-maven-3.10.0/bin/mvn"
fi

"$MVN_CMD" -q compile
[ -f target/cp.txt ] || "$MVN_CMD" -q dependency:build-classpath -Dmdep.outputFile=target/cp.txt

DEPS=$(cat target/cp.txt)
java -cp "target/classes:$DEPS" com.thapar.sportssphere.app.DemoRunner
