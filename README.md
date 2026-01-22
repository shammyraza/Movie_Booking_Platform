# BookMyShow - Movie Booking Platform

A Spring Boot-based online movie ticket booking platform that caters to both B2B (theatre partners) and B2C (end customers) clients.

## Build & Test Status

- Tests: ✅ All tests passing (64/64) — verified on 2026-01-21


## 📋 Table of Contents
- [Features](#features)
- [Technology Stack](#technology-stack)
- [Database Choice](#database-choice)
- [Architecture & Design Patterns](#architecture--design-patterns)
- [SOLID Principles](#solid-principles)
- [Logging](#logging)
- [Getting Started](#getting-started)
- [API Documentation](#api-documentation)
- [Security](#security)

## ✨ Features

### Implemented Scenarios

#### READ Scenario
- **Browse Theatres**: Browse theatres currently running a selected movie in a town, including show timings by date
  - Filter by movie, city, and date
  - View available seats for each show
  - See pricing information

#### WRITE Scenario
- **Book Tickets**: Book movie tickets by selecting theatre, timing, and preferred seats
  - Select multiple seats
  - Automatic discount calculation:
    - 50% discount on the 3rd ticket
    - 20% discount for afternoon shows
  - Generate unique booking reference
  - Real-time seat availability checking

### Additional Features
- JWT-based authentication and authorization
- User registration and login
- Secure API endpoints
- Global exception handling
- Transaction management
- Sample data initialization

## 🛠 Technology Stack

- **Language**: Java 17
- **Framework**: Spring Boot 3.2.1
- **Security**: Spring Security with JWT
- **Database**: H2 (in-memory) / PostgreSQL
- **ORM**: Spring Data JPA / Hibernate
- **Build Tool**: Maven
- **Additional Libraries**:
  - Lombok (reduce boilerplate)
  - JWT (io.jsonwebtoken)
  - Jakarta Validation

## 💾 Database Choice: SQL vs NoSQL

### Why SQL (Relational Database)?

I chose **SQL database (H2/PostgreSQL)** for this project based on the following reasons:

#### 1. **ACID Compliance**
- Ticket booking requires strong transactional guarantees
- Need to ensure seat availability is consistent across concurrent bookings
- Financial transactions (payments) require atomicity

#### 2. **Complex Relationships**
- Multiple entities with well-defined relationships:
  - Movie → Show (One-to-Many)
  - Theatre → Show (One-to-Many)
  - Show → Seat (One-to-Many)
  - User → Booking (One-to-Many)
  - Booking → Seat (One-to-Many)
- JOINs are efficient for querying related data

#### 3. **Data Integrity**
- Foreign key constraints ensure referential integrity
- Prevent orphaned records
- Maintain data consistency

#### 4. **Query Complexity**
- Complex queries like "Find all shows for a movie in a city on a specific date"
- SQL excels at filtering, sorting, and aggregating structured data
- Need for transaction isolation to handle race conditions

#### 5. **Structured Schema**
- Movie booking domain has a well-defined, stable schema
- Entities have clear attributes that don't change frequently
- Schema migrations are manageable

### When NoSQL Would Be Better?

NoSQL would be suitable if:
- Need to handle unstructured data (user reviews, images)
- Require horizontal scalability across multiple regions
- Schema flexibility is critical
- High write throughput is more important than consistency
- Document-based storage for user profiles

### Hybrid Approach (Recommended for Production)

For a production system, I would recommend a **hybrid approach**:

- **SQL Database**: Core transactional data (bookings, shows, seats)
- **NoSQL (MongoDB)**: User preferences, reviews, ratings
- **Redis**: Caching, session management, seat locking during booking
- **Elasticsearch**: Search functionality for movies, theatres

## 🏗 Architecture & Design Patterns

### 1. **Layered Architecture**
```
Controller Layer → Service Layer → Repository Layer → Database
```
- Clear separation of concerns
- Easy to test and maintain

### 2. **Strategy Pattern**
**Location**: `DiscountStrategy` interface and implementations

```java
public interface DiscountStrategy {
    double calculateDiscount(double totalAmount, int numberOfSeats, boolean isAfternoonShow);
}

@Component
public class CompositeDiscountStrategy implements DiscountStrategy {
    // Implementation that combines multiple discount rules
}
```

**Benefits**:
- Open/Closed Principle: Add new discount strategies without modifying existing code
- Flexible discount calculation
- Easy to test different strategies

### 3. **Repository Pattern**
**Location**: All repository interfaces (`UserRepository`, `ShowRepository`, etc.)

```java
@Repository
public interface ShowRepository extends JpaRepository<Show, Long> {
    List<Show> findShowsByMovieAndCityAndDate(...);
}
```

**Benefits**:
- Abstraction over data access
- Dependency Inversion Principle
- Easy to mock in tests

### 4. **DTO Pattern**
**Location**: `dto` package (`ShowDTO`, `BookingRequest`, `BookingResponse`)

**Benefits**:
- Separation between domain entities and API contracts
- Control over what data is exposed
- Version API responses independently

### 5. **Builder Pattern**
**Location**: JPA entities using Lombok's `@Data`, `@Builder`

**Benefits**:
- Clean object construction
- Immutability where needed
- Readable code

### 6. **Filter Chain Pattern**
**Location**: `JwtAuthenticationFilter`

```java
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(...) {
        // JWT validation and authentication
    }
}
```

**Benefits**:
- Cross-cutting concerns (authentication)
- Clean separation from business logic

## 📐 SOLID Principles

### 1. **Single Responsibility Principle (SRP)**

Each class has one reason to change:

- **`ShowBrowsingServiceImpl`**: Only handles show browsing logic
  ```java
  @Service
  public class ShowBrowsingServiceImpl implements ShowBrowsingService {
      // Only responsible for browsing shows
  }
  ```

- **`BookingServiceImpl`**: Only handles booking logic
- **`JwtUtils`**: Only handles JWT token operations
- **`UserDetailsServiceImpl`**: Only loads user details

### 2. **Open/Closed Principle (OCP)**

Open for extension, closed for modification:

- **`DiscountStrategy`**: Can add new discount strategies without modifying existing code
  ```java
  // Can add new strategies like SeasonalDiscountStrategy, StudentDiscountStrategy
  public class SeasonalDiscountStrategy implements DiscountStrategy {
      // New implementation
  }
  ```

- **Service interfaces**: Can create new implementations without changing existing code

### 3. **Liskov Substitution Principle (LSP)**

Subtypes can replace base types:

- **Repository interfaces**: All extend `JpaRepository`, can be substituted
- **`DiscountStrategy`**: Any implementation can replace another
- **`UserDetailsService`**: Custom implementation can replace default

### 4. **Interface Segregation Principle (ISP)**

Clients shouldn't depend on interfaces they don't use:

- **Separate service interfaces**:
  ```java
  public interface ShowBrowsingService {
      List<ShowDTO> browseShowsByMovieCityAndDate(...);
  }
  
  public interface BookingService {
      BookingResponse bookTickets(...);
  }
  ```

- Controllers only depend on the specific service they need
- No "god interface" with all methods

### 5. **Dependency Inversion Principle (DIP)**

Depend on abstractions, not concretions:

- **Services depend on repository interfaces**, not implementations:
  ```java
  @Service
  @RequiredArgsConstructor
  public class BookingServiceImpl implements BookingService {
      private final BookingRepository bookingRepository;  // Interface, not class
      private final ShowRepository showRepository;        // Interface, not class
      private final DiscountStrategy discountStrategy;    // Interface, not class
  }
  ```

- **Controllers depend on service interfaces**:
  ```java
  @RestController
  @RequiredArgsConstructor
  public class ShowController {
      private final ShowBrowsingService showBrowsingService;  // Interface
  }
  ```

- Spring's dependency injection manages concrete implementations

## � Logging

### Comprehensive Logging Implementation

The application implements **structured logging** using **SLF4J with Logback** across all layers for monitoring, debugging, and audit purposes.

#### Logging Features

✅ **All layers covered**: Controllers, Services, Security, Exception Handlers  
✅ **Multiple log levels**: ERROR, WARN, INFO, DEBUG, TRACE  
✅ **Structured messages**: Consistent format with contextual information  
✅ **Performance-optimized**: Parameterized logging (no string concatenation)  
✅ **File rotation**: Automatic log file management (10MB per file, 30 days retention)  
✅ **SQL query logging**: Hibernate SQL and parameter binding

#### Log Levels Configuration

```yaml
logging:
  level:
    root: INFO                         # Default for all packages
    com.bookmyshow: INFO               # Application logs
    com.bookmyshow.security: DEBUG     # Security operations
    org.hibernate.SQL: DEBUG           # SQL queries
  
  file:
    name: logs/bookmyshow-application.log
    max-size: 10MB
    max-history: 30
```

#### Example Log Output

**API Request:**
```
2026-01-21 10:30:15.123 [http-nio-8080-exec-1] INFO  c.b.controller.ShowController - >>> Incoming Request: GET /api/shows/browse
2026-01-21 10:30:15.124 [http-nio-8080-exec-1] INFO  c.b.controller.ShowController - Parameters - Movie ID: 1, City: Mumbai, Date: 2026-01-21
2026-01-21 10:30:15.234 [http-nio-8080-exec-1] INFO  c.b.controller.ShowController - <<< Response: 8 shows found
```

**Booking Process:**
```
2026-01-21 14:25:30.458 [http-nio-8080-exec-2] INFO  c.b.s.impl.BookingServiceImpl - === Starting Booking Process ===
2026-01-21 14:25:30.459 [http-nio-8080-exec-2] INFO  c.b.s.impl.BookingServiceImpl - User: john, Show ID: 3, Seat IDs: [1, 2, 3]
2026-01-21 14:25:30.510 [http-nio-8080-exec-2] INFO  c.b.s.impl.BookingServiceImpl - Total amount calculated: ₹600.0 for 3 seats
2026-01-21 14:25:30.520 [http-nio-8080-exec-2] INFO  c.b.s.impl.BookingServiceImpl - Discount applied: ₹220.0 (36.67%)
2026-01-21 14:25:31.121 [http-nio-8080-exec-2] INFO  c.b.s.impl.BookingServiceImpl - Booking Reference: BK-1737462331, Final Amount: ₹380.0
```

**Error Handling:**
```
2026-01-21 15:30:45.678 [http-nio-8080-exec-5] ERROR c.b.e.GlobalExceptionHandler - ResourceNotFoundException: Movie not found with id: 999
2026-01-21 11:20:33.456 [http-nio-8080-exec-9] WARN  c.b.e.GlobalExceptionHandler - BadCredentialsException: Failed login attempt
```

#### Key Logging Components

| Component | Purpose | Log Level |
|-----------|---------|-----------|
| **Controllers** | API request/response tracking | INFO |
| **Services** | Business logic execution | INFO/DEBUG |
| **Security** | Authentication, JWT validation | DEBUG |
| **Repositories** | Database operations | DEBUG |
| **Exception Handler** | Error tracking | ERROR/WARN |
| **Data Initializer** | Startup data loading | INFO |

#### Viewing Logs

**Console Output:**
```bash
mvn spring-boot:run
```

**Log File:**
```bash
# View log file (Windows PowerShell)
Get-Content logs\bookmyshow-application.log -Tail 100

# Follow logs in real-time
Get-Content logs\bookmyshow-application.log -Wait
```

**Search Logs:**
```bash
# Find ERROR logs
Get-Content logs\bookmyshow-application.log | Select-String "ERROR"

# Find booking operations
Get-Content logs\bookmyshow-application.log | Select-String "BookingService"
```

📖 **For complete logging documentation, see [LOGGING_GUIDE.md](LOGGING_GUIDE.md)**

## �🚀 Getting Started

### Prerequisites
- Java 17 or higher
- Maven 3.6+

### Installation & Running

1. **Clone the repository**
  ```powershell
  cd Movie_Booking_Platform
  ```

2. **Build the project (runs tests by default)**
  ```powershell
  mvn clean install
  ```

  - This runs the test-suite (all tests currently passing: 64/64).
  - If you need to skip tests (not recommended for CI), add `-DskipTests`.

3. **Run the application**
  ```powershell
  mvn spring-boot:run
  ```

  Or run the packaged JAR:
  ```powershell
  java -jar target\movie-booking-platform-1.0.0.jar
  ```

4. **Access the application**
  - API Base URL: `http://localhost:8080`
  - H2 Console: `http://localhost:8080/h2-console`
    - JDBC URL: `jdbc:h2:mem:bookmyshow`
    - Username: `sa`
    - Password: (leave empty)

### Sample Data

The application automatically initializes with sample data:

**Users:**
- Username: `john`, Password: `password123`, Role: USER
- Username: `admin`, Password: `admin123`, Roles: USER, ADMIN

**Movies:**
- Inception (English, Sci-Fi)
- The Dark Knight (English, Action)
- RRR (Telugu, Action)

**Theatres:**
- PVR Cinemas (Mumbai)
- INOX (Mumbai)
- Cinepolis (Delhi)

**Shows:** Multiple shows per day for each movie-theatre combination

## 📡 API Documentation

### Authentication APIs

#### 1. Register User
```http
POST /api/auth/signup
Content-Type: application/json

{
  "username": "john",
  "email": "john@example.com",
  "password": "password123"
}
```

**Response:**
```json
"User registered successfully!"
```

#### 2. Login
```http
POST /api/auth/login
Content-Type: application/json

{
  "username": "john",
  "password": "password123"
}
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer",
  "id": 1,
  "username": "john",
  "email": "john@example.com",
  "roles": ["USER"]
}
```

### Show Browsing API (READ Scenario)

#### Browse Shows by Movie, City, and Date
```http
GET /api/shows/browse?movieId=1&city=Mumbai&date=2026-01-20
Authorization: Bearer <your-jwt-token>
```

**Response:**
```json
[
  {
    "id": 1,
    "movieId": 1,
    "movieTitle": "Inception",
    "theatreId": 1,
    "theatreName": "PVR Cinemas",
    "theatreCity": "Mumbai",
    "theatreAddress": "Phoenix Mall, Lower Parel",
    "showDateTime": "2026-01-20T10:00:00",
    "basePrice": 200.0,
    "showType": "MORNING",
    "availableSeats": 100
  },
  {
    "id": 2,
    "movieId": 1,
    "movieTitle": "Inception",
    "theatreId": 1,
    "theatreName": "PVR Cinemas",
    "theatreCity": "Mumbai",
    "theatreAddress": "Phoenix Mall, Lower Parel",
    "showDateTime": "2026-01-20T14:00:00",
    "basePrice": 150.0,
    "showType": "AFTERNOON",
    "availableSeats": 100
  }
]
```

### Booking API (WRITE Scenario)

#### Book Tickets
```http
POST /api/bookings
Authorization: Bearer <your-jwt-token>
Content-Type: application/json

{
  "showId": 1,
  "seatIds": [1, 2, 3]
}
```

**Response:**
```json
{
  "bookingId": 1,
  "bookingReference": "BMS-A1B2C3D4",
  "showId": 1,
  "movieTitle": "Inception",
  "theatreName": "PVR Cinemas",
  "showDateTime": "2026-01-20T10:00:00",
  "seatNumbers": ["R1", "R2", "R3"],
  "totalAmount": 500.0,
  "discountApplied": 100.0,
  "status": "CONFIRMED",
  "bookingDateTime": "2026-01-20T09:30:00"
}
```

**Discount Calculation:**
- Base amount: 3 seats × 200 = 600
- 50% discount on 3rd ticket: -100
- Final amount: 500

### Error Responses

```json
{
  "timestamp": "2026-01-20T10:00:00",
  "message": "Movie not found with id: 999",
  "details": "uri=/api/shows/browse",
  "status": 404
}
```

## 🔒 Security

### JWT Authentication

1. **Register or Login** to get a JWT token
2. **Include the token** in subsequent requests:
   ```
   Authorization: Bearer <your-jwt-token>
   ```

### Protected Endpoints

- `/api/shows/browse/**` - Requires authentication
- `/api/bookings/**` - Requires authentication

### Public Endpoints

- `/api/auth/signup` - User registration
- `/api/auth/login` - User login

### Password Security

- Passwords are encrypted using BCrypt
- Never stored in plain text
- Strong password encoding

## 🧪 Testing the Application

### Step-by-step Testing Guide

1. **Start the application**
   ```bash
   mvn spring-boot:run
   ```

2. **Register a new user** (or use sample users)
   ```bash
   curl -X POST http://localhost:8080/api/auth/signup \
     -H "Content-Type: application/json" \
     -d '{"username":"testuser","email":"test@example.com","password":"test123"}'
   ```

3. **Login to get JWT token**
   ```bash
   curl -X POST http://localhost:8080/api/auth/login \
     -H "Content-Type: application/json" \
     -d '{"username":"john","password":"password123"}'
   ```

4. **Browse shows** (use token from step 3)
   ```bash
   curl -X GET "http://localhost:8080/api/shows/browse?movieId=1&city=Mumbai&date=2026-01-20" \
     -H "Authorization: Bearer YOUR_JWT_TOKEN"
   ```

5. **Book tickets** (use token and seat IDs from database)
   ```bash
   curl -X POST http://localhost:8080/api/bookings \
     -H "Authorization: Bearer YOUR_JWT_TOKEN" \
     -H "Content-Type: application/json" \
     -d '{"showId":1,"seatIds":[1,2,3]}'
   ```

## 📊 Database Schema

### Entity Relationships

```
User (1) ──────< (N) Booking
Movie (1) ──────< (N) Show
Theatre (1) ────< (N) Show
Show (1) ───────< (N) Seat
Show (1) ───────< (N) Booking
Booking (1) ────< (N) Seat
```

### Key Tables

- **users**: User account information
- **movies**: Movie details
- **theatres**: Theatre/Cinema information
- **shows**: Movie screenings with date/time
- **seats**: Individual seats for each show
- **bookings**: Ticket bookings

## 🔧 Configuration

### Database Configuration

**H2 (Default - In-Memory)**
```yaml
spring:
  datasource:
    url: jdbc:h2:mem:bookmyshow
    driver-class-name: org.h2.Driver
```

**PostgreSQL (Production)**
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/bookmyshow
    username: your_username
    password: your_password
    driver-class-name: org.postgresql.Driver
```

### JWT Configuration

```yaml
jwt:
  secret: your-secret-key
  expiration: 86400000  # 24 hours
```

## 🧪 Testing

### Running Tests

```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=ShowBrowsingServiceImplTest

# Run tests with coverage report
mvn clean test jacoco:report
```

### Test Coverage

**64 test methods across multiple test classes (all passing):**

- ✅ Controller tests: REST endpoint validation (Auth, Booking, Show controllers)
- ✅ Service tests: Business logic (booking, show browsing, discount strategies)
- ✅ Security tests: JWT utilities and token validation
- ✅ Repository tests: Data access and custom queries

**Detailed test documentation**: See `TEST_DOCUMENTATION.md`

### What's Tested

✅ **Functional Requirements**:
- Browse shows by movie, city, date (READ scenario)
- Book tickets with seat selection (WRITE scenario)
- 50% discount on 3rd ticket
- 20% discount for afternoon shows
- Seat availability validation
- Unique booking reference generation

✅ **Non-Functional Requirements**:
- JWT authentication and authorization
- Input validation
- Error handling and proper HTTP status codes
- Transaction management
- Security integration

✅ **Design Patterns**:
- Strategy Pattern (discount calculation)
- Repository Pattern (data access)
- DTO Pattern (data transfer)

## 📈 Future Enhancements

- Payment gateway integration
- Email notifications
- Seat locking mechanism (temporary hold during booking)
- Movie search and recommendations
- Theatre management APIs (CRUD operations for shows)
- Bulk booking and cancellation
- Rating and review system
- Multi-language support
- Caching with Redis
- Monitoring and logging
- Increase test coverage to 95%+

## 👥 Contributors

- Your Name

## 📄 License

This project is for educational purposes.

---

**Note**: This is a demonstration project for technical assessment purposes. Not for production use without proper security hardening and scalability considerations.
