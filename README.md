# Backend Resource Booking System

A clean, entry-level REST API for a **Resource Booking System** built using Java 21, Spring Boot, Spring Security, JWT, Spring Data JPA, and MySQL.

---

## Technology Stack

* **Java 21**
* **Spring Boot 3.3.3**
* **Spring Security & JSON Web Tokens (JWT)** (using io.jsonwebtoken `jjwt` 0.11.5)
* **Spring Data JPA & Hibernate**
* **MySQL Database**
* **SpringDoc OpenAPI (Swagger UI)**
* **JUnit 5, Mockito & MockMvc** for unit and integration testing
* **H2 Database** (in-memory, test-scoped for running tests out-of-the-box)

---

## Core Features

1. **JWT-Based Authentication**:
   - Acquire tokens securely via `POST /auth/login` (BCrypt hashed passwords).
   - Identity context is derived directly from the JWT.
2. **Role-Based Access Control**:
   - `ADMIN` role: Full resource CRUD and reservation management.
   - `USER` role: Read-only resources and reservation creation. Can view only their own reservations.
3. **Reservation Filtering & Paging**:
   - Search parameters (status, minPrice, maxPrice).
   - Server-side pagination (`page`, `size`) and sorting parameters.
4. **Data Validations & Exception Handling**:
   - Field validations (positive prices, valid statuses, required fields).
   - Custom business rules (start time must be before end time).
   - Uniform JSON error responses.

---

## Seed User Credentials

On startup, a CommandLineRunner seeds the database automatically if no users are present:

* **Admin User**:
  - **Email**: `admin@example.com`
  - **Password**: `AdminPassword123`
* **Standard User**:
  - **Email**: `user@example.com`
  - **Password**: `UserPassword123`

---

## Database Configuration

The application is configured to connect to MySQL on `localhost:3306` and automatically create the database `booking_system` if it does not already exist.

You can modify these configurations in the configuration file:
* **Production Configurations**: [`src/main/resources/application.properties`](src/main/resources/application.properties)
* **Testing Configurations**: [`src/test/resources/application.properties`](src/test/resources/application.properties)

### Configuration Variables

You can supply these variables in `application.properties` or set them as environment variables:

| Property | Default Value | Description |
|---|---|---|
| `spring.datasource.url` | `jdbc:mysql://localhost:3306/booking_system?createDatabaseIfNotExist=true` | JDBC URL for database connection |
| `spring.datasource.username` | `root` | Database username |
| `spring.datasource.password` | `(empty)` | Database password |
| `jwt.secret` | `mySecretKeyForBookingSystemMustBeAtLeast256BitsLongAndSecure!` | JWT Secret Key (min 256 bits) |
| `jwt.expiration` | `86400000` | Token validity duration in milliseconds (24 hours) |

---

## API Documentation

We use Swagger UI to provide interactive API documentation. When the application is running, navigate to:

👉 **[http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)**

### How to use Swagger UI with Authentication:
1. Call the `POST /auth/login` endpoint with a seed user's email and password.
2. Copy the `token` value returned in the JSON response.
3. Click the **Authorize** button at the top right of the Swagger page.
4. Enter the token in the format: `Bearer <token>` (e.g. `Bearer eyJhbGciOi...`).
5. Click **Authorize** and close the dialog. You can now execute protected endpoints.

---

## API Endpoints List

### 1. Authentication
* `POST /auth/login` - Public. Validates credentials and returns JWT token.

### 2. Resources CRUD
* `GET /resources` - USER and ADMIN. Retrieve all resources.
* `GET /resources/{id}` - USER and ADMIN. Retrieve a resource by ID.
* `POST /resources` - ADMIN only. Create a new resource.
* `PUT /resources/{id}` - ADMIN only. Update a resource.
* `DELETE /resources/{id}` - ADMIN only. Delete a resource.

### 3. Reservations CRUD
* `POST /reservations` - USER and ADMIN. Create a reservation. (Checks resource existence, validates start/end times. Identity is extracted directly from the token).
* `GET /reservations` - USER and ADMIN. Retrieve list of reservations with pagination, sorting, and filters:
  - **Filters**: `status`, `minPrice`, `maxPrice`
  - **Paging**: `page` (default 0), `size` (default 10)
  - **Sorting**: `sortBy` (default `id`), `sortDir` (default `asc` or `desc`)
  - *Enforcement: Users only see their own reservations, Admins see all.*
* `GET /reservations/{id}` - USER and ADMIN. Get reservation by ID. (Users can only get their own reservations, Admins can get any).
* `PUT /reservations/{id}` - ADMIN only. Update a reservation.
* `DELETE /reservations/{id}` - ADMIN only. Delete a reservation.

---

## How to Build and Run the Application

You do not need to install Maven globally. A portable copy of Maven is supplied or can be used.

### Build the Project
To compile the code and package it, run:
```bash
mvn clean package
```

### Run Tests
To run unit and integration tests (which execute using H2 database automatically), run:
```bash
mvn test
```

### Run the Application locally
To start the application on port `8080`, run:
```bash
mvn spring-boot:run
```
*(Make sure your MySQL database is running on port 3306 with the configured credentials before running).*

---

## How to Test the APIs manually

You can test the API using **Postman** or **cURL**:

### 1. User Login
```bash
curl -X POST http://localhost:8080/auth/login \
     -H "Content-Type: application/json" \
     -d '{"email":"user@example.com", "password":"UserPassword123"}'
```
Response:
```json
{
  "token": "eyJhbGciOi...",
  "email": "user@example.com",
  "role": "USER"
}
```

### 2. Create Resource (Requires Admin Token)
```bash
curl -X POST http://localhost:8080/resources \
     -H "Authorization: Bearer <ADMIN_JWT_TOKEN>" \
     -H "Content-Type: application/json" \
     -d '{"name":"Conference Room A", "description":"Executive conference room"}'
```

### 3. Create Reservation (Requires User Token)
*Note: The reservation matches the resource created. The user identity is automatically mapped from the JWT token. No `userId` is passed.*
```bash
curl -X POST http://localhost:8080/reservations \
     -H "Authorization: Bearer <USER_JWT_TOKEN>" \
     -H "Content-Type: application/json" \
     -d '{
       "resourceId": 1,
       "startTime": "2026-09-01T10:00:00",
       "endTime": "2026-09-01T12:00:00",
       "price": 150.00,
       "status": "PENDING"
     }'
```

### 4. Fetch Reservations (Enforces Ownership filtering)
```bash
curl -X GET "http://localhost:8080/reservations?status=PENDING&minPrice=100.00&page=0&size=10&sortBy=price&sortDir=desc" \
     -H "Authorization: Bearer <USER_JWT_TOKEN>"
```
