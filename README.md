# SeetBelt

A complete Spring Boot application with JWT-based authentication system supporting role-based access control (ADMIN and CHILD roles).

## Features

- User registration with role assignment
- JWT-based authentication
- Role-based access control (ADMIN, CHILD)
- Password encryption using BCrypt
- Input validation
- Global exception handling
- Standardized API responses
- PostgreSQL database integration
- **Interactive API documentation with Swagger/OpenAPI**

## Technology Stack

- **Java**: 17
- **Spring Boot**: 3.2.0
- **Database**: PostgreSQL
- **Build Tool**: Maven
- **Security**: Spring Security with JWT
- **Validation**: Bean Validation (Jakarta Validation)
- **API Documentation**: SpringDoc OpenAPI (Swagger UI)

## Prerequisites

- Java 17 or higher
- Maven 3.6+
- PostgreSQL 12+
- IDE (IntelliJ IDEA, Eclipse, or VS Code)

## Setup Instructions

### 1. Database Setup

1. Install PostgreSQL if not already installed
2. Create a new database:
   ```sql
   CREATE DATABASE lms_db;
   ```

3. Update database credentials in `src/main/resources/application.properties` or `application.yml`:
   ```properties
   spring.datasource.url=jdbc:postgresql://localhost:5432/lms_db
   spring.datasource.username=your_username
   spring.datasource.password=your_password
   ```

### 2. JWT Configuration

The JWT secret key is configured in `application.properties`. For production, use a strong, randomly generated secret key:

```properties
jwt.secret=your-secret-key-here
jwt.expiration=86400000  # 24 hours in milliseconds
```

### 3. Build and Run

1. Clone or navigate to the project directory:
   ```bash
   cd /path/to/LMS
   ```

2. Build the project:
   ```bash
   mvn clean install
   ```

3. Run the application:
   ```bash
   mvn spring-boot:run
   ```

   Or run the main class `LmsApplication` from your IDE.

4. The application will start on `http://localhost:8080`

## Swagger/OpenAPI Documentation

The application includes interactive API documentation powered by SpringDoc OpenAPI (Swagger UI).

### Accessing Swagger UI

Once the application is running, you can access the Swagger UI at:

```
http://localhost:8080/swagger-ui.html
```

Or alternatively:

```
http://localhost:8080/swagger-ui/index.html
```

### API Documentation JSON

The OpenAPI specification is available at:

```
http://localhost:8080/v3/api-docs
```

### Features

- **Interactive API Testing**: Test endpoints directly from the Swagger UI
- **JWT Authentication**: Use the "Authorize" button to add your JWT token for protected endpoints
- **Request/Response Examples**: View example requests and responses for each endpoint
- **Schema Documentation**: Detailed schema information for all DTOs
- **Try It Out**: Execute API calls directly from the documentation

### Using JWT Token in Swagger

1. First, register or login using the `/api/v1/auth/register` or `/api/v1/auth/login` endpoints
2. Copy the JWT token from the response
3. Click the "Authorize" button (🔒) at the top of the Swagger UI
4. Enter your token in the format: `Bearer <your-token>` (include the "Bearer " prefix)
5. Click "Authorize" and then "Close"
6. Now you can test protected endpoints with authentication

## API Documentation

All API endpoints are prefixed with `/api/v1/`

### Base URL
```
http://localhost:8080/api/v1
```

### Authentication Endpoints

#### 1. Register User

**Endpoint:** `POST /api/v1/auth/register`

**Description:** Register a new user with role assignment.

**Request Body:**
```json
{
  "username": "john_doe",
  "email": "john.doe@example.com",
  "password": "SecurePass123",
  "role": "ADMIN"
}
```

**Validation Rules:**
- `username`: Required, minimum 3 characters, alphanumeric only
- `email`: Required, valid email format
- `password`: Required, minimum 8 characters, must contain at least one uppercase letter, one lowercase letter, and one digit
- `role`: Required, must be either "ADMIN" or "CHILD"

**Success Response (201 Created):**
```json
{
  "success": true,
  "message": "User registered successfully",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "tokenType": "Bearer",
    "id": "123e4567-e89b-12d3-a456-426614174000",
    "username": "john_doe",
    "email": "john.doe@example.com",
    "role": "ADMIN"
  },
  "timestamp": "2024-01-15T10:30:00"
}
```

**Error Response (400 Bad Request):**
```json
{
  "success": false,
  "message": "Validation failed",
  "data": {
    "username": "Username must be at least 3 characters",
    "password": "Password must contain at least one uppercase letter, one lowercase letter, and one digit"
  },
  "timestamp": "2024-01-15T10:30:00"
}
```

#### 2. User Login

**Endpoint:** `POST /api/v1/auth/login`

**Description:** Authenticate user and receive JWT token.

**Request Body:**
```json
{
  "usernameOrEmail": "john_doe",
  "password": "SecurePass123"
}
```

**Validation Rules:**
- `usernameOrEmail`: Required (can be username or email)
- `password`: Required

