@echo off
REM Set JAVA_HOME to JDK 17
set "JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-17.0.18.8-hotspot"
set "PATH=%JAVA_HOME%\bin;%PATH%"

echo Using Java from: %JAVA_HOME%
echo.

REM Check if Maven is available
where mvn >nul 2>nul
if %errorlevel% neq 0 (
    echo Maven not found! Please install Maven or use IDE.
    echo.
    echo To run in IntelliJ:
    echo 1. Open project in IntelliJ
    echo 2. Go to File ^> Project Structure
    echo 3. Set Project SDK to JDK 17
    echo 4. Run JobPortalApplication
    pause
    exit /b 1
)

REM Run Spring Boot
echo Starting Job Portal Application...
mvn spring-boot:run
