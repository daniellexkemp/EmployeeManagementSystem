# Employee Management System

## Project Overview

The Employee Management System (EMS) is a semester project for INFO-C 451: System Implementation at Indiana University.

The project provides a centralized web application for managing employee records and supporting common employee management processes. It includes employee onboarding, employee profile updates, time-off requests, manager approval workflows, and role-based access for employees, managers, and HR administrators.

The system uses a Java Spring Boot backend, a MySQL database, and an HTML, CSS, and JavaScript frontend.

## Team Members

- Danielle Kemp
- Susan Rogers
- Nathaniel Johnson
- Stephen Torrijas

## Technology Stack

| Component | Technology |
|---|---|
| Programming language | Java |
| Backend framework | Spring Boot |
| Frontend | HTML, CSS, JavaScript |
| Database | MySQL 8.0 or later |
| Build tool | Maven Wrapper |
| Version control | Git and GitHub |
| Development environment | Visual Studio Code and other Java-compatible IDEs |

## Implemented Functionality

The current implementation supports:

- User authentication and sign-out
- Role-based access for employees, managers, and HR administrators
- Employee directory and employee record management
- Employee onboarding
- Employee profile updates
- Employee time-off request submission
- Manager review and approval or denial of time-off requests
- Persistence of employee and time-off records in MySQL
- Administrative user-access management

Additional functionality described in earlier project requirements may remain planned or partially implemented.

## Project Scope

The implementation focuses on employee records, onboarding, employee self-service, time-off workflows, and role-based access.

Payroll processing, tax calculations, benefits administration, and integration with external payroll or accounting platforms are outside the current project scope.

## Repository Structure

The application is located under:

`1_code/EmployeeManagementSystem/`

Important directories and files include:

- `EMSBackend/` — Spring Boot application and backend source code
- `EMSBackend/src/main/resources/static/assets/` — frontend HTML, CSS, and JavaScript files served by Spring Boot
- `EMSBackend/database/ems_db_demo_seed.sql` — demonstration database seed
- `EMSBackend/.env.example` — example environment configuration, when present
- `README.md` — repository-level documentation

## Prerequisites

Before running the application, install:

1. Java Development Kit (JDK) 21 or later, compatible with the project's Maven configuration
2. MySQL Server 8.0 or later
3. A modern web browser
4. Git, if cloning the repository

MySQL Workbench is recommended for importing the demonstration database and inspecting records.

The repository includes the Maven Wrapper, so a separate Maven installation is generally unnecessary.

## 1. Obtain the Source Code

Clone the repository:

```bash
git clone https://github.com/daniellexkemp/EmployeeManagementSystem.git
```

Open the cloned repository in Visual Studio Code or another compatible development environment.

Navigate to:

```text
1_code/EmployeeManagementSystem/EMSBackend
```

For Assignment 6 evaluation, use the exact Git commit identified in the submitted Assignment 6 documentation.

## 2. Prepare the MySQL Database

Start MySQL Server and confirm that you can connect to it.

For a reproducible demonstration, use a **new, empty database** rather than an existing database containing important records.

The repository includes the demonstration seed file:

```text
EMSBackend/database/ems_db_demo_seed.sql
```

This file contains sample employee records, login accounts, and time-off requests.

### Import the demonstration data

1. Open MySQL Workbench.
2. Connect to your local MySQL Server.
3. Open `ems_db_demo_seed.sql` using **File > Open SQL Script**.
4. Review the database name at the beginning of the script.
5. Ensure the script targets a new, empty schema.
6. Execute the script using the lightning-bolt button.
7. Confirm the import completes without errors.

The script is intended for demonstration and testing only. Do not import it into a production database or an existing database containing important records.

### Verify the imported records

If the demonstration database is named `ems_db`, run:

```sql
SELECT
    (SELECT COUNT(*) FROM ems_db.employee) AS employees,
    (SELECT COUNT(*) FROM ems_db.users) AS users,
    (SELECT COUNT(*) FROM ems_db.time_off_request) AS time_off_requests;
```

The seed used for the Assignment 6 reproducibility test contained:

| Table | Seed records |
|---|---:|
| `employee` | 5 |
| `users` | 3 |
| `time_off_request` | 4 |

These counts describe the initial demonstration data. They may increase or change as the application is used.

## 3. Configure Database Connectivity

The Spring Boot backend reads database connection settings from environment variables.

A `.env` file can be placed inside the `EMSBackend` directory.

If `.env.example` is available, copy it to `.env` and update the values.

Example:

```properties
DB_HOST=localhost
DB_PORT=3306
DB_NAME=ems_db
DB_USER=your_mysql_username
DB_PASSWORD=your_mysql_password
```

Replace the username and password with credentials for your own MySQL installation.

The MySQL user must have appropriate permissions to access the selected database.

**Important:** Do not commit `.env` files containing real credentials to GitHub.

### Using a separate test database

The application can also run against a different schema by changing `DB_NAME`.

For example:

```properties
DB_NAME=ems_repro_test
```

If PowerShell environment variables are already set, they may override the corresponding `.env` values.

You can inspect the selected database name in PowerShell:

```powershell
echo $env:DB_NAME
```

To set the database for the current PowerShell session:

```powershell
$env:DB_NAME = "ems_repro_test"
```

The database name in the Spring Boot startup log should match the database you imported.

## 4. Start the Application

Open PowerShell or a terminal in:

```text
1_code/EmployeeManagementSystem/EMSBackend
```

On Windows, run:

```powershell
.\mvnw.cmd spring-boot:run
```

Allow Maven to download any required dependencies on the first run.

The application normally starts on port `8080`.

A successful startup includes a message similar to:

