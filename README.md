# pScheduler - Personal Task Scheduler API

A robust, production-ready RESTful API for personal task and schedule management built with Spring Boot 3.2, featuring JWT authentication, comprehensive testing, and a scalable architecture.

## 🚀 Features

### Core Features
- ✅ User authentication and authorization with JWT
- ✅ Complete CRUD operations for tasks
- ✅ Task status management (Pending, In Progress, Completed, Cancelled, Overdue)
- ✅ Priority levels (Low, Medium, High, Urgent)
- ✅ Task deadlines with automatic overdue detection
- ✅ Recurring task support (Daily, Weekly, Monthly, Yearly)
- ✅ Task filtering and searching
- ✅ User-specific task isolation
- ✅ Comprehensive error handling
- ✅ Input validation
- ✅ Full test coverage

### Technical Features
- 🔐 JWT-based authentication
- 🗄️ JPA/Hibernate with H2 (dev) and PostgreSQL (prod)
- 📊 RESTful API design
- ✨ Clean architecture with separation of concerns
- 🧪 Comprehensive unit and integration tests
- 📝 Lombok for reduced boilerplate
- 🔍 Custom exception handling
- 📅 Audit fields (createdAt, updatedAt)

## 📋 Table of Contents

- [Prerequisites](#prerequisites)
- [Getting Started](#getting-started)
- [Project Structure](#project-structure)
- [API Documentation](#api-documentation)
- [Database Schema](#database-schema)
- [Configuration](#configuration)
- [Running Tests](#running-tests)
- [Development Roadmap](#development-roadmap)
- [Contributing](#contributing)

## 🔧 Prerequisites

- Java 17 or higher
- Maven 3.6+
- IDE (IntelliJ IDEA, Eclipse, or VS Code)
- Postman or similar API testing tool (optional)

## 🏁 Getting Started

### 1. Clone the Repository
```bash
git clone <your-repo-url>
cd pScheduler
```

### 2. Build the Project
```bash
mvn clean install
```

### 3. Run the Application
```bash
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

### 4. Access H2 Console (Development)
- URL: `http://localhost:8080/h2-console`
- JDBC URL: `jdbc:h2:mem:schedulerdb`
- Username: `sa`
- Password: (leave blank)

## 📁 Project Structure

```
com.masa.pScheduler/
├── config/
│   └── SecurityConfig.java           # Security configuration
├── controller/
│   ├── AuthController.java           # Authentication endpoints
│   └── TaskController.java           # Task management endpoints
├── dto/
│   ├── RegisterRequest.java          # Registration payload
│   ├── LoginRequest.java             # Login payload
│   ├── AuthResponse.java             # Auth response
│   ├── TaskCreateRequest.java        # Task creation payload
│   ├── TaskUpdateRequest.java        # Task update payload
│   ├── TaskResponse.java             # Task response DTO
│   └── UserResponse.java             # User response DTO
├── exception/
│   ├── ResourceNotFoundException.java
│   ├── ResourceAlreadyExistsException.java
│   ├── UnauthorizedAccessException.java
│   ├── ErrorResponse.java
│   └── GlobalExceptionHandler.java   # Global exception handler
├── model/
│   ├── User.java                     # User entity
│   └── Task.java                     # Task entity
├── repository/
│   ├── UserRepository.java           # User data access
│   └── TaskRepository.java           # Task data access
├── security/
│   ├── JwtUtil.java                  # JWT utilities
│   └── JwtAuthenticationFilter.java  # JWT filter
├── service/
│   ├── AuthService.java              # Authentication service
│   ├── TaskService.java              # Task business logic
│   └── CustomUserDetailsService.java # User details service
└── PSchedulerApplication.java        # Main application class
```

## 📡 API Documentation

### Base URL
```
http://localhost:8080/api/v1
```

### Authentication Endpoints

#### Register New User
```http
POST /auth/register
Content-Type: application/json

{
  "username": "johndoe",
  "email": "john@example.com",
  "password": "password123",
  "firstName": "John",
  "lastName": "Doe"
}
```

**Response (201 Created):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer",
  "userId": 1,
  "username": "johndoe",
  "email": "john@example.com"
}
```

#### Login
```http
POST /auth/login
Content-Type: application/json

{
  "username": "johndoe",
  "password": "password123"
}
```

**Response (200 OK):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer",
  "userId": 1,
  "username": "johndoe",
  "email": "john@example.com"
}
```

### Task Endpoints

**Note:** All task endpoints require JWT authentication. Include the token in the Authorization header:
```
Authorization: Bearer <your-jwt-token>
```

#### Create Task
```http
POST /tasks
Content-Type: application/json
Authorization: Bearer <token>

{
  "title": "Complete project documentation",
  "description": "Write comprehensive README",
  "deadline": "2025-11-01T15:00:00",
  "priority": "HIGH",
  "tags": "documentation,urgent",
  "isRecurring": false
}
```

**Response (201 Created):**
```json
{
  "id": 1,
  "title": "Complete project documentation",
  "description": "Write comprehensive README",
  "status": "PENDING",
  "priority": "HIGH",
  "deadline": "2025-11-01T15:00:00",
  "completedAt": null,
  "isRecurring": false,
  "recurrencePattern": null,
  "tags": "documentation,urgent",
  "userId": 1,
  "username": "johndoe",
  "createdAt": "2025-10-29T10:30:00",
  "updatedAt": "2025-10-29T10:30:00",
  "isOverdue": false
}
```

#### Get All Tasks
```http
GET /tasks
Authorization: Bearer <token>
```

#### Get Task by ID
```http
GET /tasks/{id}
Authorization: Bearer <token>
```

#### Get Tasks by Status
```http
GET /tasks/status/{status}
Authorization: Bearer <token>
```
Status values: `PENDING`, `IN_PROGRESS`, `COMPLETED`, `CANCELLED`, `OVERDUE`

#### Get Overdue Tasks
```http
GET /tasks/overdue
Authorization: Bearer <token>
```

#### Update Task
```http
PUT /tasks/{id}
Content-Type: application/json
Authorization: Bearer <token>

{
  "title": "Updated title",
  "description": "Updated description",
  "status": "IN_PROGRESS",
  "priority": "URGENT",
  "deadline": "2025-11-02T15:00:00"
}
```

#### Mark Task as Completed
```http
PATCH /tasks/{id}/complete
Authorization: Bearer <token>
```

#### Delete Task
```http
DELETE /tasks/{id}
Authorization: Bearer <token>
```

## 🗄️ Database Schema

### Users Table
| Column      | Type         | Constraints                    |
|-------------|--------------|--------------------------------|
| id          | BIGINT       | PRIMARY KEY, AUTO_INCREMENT    |
| username    | VARCHAR(50)  | UNIQUE, NOT NULL               |
| email       | VARCHAR(100) | UNIQUE, NOT NULL               |
| password    | VARCHAR(255) | NOT NULL                       |
| first_name  | VARCHAR(50)  |                                |
| last_name   | VARCHAR(50)  |                                |
| is_active   | BOOLEAN      | DEFAULT TRUE                   |
| role        | VARCHAR(20)  | NOT NULL                       |
| created_at  | TIMESTAMP    | NOT NULL                       |
| updated_at  | TIMESTAMP    |                                |

### Tasks Table
| Column              | Type          | Constraints                    |
|---------------------|---------------|--------------------------------|
| id                  | BIGINT        | PRIMARY KEY, AUTO_INCREMENT    |
| title               | VARCHAR(200)  | NOT NULL                       |
| description         | VARCHAR(2000) |                                |
| status              | VARCHAR(20)   | NOT NULL                       |
| priority            | VARCHAR(20)   | NOT NULL                       |
| deadline            | TIMESTAMP     | NOT NULL                       |
| completed_at        | TIMESTAMP     |                                |
| user_id             | BIGINT        | FOREIGN KEY → users(id)        |
| is_recurring        | BOOLEAN       | DEFAULT FALSE                  |
| recurrence_pattern  | VARCHAR(20)   |                                |
| tags                | VARCHAR(500)  |                                |
| created_at          | TIMESTAMP     | NOT NULL                       |
| updated_at          | TIMESTAMP     |                                |

## ⚙️ Configuration

### Application Properties

#### Development (application.yml)
```yaml
spring:
  datasource:
    url: jdbc:h2:mem:schedulerdb
    driver-class-name: org.h2.Driver
    username: sa
    password:
  
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true

jwt:
  secret: yourSecretKeyMustBeAtLeast256BitsLongForHS256Algorithm
  expiration: 86400000  # 24 hours
```

#### Production
Set environment variables:
```bash
DATABASE_URL=jdbc:postgresql://localhost:5432/schedulerdb
DATABASE_USERNAME=postgres
DATABASE_PASSWORD=yourpassword
JWT_SECRET=yourProductionSecretKey
```

Run with production profile:
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=prod
```

## 🧪 Running Tests

### Run All Tests
```bash
mvn test
```

### Run Specific Test Class
```bash
mvn test -Dtest=TaskServiceTest
```

### Test Coverage
The project includes:
- **Repository Tests**: Data access layer testing
- **Service Tests**: Business logic testing with mocked dependencies
- **Controller Tests**: API endpoint testing
- **Integration Tests**: End-to-end testing

## 🗺️ Development Roadmap

### Upcoming Features (Daily Additions)

#### Week 1
- [ ] Email notifications for task deadlines
- [ ] Task categories/projects
- [ ] Task sharing between users
- [ ] Task comments and notes
- [ ] Task attachments support
- [ ] Advanced search and filtering
- [ ] Task analytics dashboard

#### Week 2
- [ ] Calendar view integration
- [ ] Reminder system
- [ ] Task dependencies
- [ ] Sub-tasks support
- [ ] Task templates
- [ ] Export tasks (CSV, PDF)
- [ ] Bulk operations

#### Week 3
- [ ] REST API rate limiting
- [ ] Task activity logs
- [ ] User preferences
- [ ] Dark mode support
- [ ] Internationalization (i18n)
- [ ] WebSocket for real-time updates
- [ ] Mobile API optimization

#### Week 4
- [ ] AI-powered task suggestions
- [ ] Task time tracking
- [ ] Productivity insights
- [ ] Team collaboration features
- [ ] Integration with external calendars
- [ ] API documentation with Swagger
- [ ] Performance monitoring

## 🔒 Security

- Passwords are encrypted using BCrypt
- JWT tokens expire after 24 hours
- All task endpoints require authentication
- Users can only access their own tasks
- CSRF protection enabled
- SQL injection prevention with JPA

## 🐛 Error Handling

The API returns consistent error responses:

```json
{
  "timestamp": "2025-10-29T10:30:00",
  "status": 404,
  "error": "Not Found",
  "message": "Task not found",
  "path": "/api/v1/tasks/999"
}
```

Validation errors include field-specific messages:
```json
{
  "timestamp": "2025-10-29T10:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/api/v1/tasks",
  "validationErrors": {
    "title": "Title is required",
    "deadline": "Deadline must be in the future"
  }
}
```

## 📝 Best Practices

- Use DTOs for request/response instead of entities
- Implement proper exception handling
- Write tests for all new features
- Follow RESTful conventions
- Use appropriate HTTP status codes
- Document all API changes
- Keep controllers thin, business logic in services
- Use constructor injection over field injection

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📄 License

This project is licensed under the MIT License.

## 👥 Authors

- **Your Name** - Initial work

## 🙏 Acknowledgments

- Spring Boot team for the excellent framework
- JWT library maintainers
- All contributors and testers

## 📞 Support

For support, email your-email@example.com or open an issue in the repository.

---

**Happy Coding! 🚀**