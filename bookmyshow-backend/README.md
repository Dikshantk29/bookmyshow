# BookMyShow Clone — Backend (Spring Boot)

Online movie ticket booking REST API: browse movies, pick a show, select seats, hold them, pay, get a ticket.

## Tech stack
Java 17 · Spring Boot 3.2 · Spring Security + JWT · Spring Data JPA (Hibernate) · H2 (default) / MySQL · Maven · Lombok

## Highlights (good for interviews)
- **JWT authentication** with role-based access (USER / ADMIN), BCrypt password hashing
- **Concurrency-safe seat booking** — `PESSIMISTIC_WRITE` row locks + `@Version` optimistic locking + unique constraint, so two users can never book the same seat
- **Seat hold with expiry** — seats are locked for 5 minutes (PENDING booking); a `@Scheduled` job releases expired holds
- **Booking lifecycle**: PENDING → CONFIRMED / CANCELLED / EXPIRED, with a mock payment step and a "no cancel within 2h" rule
- Layered architecture (controller → service → repository), DTO records, validation, global exception handler

## Run
```bash
mvn spring-boot:run                                   # in-memory H2, demo data auto-seeded
mvn spring-boot:run -Dspring-boot.run.profiles=mysql  # MySQL (edit credentials in application.yml)
```
Server: http://localhost:8080 · Demo logins: `admin@bms.com / admin123`, `user@bms.com / user123`

## API
| Method | Endpoint | Auth | Description |
|---|---|---|---|
| POST | /api/auth/register | – | Create account, returns JWT |
| POST | /api/auth/login | – | Login, returns JWT |
| GET | /api/movies?q= | – | List / search movies |
| GET | /api/movies/{id} | – | Movie details |
| GET | /api/movies/{id}/shows?city=&date=yyyy-MM-dd | – | Shows for a movie |
| GET | /api/theatres?city= | – | Theatres |
| GET | /api/shows/{id}/seats | – | Seat map (AVAILABLE / LOCKED / BOOKED) |
| POST | /api/bookings/lock | USER | Hold seats `{showId, seatNumbers:["A1","A2"]}` |
| POST | /api/bookings/{id}/pay | USER | Mock payment → confirm booking |
| POST | /api/bookings/{id}/cancel | USER | Cancel booking, release seats |
| GET | /api/bookings/my | USER | Booking history |
| GET | /api/bookings/{id} | USER | Single booking (ticket) |
| POST | /api/admin/movies | ADMIN | Add movie |
| POST | /api/admin/theatres | ADMIN | Add theatre |
| POST | /api/admin/shows | ADMIN | Add show (80 seats auto-generated) |

Send the token as `Authorization: Bearer <token>`.

## Project structure
```
config/      SecurityConfig (JWT, CORS), DataSeeder
security/    JwtUtil, JwtFilter
entity/      User, Movie, Theatre, Show, ShowSeat, Booking
repository/  Spring Data JPA repos
service/     Auth, Movie, Show, Booking (business logic)
controller/  REST controllers
exception/   ApiException + GlobalExceptionHandler
```

## Ideas to extend
Razorpay/Stripe integration · email ticket · Redis seat locks · Swagger/OpenAPI · Docker · unit tests with Mockito