**Success Response (200 OK):**
```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "tokenType": "Bearer",
    "id": "123e4567-e89b-12d3-a456-426614174000",
    "username": "john_doe",
    "email": "john.doe@example.com",
    "role": "ADMIN"
  },
  "timestamp": "2024-01-15T10:30:00"
}
```

**Error Response (401 Unauthorized):**
```json
{
  "success": false,
  "message": "Invalid username/email or password",
  "timestamp": "2024-01-15T10:30:00"
}
```

### Using the JWT Token

After successful login or registration, include the JWT token in the Authorization header for protected endpoints:

```
Authorization: Bearer <your-jwt-token>
```

## Project Structure

```
src/
├── main/
│   ├── java/com/lms/
│   │   ├── controller/          # REST controllers
│   │   │   └── AuthController.java
│   │   ├── dto/                 # Data Transfer Objects
│   │   │   ├── request/         # Request DTOs
│   │   │   └── response/        # Response DTOs
│   │   ├── entity/              # JPA entities
│   │   │   ├── User.java
│   │   │   └── Role.java
│   │   ├── exception/           # Exception handlers
│   │   │   └── GlobalExceptionHandler.java
│   │   ├── repository/          # Data access layer
│   │   │   └── UserRepository.java
│   │   ├── security/            # Security configuration
│   │   │   ├── SecurityConfig.java
│   │   │   └── JwtAuthenticationFilter.java
│   │   ├── service/             # Business logic layer
│   │   │   ├── AuthService.java
│   │   │   └── UserService.java
│   │   ├── util/                # Utility classes
│   │   │   └── JwtUtil.java
│   │   └── LmsApplication.java  # Main application class
│   └── resources/
│       ├── application.properties
│       ├── application.yml
│       └── db/migration/        # Database scripts
│           └── V1__init_schema.sql
├── test/                        # Test files (to be added)
└── pom.xml                      # Maven configuration
```

## Security Features

- **Password Encryption**: All passwords are hashed using BCrypt before storage
- **JWT Authentication**: Stateless authentication using JSON Web Tokens
- **Role-Based Access Control**: Support for ADMIN and CHILD roles
- **Token Expiration**: Configurable token expiration (default: 24 hours)
- **Input Validation**: Comprehensive validation using Bean Validation annotations
- **SQL Injection Protection**: Using JPA/Hibernate parameterized queries

## Configuration

### Application Properties

Key configuration options in `application.properties`:

- `spring.datasource.*`: Database connection settings
- `jwt.secret`: Secret key for JWT token signing
- `jwt.expiration`: Token expiration time in milliseconds
- `server.port`: Server port (default: 8080)
- `logging.level.*`: Logging configuration

## Testing the API

### Using cURL

**Register a new user:**
```bash
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "email": "test@example.com",
    "password": "TestPass123",
    "role": "CHILD"
  }'
```

**Login:**
```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "usernameOrEmail": "testuser",
    "password": "TestPass123"
  }'
```

**Access protected endpoint (example):**
```bash
curl -X GET http://localhost:8080/api/v1/protected-endpoint \
  -H "Authorization: Bearer <your-jwt-token>"
```

### Using Postman

1. Import the collection (create manually):
   - POST `/api/v1/auth/register`
   - POST `/api/v1/auth/login`

2. For protected endpoints, add the JWT token in the Authorization header:
   - Type: Bearer Token
   - Token: `<your-jwt-token>`

## Error Handling

The application uses a global exception handler that returns standardized error responses:

- **400 Bad Request**: Validation errors or invalid input
- **401 Unauthorized**: Authentication failures
- **404 Not Found**: Resource not found
- **500 Internal Server Error**: Unexpected server errors

All errors follow the standard API response format:
```json
{
  "success": false,
  "message": "Error description",
  "data": { /* optional error details */ },
  "timestamp": "2024-01-15T10:30:00"
}
```

## Database Schema

### Users Table

| Column      | Type      | Constraints           |
|-------------|-----------|-----------------------|
| id          | UUID      | Primary Key           |
| username    | VARCHAR   | Unique, Not Null      |
| email       | VARCHAR   | Unique, Not Null      |
| password    | VARCHAR   | Not Null              |
| role        | VARCHAR   | Not Null (ADMIN/CHILD)|
| is_active   | BOOLEAN   | Default: true         |
| created_at  | TIMESTAMP | Not Null              |
| updated_at  | TIMESTAMP | Not Null              |

## Development Notes

- The application uses Hibernate's `ddl-auto=update` for automatic schema updates
- For production, consider using Flyway or Liquibase for database migrations
- JWT secret should be stored securely (environment variables, secrets manager) in production
- Consider implementing token refresh mechanism for better user experience
- Add rate limiting for authentication endpoints to prevent brute force attacks

## Future Enhancements

- [ ] Token refresh endpoint
- [ ] Password reset functionality
- [ ] Email verification
- [ ] Two-factor authentication (2FA)
- [ ] User profile management endpoints
- [ ] Role management endpoints
- [ ] Audit logging
- [ ] API rate limiting
- [x] Swagger/OpenAPI documentation ✓
- [ ] Unit and integration tests

## License

This project is provided as-is for educational and development purposes.

## Support

For issues or questions, please refer to the Spring Boot documentation or create an issue in the project repository.