```text
Tomcat started on port 8080
Started EmployeeManagementSystemApplication
```

Keep the terminal running while using the application.

### Port 8080 already in use

If startup fails because port `8080` is already occupied, check whether another instance of the application is still running.

In PowerShell:

```powershell
Get-NetTCPConnection -LocalPort 8080 -State Listen -ErrorAction SilentlyContinue
```

Stop the previous Spring Boot instance before starting another one.

## 5. Open the Web Application

With Spring Boot running, open the login page in a browser:

```text
http://localhost:8080/assets/login.html
```

The frontend is served through Spring Boot.

**Do not open the HTML files directly from Windows File Explorer**, because the application's authentication and backend requests depend on the running server.

## 6. Demonstration Accounts

The demonstration database includes three preconfigured accounts for testing the application's role-based functionality.

| Username | Password | Role | Employee |
|---|---|---|---|
| `testadmin` | `EMS-Test-2026!` | HR_ADMIN | Taylor Admin |
| `testmanager` | `EMS-Test-2026!` | MANAGER | Morgan Manager |
| `testemployee` | `EMS-Test-2026!` | EMPLOYEE | Alex Employee |

All three accounts use the same demonstration password.

To sign in:

1. Start the Spring Boot application.
2. Navigate to `http://localhost:8080/assets/login.html`.
3. Enter one of the usernames and the demonstration password.
4. Sign in to access the functionality available to that role.

These accounts are included in `EMSBackend/database/ems_db_demo_seed.sql`.

## 7. Demonstration Workflow 1: Employee Onboarding and Profile Management

**Goal:** Demonstrate that an HR administrator can create an employee record and that changes persist in the database.

### Steps

1. Open the EMS login page.
2. Sign in using the Taylor Admin demonstration account.
3. Navigate to the onboarding page.
4. Create a new fictional employee using the required form fields.
5. Submit the onboarding form.
6. Confirm that the application displays a successful onboarding message.
7. Open the Employee Directory.
8. Locate the newly created employee.
9. Refresh the page and confirm the employee remains listed.
10. Navigate to Taylor Admin's profile.
11. Update an editable contact field, such as the phone number.
12. Save the change.
13. Refresh the profile and confirm the updated value persists.

**Expected result:** The new employee appears in the directory, and the administrator's saved contact change remains visible after refreshing.

**Note:** The profile update is performed while signed in as Taylor Admin. Creating a new employee does not automatically create a login account for that employee.

## 8. Demonstration Workflow 2: Employee Time-Off Request and Manager Approval

**Goal:** Demonstrate a workflow involving both employee and manager roles, including request submission, approval, and persistence.

### Steps

1. Sign in using the Alex Employee demonstration account.
2. Open the Time-Off Requests page.
3. Create a new vacation request using valid future dates.
4. Submit the request.
5. Confirm that the request appears with a `PENDING` status.
6. Sign out.
7. Sign in using the Morgan Manager demonstration account.
8. Navigate to Manager Approvals.
9. Locate the pending request submitted by Alex Employee.
10. Open the request and approve it.
11. Confirm that the application displays the approval result.
12. Sign out.
13. Sign back in as Alex Employee.
14. Open the Time-Off Requests page.
15. Confirm that the request status is now `APPROVED`.
16. Refresh the page and confirm the approved status persists.

**Expected result:** The employee submits a request, the authorized manager approves it, and the employee can see the updated status after signing in again.

This workflow demonstrates communication between the frontend, Spring Boot backend, and MySQL database, as well as the integration of employee and manager roles.

## 9. Reproducibility Verification

The demonstration setup was tested using a separate MySQL schema named:

```text
ems_repro_test
```

The database seed was imported into this schema, and the backend was configured to connect to it.

The following checks were completed:

- The SQL seed imported successfully.
- The database contained the expected initial sample records.
- Spring Boot connected successfully to the test schema.
- The application started on port 8080.
- A demonstration account could sign in.
- Employee records appeared in the directory after refreshing.
- The application workflows were tested successfully.

These checks establish that the application can run against a separately imported demonstration database rather than relying exclusively on the original development database.

## 10. Troubleshooting

### Application fails because port 8080 is occupied

Stop any other running Spring Boot instance, then restart the application.

### Login page returns HTTP 401

Confirm that Spring Boot is running and that you are opening the application's login page rather than a protected API endpoint.

Use:

```text
http://localhost:8080/assets/login.html
```

### Database connection fails

Verify that:

- MySQL Server is running.
- `DB_HOST` and `DB_PORT` are correct.
- `DB_NAME` matches the imported schema.
- The MySQL username and password are correct.
- The account has the necessary database permissions.

Check the JDBC URL in the Spring Boot startup log to confirm the selected database.

### Demonstration employees are missing

Check that the seed was imported into the same schema the application is using.

For example:

```sql
SELECT COUNT(*) FROM ems_repro_test.employee;
```

If the application connects to `ems_repro_test`, importing data only into `ems_db` will not populate the application's active database.

### Port is open, but the application is not responding correctly

Verify that the process listening on port 8080 is the intended Spring Boot instance and that the terminal shows a successful startup.

## 11. Assignment 6 Demonstration

The Assignment 6 recorded demonstration covers two integrated workflows:

1. Employee onboarding and profile management
2. Employee time-off submission and manager approval

The recording, exact submitted Git commit hash, and additional evaluation details are provided in the Assignment 6 semester project documentation.

## Security and Data Notice

This repository is an educational project.

The demonstration SQL seed includes fictional employee information and reusable development account password hashes. It is provided exclusively for testing and evaluation.

Do not use the demonstration credentials or database seed in a production environment.