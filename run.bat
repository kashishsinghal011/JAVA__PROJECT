@echo off
rem Compile (via Maven, so the Spring/JPA sources get their classpath) and start
rem the console menu. Extra arguments go to the program: run.bat --sample
setlocal
cd /d "%~dp0"

rem Locate Maven: use mvn.cmd from PATH if present, otherwise the known install.
set "MVN_CMD=mvn.cmd"
where mvn.cmd >nul 2>nul
if errorlevel 1 set "MVN_CMD=C:\Users\hp\Downloads\apache-maven-3.10.0-bin\apache-maven-3.10.0\bin\mvn.cmd"

call "%MVN_CMD%" -q compile
if errorlevel 1 (
    echo Build failed - see the Maven output above.
    exit /b 1
)

rem Cache the full dependency classpath once; reused until pom.xml changes.
if not exist target\cp.txt call "%MVN_CMD%" -q dependency:build-classpath -Dmdep.outputFile=target\cp.txt
if errorlevel 1 exit /b 1

set /p DEPS=<target\cp.txt
java -cp "target\classes;%DEPS%" com.thapar.sportssphere.app.Main %*
