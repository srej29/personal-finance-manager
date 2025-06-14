# Personal Finance Manager

A comprehensive web-based personal finance management system built with Spring Boot that enables users to track income, expenses, savings goals, and generate detailed financial reports.

## Features

- **User Authentication**: Secure registration and login with session-based authentication
- **Transaction Management**: Full CRUD operations for financial transactions
- **Category Management**: Default and custom categories for organizing transactions
- **Savings Goals**: Create and track progress towards financial goals
- **Financial Reports**: Income/expense summaries and spending analysis
- **Data Security**: Complete data isolation between users

## Technology Stack

- **Programming Language**: Java 17
- **Framework**: Spring Boot 3.5.0
- **Security**: Spring Security with session-based authentication
- **Database**: H2 (in-memory for development)
- **Build Tool**: Gradle
- **Testing**: JUnit 5, Mockito
- **Documentation**: JavaDoc

## Prerequisites

- Java 17 or higher
- Gradle 7.x or higher (or use included Gradle wrapper)

## Setup Instructions

### 1. Clone the Repository
```bash
git clone https://github.com/YOUR_USERNAME/personal-finance-manager.git
cd personal-finance-manager
```

### 2. Build the Application
```bash
./gradlew build
```

### 3. Run Tests
```bash
./gradlew test
```

### 4. Start the Application
```bash
./gradlew bootRun
```

The application will be available at `http://localhost:8080`

### 5. Access H2 Database Console (Development)
- URL: `http://localhost:8080/h2-console`
- JDBC URL: `jdbc:h2:mem:testdb`
- Username: `sa`
- Password: (empty)

## API Documentation

### Base URL
- Local: `http://localhost:8080/api`
- Production: `https://your-app.onrender.com/api`

### Authentication Endpoints

#### Register User
```http
POST /api/auth/register
Content-Type: application/json

{
  "username": "user@example.com",
  "password": "securePassword123",
  "fullName": "John Doe",
  "phoneNumber": "+1234567890"
}
```

#### Login
```http
POST /api/auth/login
Content-Type: application/json

{
  "username": "user@example.com",
  "password": "securePassword123"
}
```

#### Logout
```http
POST /api/auth/logout
```

### Transaction Endpoints

#### Create Transaction
```http
POST /api/transactions
Content-Type: application/json

{
  "amount": 1500.00,
  "date": "2024-01-15",
  "categoryName": "Salary",
  "description": "Monthly salary"
}
```

#### Get All Transactions
```http
GET /api/transactions
GET /api/transactions?startDate=2024-01-01&endDate=2024-01-31&categoryName=Food
```

#### Get Transaction by ID
```http
GET /api/transactions/{id}
```

#### Update Transaction
```http
PUT /api/transactions/{id}
Content-Type: application/json

{
  "amount": 1600.00,
  "categoryName": "Salary",
  "description": "Updated salary"
}
```

#### Delete Transaction
```http
DELETE /api/transactions/{id}
```

### Category Endpoints

#### Get All Categories
```http
GET /api/categories
```

#### Create Custom Category
```http
POST /api/categories
Content-Type: application/json

{
  "name": "Gaming",
  "type": "EXPENSE"
}
```

#### Delete Custom Category
```http
DELETE /api/categories/{name}
```

### Savings Goals Endpoints

#### Create Goal
```http
POST /api/goals
Content-Type: application/json

{
  "goalName": "Emergency Fund",
  "targetAmount": 5000.00,
  "targetDate": "2025-12-31",
  "startDate": "2024-01-01",
  "currentProgress": 1000.00
}
```

#### Get All Goals
```http
GET /api/goals
```

#### Get Goal by ID
```http
GET /api/goals/{id}
```

#### Update Goal
```http
PUT /api/goals/{id}
Content-Type: application/json

{
  "targetAmount": 6000.00,
  "currentProgress": 1500.00
}
```

#### Delete Goal
```http
DELETE /api/goals/{id}
```

### Reports Endpoints

#### Income/Expense Summary
```http
GET /api/reports/summary?startDate=2024-01-01&endDate=2024-01-31
```

#### Spending Analysis by Category
```http
GET /api/reports/spending-by-category?startDate=2024-01-01&endDate=2024-01-31
```

## Design Decisions

### Architecture
- **Layered Architecture**: Controller → Service → Repository pattern for clear separation of concerns
- **DTOs**: Separate request/response objects from entities for better API design
- **Global Exception Handling**: Centralized error handling with appropriate HTTP status codes

### Security
- **Session-based Authentication**: Uses HTTP sessions with secure cookies
- **Data Isolation**: All operations ensure users can only access their own data
- **Input Validation**: Comprehensive validation using Bean Validation annotations
- **CSRF Protection**: Enabled for state-changing operations

### Database Design
- **Default Categories**: Pre-populated categories that cannot be modified
- **Custom Categories**: User-created categories with proper isolation
- **Soft Constraints**: Business logic prevents deletion of categories with existing transactions

### Testing Strategy
- **Unit Tests**: Comprehensive service and repository layer testing
- **Integration Tests**: Controller testing with MockMvc
- **Test Coverage**: 80%+ code coverage with meaningful test cases
- **Mocking**: Proper use of Mockito for external dependencies

## Error Handling

The API returns appropriate HTTP status codes:
- `200 OK`: Successful GET, PUT operations
- `201 Created`: Successful POST operations
- `400 Bad Request`: Validation errors, malformed input
- `401 Unauthorized`: Authentication required
- `403 Forbidden`: Access denied, CSRF token missing
- `404 Not Found`: Resource not found
- `409 Conflict`: Duplicate data conflicts

Error responses include descriptive messages:
```json
{
  "message": "Error description",
  "userId": null
}
```

## Default Categories

The system provides these default categories:

**Income:**
- Salary

**Expenses:**
- Food
- Rent
- Transportation
- Entertainment
- Healthcare
- Utilities

## Testing

### Run All Tests
```bash
./gradlew test
```

### Test Coverage Report
```bash
./gradlew jacocoTestReport
```
Report available at: `build/reports/jacoco/test/html/index.html`

### Test Categories
- **Entity Tests**: Validate domain objects and relationships
- **Repository Tests**: Test data access layer with H2 database
- **Service Tests**: Business logic testing with mocked dependencies
- **Controller Tests**: API endpoint testing with MockMvc
- **DTO Tests**: Validation and serialization testing

## Deployment

### Local Development
```bash
./gradlew bootRun
```

### Production Deployment (Render)
1. Push code to GitHub
2. Connect Render to GitHub repository
3. Configure build settings:
    - Build Command: `./gradlew build`
    - Start Command: `java -jar build/libs/*.jar`
4. Deploy and test with provided test script

### Environment Configuration
- **Development**: H2 in-memory database
- **Production**: Can be configured for PostgreSQL or MySQL

## Project Structure

```
src/
├── main/java/com/finance/
│   ├── auth/                 # Authentication controllers and DTOs
│   ├── category/            # Category management
│   ├── exception/           # Custom exceptions
│   ├── goal/               # Savings goals management
│   ├── report/             # Financial reports
│   ├── transaction/        # Transaction management
│   └── user/               # User management
├── main/resources/
│   ├── application.properties
│   └── application-test.properties
└── test/java/              # Comprehensive test suite
```

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make changes with appropriate tests
4. Ensure all tests pass
5. Submit a pull request

## License

This project is developed as part of a software engineering assignment.

## Contact

For questions or issues, please contact the development team.