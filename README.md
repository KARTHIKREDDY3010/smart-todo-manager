# Smart Todo Manager

A task management web app with a Spring Boot REST API, MySQL database and a responsive Bootstrap 5 frontend.

## Features

- Create, view, update and delete tasks
- Task fields: title, description, priority (LOW, MEDIUM, HIGH), due date, status (TODO, ONGOING, COMPLETED)
- Search tasks by title
- Filter by status and priority
- Dashboard with total, pending, ongoing and completed counts
- Input validation with clear JSON error responses
- Responsive UI with overdue highlighting, toast messages and delete confirmation

## Tech stack

- Java 21, Spring Boot 4.1, Spring Web, Spring Data JPA, Bean Validation
- MySQL 8
- HTML, CSS, JavaScript, Bootstrap 5
- JUnit 5, Mockito, MockMvc, H2 (tests only)
- Maven, Git, Postman

## Screenshot
![Smart Todo Manager](screenshot.png)

## Getting started

### Requirements

- JDK 21 or newer
- MySQL 8 or newer

### 1. Create the database

```sql
CREATE DATABASE smart_todo_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'smart_todo_user'@'localhost' IDENTIFIED BY 'choose-a-strong-password';
GRANT ALL PRIVILEGES ON smart_todo_db.* TO 'smart_todo_user'@'localhost';
FLUSH PRIVILEGES;
```

### 2. Set the database password

The password is never stored in this repository. Use one of these:

- Create `src/main/resources/application-local.properties` (it is git-ignored) containing:

  ```properties
  spring.datasource.password=choose-a-strong-password
  ```

- Or set an environment variable named `DB_PASSWORD` before starting the app.

Other settings can be overridden with `DB_HOST`, `DB_PORT`, `DB_NAME` and `DB_USERNAME`.

### 3. Run

```cmd
mvnw.cmd spring-boot:run
```

On macOS or Linux use `./mvnw spring-boot:run`. Then open http://localhost:8080.

### 4. Run the tests

```cmd
mvnw.cmd test
```

Tests use an in-memory H2 database, so they never touch your MySQL data.

## REST API

| Method | URL | Description | Success |
|---|---|---|---|
| GET | `/api/tasks` | List tasks. Optional query parameters: `keyword`, `status`, `priority` | 200 |
| GET | `/api/tasks/{id}` | Get one task | 200 |
| GET | `/api/tasks/stats` | Dashboard counts | 200 |
| POST | `/api/tasks` | Create a task | 201 |
| PUT | `/api/tasks/{id}` | Update a task | 200 |
| PATCH | `/api/tasks/{id}/status` | Change only the status | 200 |
| DELETE | `/api/tasks/{id}` | Delete a task | 204 |

Example request:

```http
POST /api/tasks
Content-Type: application/json

{
  "title": "Learn Spring Boot",
  "description": "Finish the REST API",
  "priority": "HIGH",
  "dueDate": "2026-11-01"
}
```

Validation errors return **400** with a body like:

```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "fieldErrors": { "title": "Title is required" }
}
```

A missing task returns **404**.

## Project structure

```
src/main/java/smart_todo_api/
├── controller/   REST endpoints
├── service/      business logic
├── repository/   Spring Data JPA queries
├── entity/       Task, TaskStatus, TaskPriority
├── dto/          request and response objects
└── exception/    error handling
src/main/resources/static/   frontend (HTML, CSS, JS)
```

## Roadmap

- User registration and login with Spring Security
- Deployment on a free hosting service

## Author

[KARTHIKREDDY3010](https://github.com/KARTHIKREDDY3010)
