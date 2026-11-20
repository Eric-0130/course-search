@echo off
setlocal
cd /d "%~dp0.."
if not exist .build mkdir .build
javac --release 17 -encoding UTF-8 -d .build src\main\java\edu\portfolio\coursesearch\*.java src\test\java\edu\portfolio\coursesearch\*.java
if errorlevel 1 exit /b 1
java -cp .build edu.portfolio.coursesearch.CourseSearchTests
