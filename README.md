# Job Portal - Spring Boot Application

A job portal web application built with Spring Boot, Thymeleaf, Spring Security, and MySQL.

## Prerequisites

### Required Software

| Software | Version | Download |
|----------|---------|----------|
| Java JDK | 17+ | https://adoptium.net/temurin/releases/?version=17 |
| MySQL | 8.0+ | https://dev.mysql.com/downloads/mysql/ (or use WAMP/XAMPP) |
| Maven | 3.9+ | https://maven.apache.org/download.cgi |

### Windows Setup

#### 1. Install Java JDK 17

- Download from [Adoptium](https://adoptium.net/temurin/releases/?version=17)
- During installation, check **"Set JAVA_HOME variable"**
- Verify: open Command Prompt and run:
```cmd
java -version
```

#### 2. Install Maven

- Download the **Binary zip archive** from https://maven.apache.org/download.cgi
- Extract to `C:\Program Files\Apache\maven`
- Add to **System Environment Variables**:
  - Add `MAVEN_HOME` = `C:\Program Files\Apache\maven\apache-maven-3.9.x`
  - Add `%MAVEN_HOME%\bin` to the `Path` variable
- Verify:
```cmd
mvn --version
```

#### 3. Install MySQL

**Option A: Standalone MySQL**
- Download from https://dev.mysql.com/downloads/mysql/
- Install and set root password

**Option B: WAMP Server (Recommended for Windows)**
- Download from https://www.wampserver.com/
- Start WAMP, MySQL runs automatically on port 3306

## Database Setup

### 1. Create the database

Open MySQL command line or phpMyAdmin and run the SQL scripts in order:

```sql
-- Run this first (from 00-starter-sql-scripts/00-starter-sql-scripts/)
source 00-create-user.sql;
source 01-jobportal.sql;
```

Or manually:
```sql
CREATE DATABASE jobportal;
```

> The tables will be auto-created by Hibernate (`spring.jpa.hibernate.ddl-auto=update`), but running `01-jobportal.sql` will also insert the required user types (Recruiter, Job Seeker).

### 2. Configure database credentials

Edit `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/jobportal?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true&createDatabaseIfNotExist=true
spring.datasource.username=root
spring.datasource.password=YOUR_MYSQL_PASSWORD
```

Replace `YOUR_MYSQL_PASSWORD` with your MySQL root password.

> **WAMP users**: default root password is empty (blank), so use:
> ```properties
> spring.datasource.password=
> ```

## Running the Application

### Option 1: Command Line

```cmd
mvn spring-boot:run
```

Or use the provided script:
```cmd
run.bat
```

### Option 2: IntelliJ IDEA

1. Open the project folder in IntelliJ
2. Go to **File > Project Structure > Project**
3. Set **SDK** to JDK 17
4. Wait for Maven to download dependencies
5. Run `JobPortalApplication.java` (right-click > Run)

### Option 3: Build JAR and Run

```cmd
mvn clean package -DskipTests
java -jar target/jobportal-1.0.0.jar
```

## Access the Application

Once running, open your browser:

| Page | URL |
|------|-----|
| Home | http://localhost:8080 |
| Login | http://localhost:8080/login |
| Register | http://localhost:8080/register |
| Browse Jobs | http://localhost:8080/jobs |

## User Types

- **Recruiter** - Can post jobs and view applicants
- **Job Seeker** - Can browse jobs, apply, save jobs, and manage skills

## Project Structure

```
src/main/java/com/jobportal/
    config/          - Security configuration
    controller/      - Web controllers
    dto/             - Data Transfer Objects
    entity/          - JPA entities
    repository/      - Spring Data JPA repositories
    security/        - Custom authentication

src/main/resources/
    templates/       - Thymeleaf HTML templates
    application.properties - App configuration
```

## Troubleshooting

### Port 8080 already in use
Change the port in `application.properties`:
```properties
server.port=9090
```

### MySQL connection refused
- Make sure MySQL is running
- Check that the username/password in `application.properties` is correct
- For WAMP: make sure the WAMP icon is green (all services running)

### Maven not found
- Make sure Maven is installed and added to PATH
- Restart Command Prompt after setting environment variables
