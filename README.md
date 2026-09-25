# Employee Management System

## Project Overview

The Employee Management System is a semester project for INFO-C 451: System Implementation at Indiana University. The goal of the project is to develop a centralized system for managing employee information and common employee management processes.

The system will provide different functionality for employees, managers, and HR administrators. The project will focus on employee records, employee self-service, onboarding, time-off requests, management reporting, role-based access, and audit logging.

## Team Members

- Danielle Kemp
- Susan Rogers
- Nathaniel Johnson
- Stephen Torrijas

## Planned Features

- Employee record management
- Employee profile management
- Employee onboarding
- Time-off requests and tracking
- Manager approval workflows
- Basic employee and management reports
- Role-based access
- Audit logging

## Technology Stack

- **Programming Language:** Java
- **Framework:** Spring Boot
- **Front End:** HTML, CSS, JavaScript
- **Back End:** Java and Spring Boot
- **Database:** MySQL
- **Version Control:** Git and GitHub

## Project Scope

The initial version of the Employee Management System will focus on employee records, self-service profile management, onboarding, time-off workflows, and basic management reporting.

Payroll processing, tax calculations, employee benefits administration, and integrations with external payroll or accounting systems are outside the scope of the initial project.

## Project Status

Assignment 3 – System Architecture and Behavioral Design
Pushed employee time-off request feature to a seperate branch


## Skeleton Setup Instructions

### Required software

| Software | Version | Notes |
|---|---|---|
| Java Development Kit | 21 or later | The project targets Java 21.|
| Apache Maven | 3.9.16 | The Maven Wrapper (mvnw, mvnw.cmd) is in the repository and will download Maven on first use. |
| MySQL Server | 8.0 or later | A local install or a hosted somewhere else both work. |
| Web browser | Any current version | The user interface is static HTML, CSS, and JavaScript. |
| MySQL Workbench | Optional | Useful for inspecting tables and loading sample data. |

### Database configuration

1. Start MySQL and note the host, port, and an account the application can use.
2. Create a database user for the application. The schema **ems_db** is created automatically on first start if the user has the global **CREATE** privilege. Otherwise create it first:

   ```sql
   CREATE DATABASE IF NOT EXISTS ems_db;
   CREATE USER 'ems'@'%' IDENTIFIED BY '<password>';
   GRANT ALL PRIVILEGES ON ems_db.* TO 'ems'@'%';
   FLUSH PRIVILEGES;
   ```

### Environment variables and configuration files

Database settings are read from environment variables in **EMSBackend/src/main/resources/application.properties.**

| Variable | Default | Purpose |
|---|---|---|
| DB_HOST | localhost | MySQL host name or IP address |
| DB_PORT | 3306 | MySQL port |
| DB_NAME | ems_db | Schema name |
| DB_USER | root | MySQL account |
| DB_PASSWORD | *(none)* | Password for the account |

The simplest way to supply the variables is a .env file in the EMSBackend folder. Spring is configured to load it automatically. You can copy the example and fill in the values using the terminal:

```
cd 1_code/EmployeeManagementSystem/EMSBackend
copy .env.example .env
```

```
DB_HOST=localhost
DB_PORT=3306
DB_NAME=ems_db
DB_USER=ems
DB_PASSWORD=<password>
```

### Starting the back end

From 1_code/EmployeeManagementSystem/EMSBackend run:

```
.\mvnw.cmd spring-boot:run

```

The first run downloads Maven and the dependencies.

The API listens on port 8080. You can confirm it is up by going to **http://localhost:8080/api/employees** in a browser.

### Starting the user interface

With the back end running:

- For now open the pages directly from the file system, for example `EMSFrontend/assets/onboarding.html`


Screens: onboarding.html (create employee), employees.html (employee directory), profile.html, timeoff_employee.html, manager_approvals.html, user_access.html.

### Initial data

The system starts from an empty database. The onboarding screen creates the first employee record and the employee directory lists it.

For testing, sample employees can be loaded with a SQL script in MySQL Workbench. The manager approvals screen can be exercised by inserting a few rows into **time_off_request** that reference existing employee ids. 

### Example

```sql
INSERT INTO time_off_request (employee_id, leave_type, start_date, end_date, days_requested, reason, status, submitted_at) VALUES
  (1, 'VACATION', '2026-10-05', '2026-10-09', 5, 'Family vacation', 'PENDING', NOW()),
  (2, 'SICK', '2026-09-24', '2026-09-25', 2, 'Flu', 'PENDING', NOW());
```

