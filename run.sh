#!/bin/bash
# Compile (via Maven, so the Spring/JPA sources get their classpath) and start
# the console menu. Extra arguments go to the program: ./run.sh --sample | --reset | other.dat
set -e
cd "$(dirname "$0")"

MVN_CMD=mvn
if ! command -v mvn >/dev/null 2>&1; then
    MVN_CMD="$HOME/Downloads/apache-maven-3.10.0-bin/apache-maven-3.10.0/bin/mvn"
fi

"$MVN_CMD" -q compile
[ -f target/cp.txt ] || "$MVN_CMD" -q dependency:build-classpath -Dmdep.outputFile=target/cp.txt

DEPS=$(cat target/cp.txt)
java -cp "target/classes:$DEPS" com.thapar.sportssphere.app.Main "$@"
