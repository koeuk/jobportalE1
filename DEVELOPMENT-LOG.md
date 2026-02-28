# Job Portal - Development Log

## Overview

This document summarizes all development work done on the Job Portal application, including feature additions, UI redesigns, and configuration changes.

---

## 1. Sidebar Navigation for Recruiters

### What was done
- Created a reusable sidebar fragment (`fragments/sidebar.html`) with three navigation components:
  - **Sidebar** (Recruiter): Dashboard, Jobs, Post Job, Profile, Users, User Types, Logout
  - **Top Navbar** (Job Seeker): Home, Jobs, Profile, Logout
  - **Guest Navbar** (Guest): Home, Jobs, Login, Register
- Added sidebar CSS in `styles.css` (fixed 250px dark sidebar, active link highlighting, responsive collapse)
- Updated all authenticated templates to use sidebar layout for recruiters

### Files created
- `src/main/resources/templates/fragments/sidebar.html`

### Files modified
- `src/main/resources/static/css/styles.css`
- `src/main/resources/templates/dashboard.html`
- `src/main/resources/templates/profile.html`
- `src/main/resources/templates/post-job.html`
- `src/main/resources/templates/applicants.html`
- `src/main/resources/templates/jobs.html`
- `src/main/resources/templates/job-details.html`
- `src/main/resources/templates/index.html`

---

## 2. Role-Based Navigation

### What was done
- Recruiters see a **sidebar** on all authenticated pages
- Job Seekers see a **top navbar** with: Home, Jobs, Profile, Logout
- Guests (not logged in) see a **top navbar** with: Home, Jobs, Login, Register
- Used `sec:authorize` Thymeleaf attributes for conditional rendering by role

### Implementation
All templates use three `sec:authorize` blocks:
```html
<div sec:authorize="hasRole('RECRUITER')" th:replace="fragments/sidebar :: sidebar('page')"></div>
<div sec:authorize="hasRole('JOB_SEEKER')" th:replace="fragments/sidebar :: topbar('page')"></div>
<div sec:authorize="!isAuthenticated()" th:replace="fragments/sidebar :: guestbar('page')"></div>
```

---

## 3. Role-Based Login Redirect

### What was done
- Created `CustomAuthenticationSuccessHandler` to route users after login:
  - **Recruiter** -> `/dashboard`
  - **Job Seeker** -> `/jobs`
- Job Seekers visiting `/` (home) are automatically redirected to `/jobs`

### Files created
- `src/main/java/com/jobportal/config/CustomAuthenticationSuccessHandler.java`

### Files modified
- `src/main/java/com/jobportal/config/SecurityConfig.java`
- `src/main/java/com/jobportal/controller/MainController.java`

---

## 4. Email Update on Profile

### What was done
- Added email field to profile forms for both Recruiter and Job Seeker
- Email is stored in the `Users` entity (not profile tables)
- Includes duplicate email validation - returns error if email already exists

### Files modified
- `src/main/java/com/jobportal/controller/MainController.java`
- `src/main/resources/templates/profile.html`

---

## 5. Job Seeker Profile Redesign

### What was done
- Profile header with avatar circle (initials), name, email, and location
- 3 Bootstrap tabs below the header:
  - **Profile Info**: Edit form with email, name, location, employment type, work authorization, resume, skills
  - **Saved Jobs**: Cards showing saved jobs with View Details button
  - **Applications**: Table listing applied jobs with date and status badge
- Each tab shows an empty state message with action link when no data exists

### Files modified
- `src/main/java/com/jobportal/controller/MainController.java` (added `@Transactional`, loaded `appliedJobs` and `savedJobs`)
- `src/main/resources/templates/profile.html`

---

## 6. Job Seeker Jobs Page Redesign

### What was done
- Modern "Explore Job Listings" layout for job seekers
- Filter bar with search input, location filter, and Search button
- 3-column card grid with:
  - Company logo icon
  - "New" badge (posted within 3 days) or "Posted Xd ago" badge
  - Job title (linked), company name, location, salary, job type
  - Save button and Quick Apply button
- Jobs count indicator
- Empty state with search icon

### Files modified
- `src/main/resources/templates/jobs.html`

---

## 7. Login & Register Page Redesign

### What was done
- **Login page**: Split-screen layout with gradient left panel (feature list) and form on right
- **Register page**: Split-screen layout matching login, with clickable user type cards (Job Seeker / Recruiter) instead of dropdown
- Styled inputs with rounded corners, gradient buttons

### Files modified
- `src/main/resources/templates/login.html`
- `src/main/resources/templates/register.html`

---

## 8. Bug Fixes

| Issue | Fix |
|-------|-----|
| `SecurityConfig.java` had `public class nowhat SecurityConfig` typo | Removed `nowhat` |
| Job Seeker home page showed landing page instead of jobs | Added redirect from `/` to `/jobs` for seekers |
| `index.html` showed old navbar for logged-in seekers | Added role-based fragment includes |
| Jobs page showed plain layout for seekers | Applied modern card grid design with `sec:authorize` conditional blocks |

---

## 9. Configuration

### Database Setup (SETUP.md)
- MySQL 8.0+ required
- Update `spring.datasource.password` in `application.properties`
- Database `jobportal` is auto-created on first run

### Running the App
```bash
mvn spring-boot:run
```
Then open: http://localhost:8080

---

## 10. Role Capabilities Summary

### Job Seeker
- Browse and search job listings (modern card grid UI)
- View job details (title, company, location, salary, type, remote, description)
- Quick Apply / Apply Now for jobs
- Save jobs for later
- View and update profile (name, email, location, employment type, work authorization)
- Upload resume
- Add/manage skills
- View saved jobs and application history (Profile tabs)

### Recruiter
- Dashboard with statistics (total jobs posted, applications received, active listings)
- Post new jobs (title, description, type, salary, location, remote option)
- View all job listings
- View applicants for each job
- Manage profile (company info, email)
- Admin: manage Users and User Types

### Guest
- Browse job listings
- View job details
- Search jobs by keyword and location
- Login / Register

---

## Technology Stack

- **Backend**: Spring Boot 3.1.5, Java 17
- **Security**: Spring Security with role-based access
- **Templates**: Thymeleaf with Spring Security extras
- **Database**: MySQL 8.0+ with JPA/Hibernate
- **Frontend**: Bootstrap 4.6.2, Font Awesome 6.0
- **Build**: Maven
