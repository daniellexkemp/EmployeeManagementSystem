# Employee Management System

## Project Overview

The Employee Management System (EMS) is a semester project for INFO-C 451: System Implementation at Indiana University.

The project provides a centralized web application for managing employee records and supporting common employee management processes. It includes employee onboarding, employee profile updates, time-off requests, manager approval workflows, reporting, an audit log, and role-based access for employees, managers, and HR administrators.

The system uses a Java Spring Boot backend, a MySQL database, and an HTML, CSS, and JavaScript frontend.

## Team Members

- Danielle Kemp
- Susan Rogers
- Nathaniel Johnson
- Stephen Torrijas

## Technology Stack

| Component | Technology |
|---|---|
| Programming language | Java 21 |
| Backend framework | Spring Boot |
| Frontend | HTML, CSS, JavaScript |
| Database | MySQL 8.0 or later |
| Build tool | Maven Wrapper |
| Version control | Git and GitHub |
| Development environment | Visual Studio Code, IntelliJ IDEA, NetBeans, or another Java-compatible IDE |

## Implemented Functionality

The current implementation supports:

- User authentication and sign-out
- Role-based access for employees, managers, and HR administrators, enforced on both the pages and the backend
- Employee onboarding, including optional supervisor assignment
- Employee Directory with search, department and status filters, and CSV export
- HR administrator viewing, editing, deactivation, and reactivation of employee records
- Read-only directory view of assigned employees for managers
- Employee profile viewing and contact information updates
- Employee time-off request submission and status tracking
- Manager review and approval or denial of time-off requests, with an optional decision note
- HR Time-Off Report with filtering and CSV export
- Administrative user-access (role) management
- Audit log of sign-ins and record changes, viewable by HR administrators only
- Persistence of all records in MySQL

