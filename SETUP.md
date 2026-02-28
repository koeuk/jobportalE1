# Job Portal - Setup Guide

## Prerequisites

- Java 17 or higher
- Maven 3.8+
- MySQL 8.0+ (via WAMP, XAMPP, or standalone)

## Configuration

### Database Setup

Before running the application, update the database connection in:

```
src/main/resources/application.properties
```

| Property | Line | Description | Example |
|----------|------|-------------|---------|
| `spring.datasource.password` | 7 | Your MySQL root password | `password` or leave empty |

#### Default configuration:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/jobportal?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true&createDatabaseIfNotExist=true
spring.datasource.username=root
spring.datasource.password=koeuk
```

#### For Windows (WAMP/XAMPP default - no password):

```properties
spring.datasource.password=
```

#### For Windows (custom password):

```properties
spring.datasource.password=your_password_here
```

> The database `jobportal` will be created automatically on first run.

## Run the Application

```bash
mvn spring-boot:run
```

Then open: [http://localhost:8080](http://localhost:8080)

## Default Roles

| Role | Menu | After Login |
|------|------|-------------|
| Job Seeker | Home, Jobs, Profile, Logout (top navbar) | Redirects to `/jobs` |
| Recruiter | Dashboard, Jobs, Post Job, Profile, Users, User Types, Logout (sidebar) | Redirects to `/dashboard` |
| Guest | Home, Jobs, Login, Register (top navbar) | — |
