# Job Portal - Setup Guide

## Prerequisites

- Java 17 or higher ([Download](https://www.oracle.com/java/technologies/downloads/))
- Maven 3.8+ ([Download](https://maven.apache.org/download.cgi))
- MySQL 8.0+ (via WAMP, XAMPP, or standalone)

## Step-by-Step Setup (Windows)

### 1. Install Java 17

1. Download and install JDK 17 from Oracle or AdoptOpenJDK
2. Set `JAVA_HOME` environment variable:
   - Right-click **This PC** > **Properties** > **Advanced system settings** > **Environment Variables**
   - Add new System variable: `JAVA_HOME` = `C:\Program Files\Java\jdk-17` (your install path)
   - Add `%JAVA_HOME%\bin` to the `Path` variable
3. Verify: open Command Prompt and run:
   ```
   java -version
   ```

### 2. Install Maven

1. Download Maven and extract to a folder (e.g. `C:\apache-maven-3.9.6`)
2. Add `MAVEN_HOME` = `C:\apache-maven-3.9.6` to System variables
3. Add `%MAVEN_HOME%\bin` to the `Path` variable
4. Verify:
   ```
   mvn -version
   ```

### 3. Install MySQL

#### Option A: Using WAMP Server
1. Download and install [WAMP](https://www.wampserver.com/)
2. Start WAMP and ensure MySQL service is running (green icon)
3. Default: username `root`, no password

#### Option B: Using XAMPP
1. Download and install [XAMPP](https://www.apachefriends.org/)
2. Open XAMPP Control Panel and start **MySQL**
3. Default: username `root`, no password

#### Option C: Standalone MySQL
1. Download and install [MySQL Community Server](https://dev.mysql.com/downloads/mysql/)
2. During setup, set your root password

### 4. Configure Database Connection

Open the file:
```
src/main/resources/application.properties
```

Update **line 7** with your MySQL password:

| Property | Description |
|----------|-------------|
| `spring.datasource.password` | Your MySQL root password |

#### For WAMP/XAMPP (default - no password):
```properties
spring.datasource.password=
```

#### For custom MySQL password:
```properties
spring.datasource.password=your_password_here
```

> The database `jobportal` will be created automatically on first run. No need to create it manually.

### 5. Run the Application

Open Command Prompt or PowerShell in the project folder and run:

```
mvn spring-boot:run
```

Wait until you see:
```
Started JobPortalApplication in X seconds
```

Then open your browser: [http://localhost:8080](http://localhost:8080)

### 6. Stop the Application

Press `Ctrl + C` in the terminal where the app is running.

---

## Troubleshooting

| Problem | Solution |
|---------|----------|
| `java is not recognized` | Set `JAVA_HOME` and add `%JAVA_HOME%\bin` to `Path` |
| `mvn is not recognized` | Set `MAVEN_HOME` and add `%MAVEN_HOME%\bin` to `Path` |
| `Communications link failure` | Make sure MySQL is running (check WAMP/XAMPP) |
| `Access denied for user 'root'` | Update `spring.datasource.password` in `application.properties` |
| Port 8080 already in use | Stop the other process or change `server.port` in `application.properties` |

---

## Roles & Features

| Role | Menu | After Login |
|------|------|-------------|
| Job Seeker | Home, Jobs, Profile, Logout (top navbar) | Redirects to `/jobs` |
| Recruiter | Dashboard, Jobs, Post Job, Applications, Profile, Users, User Types, Logout (sidebar) | Redirects to `/dashboard` |
| Guest | Home, Jobs, Login, Register (top navbar) | — |