See [Known Limitations](#14-known-limitations) for functionality that is partially implemented or deferred.

## Project Scope

The implementation focuses on employee records, onboarding, employee self-service, time-off workflows, reporting, auditing, and role-based access.

Payroll processing, tax calculations, benefits administration, and integration with external payroll or accounting platforms are outside the current project scope.

## Repository Structure

The application is located under:

`1_code/EmployeeManagementSystem/`

Important directories and files include:

- `EMSBackend/` — Spring Boot application and backend source code
- `EMSBackend/src/main/java/com/employeemanagement/` — controllers, services, repositories, models, and security configuration
- `EMSBackend/src/main/resources/static/` — frontend served by Spring Boot (`assets/` HTML pages, `css/` stylesheets, `js/` scripts)
- `EMSBackend/src/test/java/com/employeemanagement/` — automated tests
- `EMSBackend/database/ems_db_demo_seed.sql` — demonstration database seed
- `EMSBackend/.env.example` — example database configuration
- `README.md` — repository-level documentation

## Prerequisites

Before running the application, install:

1. Java Development Kit (JDK) 21 or later
2. MySQL Server 8.0 or later
3. A modern web browser
4. Git, if cloning the repository

MySQL Workbench is recommended for importing the demonstration database and inspecting records.

The repository includes the Maven Wrapper (`mvnw` / `mvnw.cmd`), so a separate Maven installation is not required. The first run downloads Maven and the project dependencies, which requires an internet connection.

No other external services are required.

## 1. Obtain the Source Code

Clone the repository:

```bash
git clone https://github.com/daniellexkemp/EmployeeManagementSystem.git
```

Open the cloned repository in your development environment and navigate to:

```text
1_code/EmployeeManagementSystem/EMSBackend
```

For grading, check out the exact Git commit identified in the submitted assignment documentation.

## 2. Prepare the MySQL Database

Start MySQL Server and confirm that you can connect to it.

For a reproducible demonstration, use a **new, empty database**. The seed script creates its tables with `CREATE TABLE` and does not drop existing tables, so it stops with a "Table already exists" error if it is run against a database that already contains the application's tables.

The repository includes the demonstration seed file:

```text
EMSBackend/database/ems_db_demo_seed.sql
```

This file contains sample employee records, login accounts, and time-off requests.

### Choose the database name

The first lines of the seed script are:

```sql
CREATE DATABASE IF NOT EXISTS `ems_db` CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
USE `ems_db`;
```

- To use `ems_db`, run the script as-is. `ems_db` must not already contain the application's tables.
- To use a different schema (for example, `ems_repro_test`), replace `ems_db` in both lines before running the script, and set `DB_NAME` to the same name in Step 3.

### Import the demonstration data

1. Open MySQL Workbench.
2. Connect to your MySQL Server.
3. Open `ems_db_demo_seed.sql` using **File > Open SQL Script**.
4. Confirm the database name in the first two lines, as described above.
5. Execute the script using the lightning-bolt button.
6. Confirm the import completes without errors.

The script is intended for demonstration and testing only. Do not import it into a production database or an existing database containing important records.

### Verify the imported records

If the demonstration database is named `ems_db`, run:

```sql
SELECT
    (SELECT COUNT(*) FROM ems_db.employee) AS employees,
    (SELECT COUNT(*) FROM ems_db.users) AS users,
    (SELECT COUNT(*) FROM ems_db.time_off_request) AS time_off_requests;
```

A freshly imported seed contains:

| Table | Seed records |
|---|---:|
| `employee` | 5 |
| `users` | 3 |
| `time_off_request` | 4 |

These counts describe the initial demonstration data. They change as the application is used.

### Schema updates on first start

The seed was created before some features were added. On the first start, Spring Boot (Hibernate) automatically:

- creates the `audit_log` table
- adds the `employee.manager_id` column used for supervisor assignments

No manual schema changes are required.

## 3. Configure Database Connectivity

The Spring Boot backend reads database connection settings from environment variables or from a `.env` file in the `EMSBackend` directory.

Copy the example file and update the values:

```powershell
# Windows (PowerShell)
Copy-Item .env.example .env
```

```bash
# macOS / Linux
cp .env.example .env
```

Example `.env`:

```properties
DB_HOST=localhost
DB_PORT=3306
DB_NAME=ems_db
DB_USER=your_mysql_username
DB_PASSWORD=your_mysql_password
```

Replace the username and password with credentials for your own MySQL installation. The MySQL user needs permission to create tables and alter them in the selected database, because the application creates the `audit_log` table on first start.

**Important:** Do not commit `.env` files containing real credentials to GitHub.

### Using a separate test database

The application can run against a different schema by changing `DB_NAME`:

```properties
DB_NAME=ems_repro_test
```

If PowerShell environment variables are already set, they override the corresponding `.env` values. To inspect or set the database for the current PowerShell session:

```powershell
echo $env:DB_NAME
$env:DB_NAME = "ems_repro_test"
```

The JDBC URL in the Spring Boot startup log should name the database you imported.

## 4. Start the Application

Open a terminal in:

```text
1_code/EmployeeManagementSystem/EMSBackend
```

Run:

```powershell
# Windows (PowerShell)
.\mvnw.cmd spring-boot:run
```

```bash
# macOS / Linux
./mvnw spring-boot:run
```

The application starts on port `8080`. A successful startup includes a message similar to:

```text
Tomcat started on port 8080
Started EmployeeManagementSystemApplication
```

Keep the terminal running while using the application. Press `Ctrl+C` in the terminal to stop it.

## 5. Open the Web Application

With Spring Boot running, open the login page in a browser:

```text
http://localhost:8080/assets/login.html
```

The frontend is served by Spring Boot.

**Do not open the HTML files directly from File Explorer or Finder**, because authentication and backend requests depend on the running server.

## 6. Demonstration Accounts

The demonstration database includes three preconfigured accounts for testing the application's role-based functionality.

| Username | Password | Role | Employee |
|---|---|---|---|
| `testadmin` | `EMS-Test-2026!` | HR_ADMIN | Taylor Admin |
| `testmanager` | `EMS-Test-2026!` | MANAGER | Morgan Manager |
| `testemployee` | `EMS-Test-2026!` | EMPLOYEE | Alex Employee |

All three accounts use the same demonstration password and are included in `EMSBackend/database/ems_db_demo_seed.sql`.

To sign in, open `http://localhost:8080/assets/login.html`, enter one of the usernames and the demonstration password, and sign in to access the functionality available to that role.

## 7. Demonstration Workflow 1: Employee Onboarding and Profile Management

**Goal:** Demonstrate that an HR administrator can create and update employee records and that changes persist in the database.

### Steps

1. Sign in as `testadmin` (Taylor Admin).
2. Open **Onboarding**.
3. Create a new fictional employee using the required form fields, and optionally select a supervisor.
4. Submit the form and confirm the successful onboarding message.
5. Open the **Employee Directory** and locate the new employee.
6. Select **Edit**, change a field such as the job title, and save.
7. Refresh the page and confirm the employee and the edited value remain.
8. Open **Profile**, update an editable contact field such as the phone number, and save.
9. Refresh the profile and confirm the updated value persists.

**Expected result:** The new employee appears in the directory, the edit is saved, and the administrator's contact change remains visible after refreshing.

**Note:** Creating a new employee does not automatically create a login account for that employee.

## 8. Demonstration Workflow 2: Employee Time-Off Request and Manager Approval

**Goal:** Demonstrate a workflow involving both employee and manager roles, including request submission, approval, and persistence.

### Steps

1. Sign in as `testemployee` (Alex Employee).
2. Open **Time-Off Requests**.
3. Submit a vacation request using valid future dates.
4. Confirm that the request appears with a `PENDING` status, then sign out.
5. Sign in as `testmanager` (Morgan Manager).
6. Open **Manager Approvals** and locate the pending request from Alex Employee.
7. Select **View**, optionally enter a decision note, and select **Approve**.
8. Confirm the approval message, then sign out.
9. Sign back in as `testemployee` and open **Time-Off Requests**.
10. Confirm that the request status is now `APPROVED`, and refresh to confirm it persists.

**Expected result:** The employee submits a request, the authorized manager approves it, and the employee sees the updated status after signing in again.

## 9. Demonstration Workflow 3: Audit Log (HR Administrators Only)

**Goal:** Demonstrate that important actions are recorded in the audit log and that only HR administrators can view it.

The system records sign-ins, failed sign-ins, sign-outs, password and role changes, new user accounts, employee record changes, and time-off submissions and decisions. Each entry records the date and time, the user and role, the action, the affected record, and a short detail of the change.

### Steps

1. Complete Workflow 1 or Workflow 2 so that some actions have been recorded.
2. Sign in as `testadmin`.
3. Open **Audit Log** from the navigation bar or the dashboard.
4. Confirm that the recent actions appear, newest first.
5. Filter by username, action, or date range, and use **Previous** and **Next** to page through results.
6. Select **Export CSV** to download every entry that matches the current filters.
7. Sign out and sign in as `testmanager` or `testemployee`.
8. Confirm that the Audit Log link does not appear in the navigation bar or on the dashboard.
9. While signed in as that user, open `http://localhost:8080/api/audit-log` and confirm the request is rejected (`403 Forbidden`).

**Expected result:** HR administrators can view, filter, and export the audit log. Managers and employees cannot see it, and the backend rejects their requests. Audit entries cannot be edited or deleted through the application.

## 10. Automated Tests

From `1_code/EmployeeManagementSystem/EMSBackend`, run:

```powershell
# Windows (PowerShell)
.\mvnw.cmd test
```

```bash
# macOS / Linux
./mvnw test
```

The test suite includes:

| Test class | Coverage |
|---|---|
| `ContactUpdateRequestValidationTest` | Employee contact field validation |
| `TimeOffServiceTest` | Time-off submission rules and date validation |
| `TimeOffReportControllerTest` | Time-Off Report access by role |
| `audit/AuditControllerSecurityTest` | Audit log access restricted to HR administrators |
| `audit/AuditServiceTest` | Audit entry recording, search, and failure handling |
| `EmployeeManagementSystemApplicationTests` | Application context startup |

`EmployeeManagementSystemApplicationTests` starts the full application and needs a reachable database configured as in Step 3. The other tests run without a database. To run them only:

```powershell
.\mvnw.cmd test "-Dtest=!EmployeeManagementSystemApplicationTests"
```

## 11. Reproducibility Verification

The demonstration setup was tested using a separate MySQL schema named `ems_repro_test` and with a freshly reseeded `ems_db`.

The following checks were completed:

- The SQL seed imported successfully into an empty schema.
- The database contained the expected initial sample records.
- Spring Boot connected to the schema, created the `audit_log` table, and started on port 8080.
- All three demonstration accounts could sign in.
- Employee records appeared in the directory after refreshing.
- Sign-ins and failed sign-ins were recorded in the audit log.
- Only the HR administrator could open the audit log; the manager and employee accounts received `403 Forbidden`.

These checks establish that the application can run against a separately imported demonstration database rather than relying on the original development database.

## 12. Troubleshooting

### Seed import fails with "Table already exists"

The seed is intended for an empty database. Import it into a new schema (see Step 2) and point `DB_NAME` at that schema.

### Application fails because port 8080 is occupied

Another instance of the application may still be running. In PowerShell:

```powershell
Get-NetTCPConnection -LocalPort 8080 -State Listen -ErrorAction SilentlyContinue
```

Stop the previous Spring Boot instance before starting another one. If the terminal that started it was closed without pressing `Ctrl+C`, the Java process may still be running and can be ended from Task Manager.

### Login page returns HTTP 401

Confirm that Spring Boot is running and that you are opening the login page rather than a protected API endpoint:

```text
http://localhost:8080/assets/login.html
```

### Demonstration password is rejected

The application's database may not contain the demonstration accounts. Confirm that the seed was imported into the schema named by `DB_NAME`:

```sql
SELECT username, role FROM ems_db.users;
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

Check that the seed was imported into the same schema the application is using. For example, if the application connects to `ems_repro_test`, importing data only into `ems_db` will not populate the application's active database.

### Audit Log link does not appear

The Audit Log is available only to the `HR_ADMIN` role. Sign in as `testadmin`.

## 13. Assignment 8 Evaluation

Assignment 8 (System Integration and Implementation Milestone) can be evaluated with Workflows 1–3 and the automated tests above. The submitted Git commit hash and additional evaluation details are provided in the Assignment 8 semester project documentation.

## 14. Known Limitations

- Time-off requests are routed to managers by department rather than by the assigned supervisor.
- A supervisor can be assigned during onboarding but not yet changed afterward.
- Managers can view their assigned employees but cannot yet generate reports.
- Onboarding task and status tracking is deferred.
- Performance and scalability targets have not yet been measured.
- Audit entries record a summary of each change rather than full before-and-after values.

## Security and Data Notice

This repository is an educational project.

The demonstration SQL seed includes fictional employee information and reusable development account password hashes. It is provided exclusively for testing and evaluation.

Do not use the demonstration credentials or database seed in a production environment.
