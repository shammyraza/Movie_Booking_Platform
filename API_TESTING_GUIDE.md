# API Testing Guide - BookMyShow Platform

This document provides sample API requests for testing the BookMyShow platform.

## Prerequisites
- Application running on `http://localhost:8080`
- Tool: Postman, cURL, or any REST client

## 📝 Test Scenarios

### Scenario 1: User Registration and Login

#### Step 1: Register a New User
```http
POST http://localhost:8080/api/auth/signup
Content-Type: application/json

{
  "username": "testuser",
  "email": "testuser@example.com",
  "password": "password123"
}
```

**Expected Response:**
```
User registered successfully!
```

#### Step 2: Login with Registered User
```http
POST http://localhost:8080/api/auth/login
Content-Type: application/json

{
  "username": "testuser",
  "password": "password123"
}
```

**Expected Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "type": "Bearer",
  "id": 3,
  "username": "testuser",
  "email": "testuser@example.com",
  "roles": ["USER"]
}
```

**Save the `token` value for subsequent requests!**

---

### Scenario 2: Browse Shows (READ Scenario)

#### Get Shows for a Movie in a City on Specific Date

```http
GET http://localhost:8080/api/shows/browse?movieId=1&city=Mumbai&date=2026-01-20
Authorization: Bearer <your-token-here>
```

**Query Parameters:**
- `movieId`: 1 (Inception)
- `city`: Mumbai
- `date`: 2026-01-20 (today's date or any future date)

**Expected Response:**
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

---

### Scenario 3: Book Tickets (WRITE Scenario)

#### Step 1: Find Available Seats

First, check the H2 console or use the database to find seat IDs:
```
http://localhost:8080/h2-console
```

Query to get seat IDs:
```sql
SELECT * FROM SEATS WHERE SHOW_ID = 1 AND STATUS = 'AVAILABLE' LIMIT 3;
```

#### Step 2: Book Three Tickets (with 50% discount on 3rd ticket)

```http
POST http://localhost:8080/api/bookings
Authorization: Bearer <your-token-here>
Content-Type: application/json

{
  "showId": 1,
  "seatIds": [1, 2, 3]
}
```

**Expected Response:**
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
  "bookingDateTime": "2026-01-20T09:45:30.123"
}
```

**Discount Calculation:**
- 3 regular seats @ 200 each = 600
- 50% discount on 3rd ticket = -100
- Final amount = 500

#### Step 3: Book Afternoon Show (20% discount)

```http
POST http://localhost:8080/api/bookings
Authorization: Bearer <your-token-here>
Content-Type: application/json

{
  "showId": 2,
  "seatIds": [101, 102]
}
```

**Expected Response:**
```json
{
  "bookingId": 2,
  "bookingReference": "BMS-X5Y6Z7A8",
  "showId": 2,
  "movieTitle": "Inception",
  "theatreName": "PVR Cinemas",
  "showDateTime": "2026-01-20T14:00:00",
  "seatNumbers": ["R1", "R2"],
  "totalAmount": 240.0,
  "discountApplied": 60.0,
  "status": "CONFIRMED",
  "bookingDateTime": "2026-01-20T10:15:45.789"
}
```

**Discount Calculation:**
- 2 regular seats @ 150 each = 300
- 20% afternoon discount = -60
- Final amount = 240

---

## 🧪 Edge Cases to Test

### 1. Invalid Movie ID
```http
GET http://localhost:8080/api/shows/browse?movieId=999&city=Mumbai&date=2026-01-20
Authorization: Bearer <your-token>
```

**Expected Response (404):**
```json
{
  "timestamp": "2026-01-20T10:00:00",
  "message": "Movie not found with id: 999",
  "details": "uri=/api/shows/browse",
  "status": 404
}
```

### 2. Book Already Booked Seats
```http
POST http://localhost:8080/api/bookings
Authorization: Bearer <your-token>
Content-Type: application/json

{
  "showId": 1,
  "seatIds": [1, 2, 3]
}
```

**Expected Response (400):**
```json
{
  "timestamp": "2026-01-20T10:30:00",
  "message": "Seats not available: R1, R2, R3",
  "details": "uri=/api/bookings",
  "status": 400
}
```

### 3. Unauthorized Access (No Token)
```http
GET http://localhost:8080/api/shows/browse?movieId=1&city=Mumbai&date=2026-01-20
```

**Expected Response (401 or 403):**
```
Unauthorized
```

### 4. Invalid Credentials
```http
POST http://localhost:8080/api/auth/login
Content-Type: application/json

{
  "username": "wronguser",
  "password": "wrongpassword"
}
```

**Expected Response (401):**
```json
{
  "timestamp": "2026-01-20T10:00:00",
  "message": "Invalid username or password",
  "details": "uri=/api/auth/login",
  "status": 401
}
```

---

## 📊 Sample Data Reference

### Users (Pre-loaded)
1. **Username:** `john`, **Password:** `password123`, **Role:** USER
2. **Username:** `admin`, **Password:** `admin123`, **Roles:** USER, ADMIN

### Movies
1. **ID:** 1, **Title:** Inception, **Language:** English, **Genre:** Sci-Fi
2. **ID:** 2, **Title:** The Dark Knight, **Language:** English, **Genre:** Action
3. **ID:** 3, **Title:** RRR, **Language:** Telugu, **Genre:** Action

### Theatres
1. **ID:** 1, **Name:** PVR Cinemas, **City:** Mumbai
2. **ID:** 2, **Name:** INOX, **City:** Mumbai
3. **ID:** 3, **Name:** Cinepolis, **City:** Delhi

### Show Types & Discounts
- **MORNING** (10:00 AM): No discount
- **AFTERNOON** (2:00 PM): 20% discount
- **EVENING** (6:30 PM): No discount
- **NIGHT** (9:30 PM): No discount
- **3rd Ticket**: 50% discount (applicable to all shows)

---

## 🔧 cURL Commands

### Register User
```bash
curl -X POST http://localhost:8080/api/auth/signup \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","email":"test@example.com","password":"password123"}'
```

### Login
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"john","password":"password123"}'
```

### Browse Shows
```bash
curl -X GET "http://localhost:8080/api/shows/browse?movieId=1&city=Mumbai&date=2026-01-20" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### Book Tickets
```bash
curl -X POST http://localhost:8080/api/bookings \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"showId":1,"seatIds":[1,2,3]}'
```

---

## 📝 Notes

1. Replace `<your-token-here>` with the actual JWT token received from login
2. Replace `YOUR_JWT_TOKEN` in cURL commands with the actual token
3. Seat IDs can be found by querying the H2 console
4. Dates should be in ISO format: `yyyy-MM-dd`
5. Make sure the application is running before testing

---

## 🎯 Testing Checklist

- [ ] User can register successfully
- [ ] User can login and receive JWT token
- [ ] User can browse shows with valid parameters
- [ ] User can book tickets with available seats
- [ ] 50% discount is applied on 3rd ticket
- [ ] 20% discount is applied for afternoon shows
- [ ] Booking fails when seats are already booked
- [ ] API returns proper error messages
- [ ] Unauthorized access is blocked
- [ ] Invalid credentials are rejected

---

**Happy Testing! 🚀**
