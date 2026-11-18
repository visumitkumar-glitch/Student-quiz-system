@echo off
setlocal
set "SQLITE_JDBC_JAR=%SQLITE_JDBC_JAR%"
if "%SQLITE_JDBC_JAR%"=="" set "SQLITE_JDBC_JAR=%USERPROFILE%\.m2\repository\org\xerial\sqlite-jdbc\3.47.1.0\sqlite-jdbc-3.47.1.0.jar"
set "JAVA_BIN=java"
set "JAVAC_BIN=javac"
if exist "%ProgramFiles%\Java\latest\jdk-26\bin\java.exe" set "JAVA_BIN=%ProgramFiles%\Java\latest\jdk-26\bin\java.exe"
if exist "%ProgramFiles%\Java\latest\jdk-26\bin\javac.exe" set "JAVAC_BIN=%ProgramFiles%\Java\latest\jdk-26\bin\javac.exe"

if not exist "%SQLITE_JDBC_JAR%" (
  echo SQLite JDBC driver not found.
  echo Set SQLITE_JDBC_JAR to the full path of sqlite-jdbc-3.47.1.0.jar and run again.
  pause
  exit /b 1
)

if not exist build mkdir build
"%JAVAC_BIN%" -cp "%SQLITE_JDBC_JAR%" -d build src\*.java
if errorlevel 1 (
  pause
  exit /b 1
)
"%JAVA_BIN%" -cp "build;%SQLITE_JDBC_JAR%" QuizApp
