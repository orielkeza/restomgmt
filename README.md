# RestoManagement

A full-stack restaurant management system. The backend is Spring Boot + PostgreSQL and the frontend is React + TypeScript. Customers can register, browse the menu, build a cart, place and pay for orders, and track them. Staff and admins manage the menu, move orders through their status flow, assign delivery riders, and manage user accounts. Everything is behind JWT auth with role-based access control.

The project started from a requirements brief (receptionist-to-CEO restaurant management) during my internship at Wys Ltd. I didn't get through all of it; see [What I Didn't Finish](#known-limitations--what-i-didnt-finish).

> **Note:** The most recent work (cart, ordering, payments, email, and the connected frontend) is on the `19connectFrontendBackend` branch and hasn't been merged into `main` yet.

---

## Motivation

After building a small student registration CRUD API, I wanted to find out what a "real" backend actually involves. This project was where I learned security (JWT, RBAC), multi-environment config with Spring profiles, Docker, a proper testing setup per layer, third-party payment integration, and connecting a React frontend to my own API. It was also my first time having to go back and **stabilize and refactor a codebase I'd already written**, which ended up teaching me more than building it did.

---

## Features

- **Authentication**: register/login with JWT, email verification (with resend), and forgot/reset password by email
- **Password rules**: custom `@ValidPassword` validator (uppercase, number, special character, and the password can't contain the username)
- **Role-based access control**: three roles, `ROLE_USER` (customers), `ROLE_STAFF`, and `ROLE_ADMIN`, with a role hierarchy (ADMIN > STAFF > USER). Users, roles, and permissions are separate entities, and endpoints are locked down with `@PreAuthorize`
- **User management (admin)**: create staff/admin accounts (a temporary password is generated with `SecureRandom` and emailed to the new user), assign and remove roles, update and delete users
- **Menu management**: categories and menu items. Staff can add, edit, and toggle availability; only admins can delete. Browsing the menu is public
- **Cart**: add, update quantity, remove, and clear. A scheduled job runs every night and empties carts that haven't been touched in 3 months
- **Ordering**: place an order from the cart. Each order item saves `priceAtOrder`, so later menu price changes don't rewrite old orders. Status flow: `PENDING → CONFIRMED → PREPARING → READY → OUTFORDELIVERY → DELIVERED` (or `CANCELLED`)
- **Order tracking** for customers, plus rider assignment (staff record the rider's phone number when an order goes out for delivery)
- **Cancellation**: customers can cancel a `PENDING` order within 30 minutes; if it was already paid, a refund gets flagged
- **Payments**: MTN Mobile Money (MoMo) sandbox integration (partial, see below)
- **Email notifications**: verification, password reset, payment confirmed/failed, and admin-created accounts
- **Auditing**: `BaseEntity` with JPA auditing (`createdAt`, `updatedAt`)
- **Frontend**: React pages for login/registration, email verification, password reset, menu, cart, orders and order tracking, and admin views for menu items, orders/rider assignment, and user management. The frontend logs you out when your token expires

---

## Tech Stack

| Layer            | Technology                                           |
| ---------------- | ---------------------------------------------------- |
| Backend          | Spring Boot 4, Java 21                               |
| Database         | PostgreSQL (H2 in-memory for the `uat` test profile) |
| ORM              | Spring Data JPA / Hibernate                          |
| Security         | Spring Security, JWT (jjwt)                          |
| Email            | Spring Mail                                          |
| Payments         | MTN MoMo sandbox API                                 |
| Testing          | JUnit 5, Mockito, `@WebMvcTest`, `@DataJpaTest`, JaCoCo |
| API Docs         | Swagger / OpenAPI (springdoc)                        |
| API Testing      | Postman                                              |
| Boilerplate      | Lombok, DTOs                                         |
| Frontend         | React, TypeScript, Redux Toolkit, Vite               |
| Containerization | Docker, Docker Compose                               |
| CI               | GitHub Actions (skeleton only, see below)            |
| Build Tool       | Maven (wrapper included)                             |

---

## Project Structure

```
restomgmt/
├── backend/
│   ├── docker-compose.yml          # backend + infrastructure
│   └── restomgmt/src/main/java/com/restomgmt/site/
│       ├── user/        # auth, JWT filter, users, roles, permissions, email, security config
│       ├── menu/        # categories and menu items
│       ├── cart/
│       ├── order/
│       ├── payment/     # MoMo client + config
│       ├── validation/  # custom password validator
│       ├── BaseEntity.java
│       └── SetupDataLoader.java    # seeds roles, permissions, sample menu, and a test admin
├── frontend/
│   ├── docker-compose.yml
│   └── restomgmtReact/src/
│       ├── api/         # fetch wrappers per domain
│       ├── features/    # auth, menu, cart, order, payments, users, dashboard (views + Redux slices)
│       ├── components/
│       └── store/
├── docker-compose.prod.yml          # full stack
├── er_diagram_restomgmt.png
└── .env.example
```

The backend is organized **by domain** rather than by layer, so each feature keeps its own controllers, services, repositories, models, and DTOs together. I started with a layer-based structure (like the student reg project) and switched over, because once there were more than a couple of domains, jumping between folders for one feature got painful.

The database design is in `er_diagram_restomgmt.png`.

---

## Setup and Installation

### Prerequisites

- Java 21
- Node.js (for the frontend)
- Docker and Docker Compose
- Maven (or the included wrapper)

### Steps

1. **Clone the repo**

   ```
   git clone https://github.com/orielkeza/restomgmt.git
   cd restomgmt
   ```

2. **Create a `.env` file** from `.env.example` and fill it in. Secrets are never committed.

   ```
   DB_NAME=
   DB_USERNAME=
   DB_PASSWORD=
   JWT_SECRET=
   CORS_ALLOWED_ORIGIN=
   VITE_API_URL=
   MTN_SUBSCRIPTION_KEY=
   MTN_API_USER=
   MTN_API_KEY=
   ```

   The JWT secret should come from a cryptographically secure random generator, not something typed by hand, e.g.:

   ```
   openssl rand -base64 64
   ```

   For emails to actually send, you also need mail server settings (`spring.mail.*`) in your local profile.

3. **Run with Docker.** See `README.docker.md` for all the options.

   ```
   # backend + infrastructure
   cd backend && docker compose up -d

   # or the full stack
   docker compose -f docker-compose.prod.yml up -d
   ```

4. **Or run the backend locally with a profile**

   ```
   cd backend/restomgmt
   ./mvnw spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=dev"
   ```

   Profiles:
   - `dev`: local development against PostgreSQL. `application-dev.properties` is gitignored, so each developer keeps their own
   - `uat`: H2 in-memory database plus a permissive `TestSecurityConfig`. The tests use this one
   - `docker`: used inside Docker Compose, reads everything from environment variables
   - `prod`: `ddl-auto=validate`, quieter logging

   On startup (in every profile except `uat`), `SetupDataLoader` seeds the roles and permissions, a few sample menu items, and a test admin account.

5. **Run the frontend**

   ```
   cd frontend/restomgmtReact
   npm install
   npm run dev
   ```

   It runs on `http://localhost:5173`, which is also the backend's default allowed CORS origin.

Once the backend is running, Swagger UI is at `http://localhost:8080/swagger-ui/index.html`.

---

## API Endpoints

| Method | Endpoint                               | Access        | Description                              |
| ------ | -------------------------------------- | ------------- | ---------------------------------------- |
| POST   | `/auth/register`                       | Public        | Register a new customer                  |
| POST   | `/auth/login`                          | Public        | Log in, returns JWT                      |
| GET    | `/auth/verify-email`                   | Public        | Verify email with token                  |
| POST   | `/auth/resend-verification`            | Public        | Resend verification email                |
| POST   | `/auth/forgot-password`                | Public        | Request a password reset email           |
| POST   | `/auth/reset-password`                 | Public        | Reset password with token                |
| GET    | `/users`                               | Authenticated | List users                               |
| GET    | `/users/{id}/info`                     | Authenticated | Get a user                               |
| PUT    | `/users/{id}/update`                   | Admin         | Update a user                            |
| DELETE | `/users/{id}/delete`                   | Admin         | Delete a user                            |
| PUT    | `/users/{id}/roles`                    | Admin         | Assign a role                            |
| DELETE | `/users/{id}/roles`                    | Admin         | Remove a role                            |
| POST   | `/users/admin/create`                  | Admin         | Create a staff/admin account             |
| GET    | `/menu/categories`                     | Public        | List categories                          |
| GET    | `/menu/categories/{id}`                | Public        | Get a category                           |
| POST   | `/menu/categories`                     | Staff+        | Create a category                        |
| PUT    | `/menu/categories/{id}`                | Staff+        | Update a category                        |
| DELETE | `/menu/categories/{id}`                | Admin         | Delete a category                        |
| GET    | `/menu/items`                          | Public        | List available menu items                |
| GET    | `/menu/items/all`                      | Staff+        | List all items, including unavailable    |
| GET    | `/menu/items/category/{categoryId}`    | Public        | Items in a category                      |
| GET    | `/menu/items/{id}`                     | Public        | Get a menu item                          |
| POST   | `/menu/items`                          | Staff+        | Create a menu item                       |
| PUT    | `/menu/items/{id}`                     | Staff+        | Update a menu item                       |
| PATCH  | `/menu/items/{id}/availability`        | Staff+        | Toggle availability                      |
| DELETE | `/menu/items/{id}`                     | Admin         | Delete a menu item                       |
| GET    | `/cart`                                | Authenticated | View cart                                |
| POST   | `/cart/items`                          | Authenticated | Add item to cart                         |
| PUT    | `/cart/items/{menuItemId}`             | Authenticated | Update quantity                          |
| DELETE | `/cart/items/{menuItemId}`             | Authenticated | Remove item                              |
| DELETE | `/cart`                                | Authenticated | Clear cart                               |
| POST   | `/orders`                              | Authenticated | Place an order from the cart             |
| GET    | `/orders`                              | Authenticated | My orders                                |
| GET    | `/orders/{id}`                         | Authenticated | Track one order                          |
| DELETE | `/orders/{id}/cancel`                  | Authenticated | Cancel (PENDING, within 30 min)          |
| GET    | `/orders/all`                          | Staff+        | All orders                               |
| PUT    | `/orders/{id}/status`                  | Staff+        | Update order status                      |
| PUT    | `/orders/{id}/rider`                   | Staff+        | Assign a rider                           |
| POST   | `/payments/orders/{orderId}`           | Authenticated | Start a MoMo payment                     |
| GET    | `/payments/orders/{orderId}/status`    | Authenticated | Check and update payment status          |
| GET    | `/payments/orders/{orderId}`           | Authenticated | Get payment details                      |
| POST   | `/payments/orders/{orderId}/refund`    | Staff+        | Flag a refund                            |

"Staff+" means staff or admin, because of the role hierarchy.

---

## Testing

```
cd backend/restomgmt
./mvnw clean test
```

Tests run under the `uat` profile (H2 + `TestSecurityConfig`) and are written in **Arrange-Act-Assert** style:

- **Service layer**: plain unit tests with `@ExtendWith(MockitoExtension.class)`, with repositories and other dependencies mocked. Covers every domain (auth, users, menu, cart, orders, payments)
- **Controller layer**: `@WebMvcTest` slices checking status codes, request mappings, and responses. Also covers every domain
- **Repository layer**: `@DataJpaTest` for the user, role, permission, category, and menu item repositories
- **JWT utility**: its own unit tests

JaCoCo is set up for coverage reports (`./mvnw test jacoco:report`).

### Manual API testing with Postman

This was also the project where I learned Postman. I used it to test endpoints by hand as I built them: logging in, grabbing the JWT from the response and sending it on later requests, checking that each role got the right access (and the wrong roles got blocked), and walking an order through the whole flow from cart to delivered. It was especially useful for catching things the automated tests didn't cover, like how the real security chain behaved with actual tokens, since the tests run with a permissive security config.

---

## Challenges and Solutions

**Piecing things together from different tutorials.** I was learning each part of this (JWT, Spring Security, JPA relationships, Docker, email) as I built it, and usually from a different tutorial or guide for each one. Each source had its own conventions for structuring code, and some were written for older versions of Spring Boot and Spring Security. Every piece made sense on its own, but put together they didn't line up. For example, I ended up with a plain JDBC dependency alongside Spring Data JPA, duplicate entity classes, a mix of field injection and constructor injection, and a circular dependency between security beans. I also had a broken authority mapping, where roles weren't being translated into authorities the way Spring Security expected, so access checks didn't behave the way I thought they would.

The outdated material was the hardest part. Code that matched a tutorial line for line would still fail with deprecation errors or just behave differently, and working out *why* meant going to the official docs and changelogs instead of the tutorial.

Eventually I did a ground-up stabilization pass. I removed JDBC and went fully Spring Data JPA, deleted the duplicate entities, switched everything to constructor injection with Lombok, fixed the authority mapping and the circular dependency, and brought every domain onto one consistent set of conventions (DTOs in and out, never exposing entities). When I had a choice, I went with the least disruptive fix that worked rather than the cleanest rewrite, because every "cleaner" refactor I tried early on broke something else.

**Secret management.** At one point I hardcoded the database password just to get things running, which told me my environment variables were set up wrong. I moved all secrets into a `.env` file kept out of git (with a `.env.example` to show what's needed), gitignored the dev properties file, and generate the JWT secret with a CSPRNG so it's long and actually random.

**User and role design.** My first schema had separate entities for different kinds of users (clients vs. staff). That got messy fast, so I restructured it to one `User` entity with roles, and roles linked to permissions. Adding a role hierarchy (ADMIN > STAFF > USER) meant I didn't have to list every role on every endpoint.

**Compilation errors that weren't really code errors.** For a while the app wouldn't start, and I went through build and model files trying to find the bug. Some of it was real (profile management wasn't set up properly, and the H2 driver for the `uat` profile was missing), but one "compilation error" turned out to be a PostgreSQL database that just didn't exist yet.

**Testing with security on.** I set up a `uat` profile with H2 and a `TestSecurityConfig` so tests don't need a real database or real tokens. The tradeoff is that the automated tests don't exercise the real security rules, which is part of why I leaned on Postman.

**MoMo sandbox.** The sandbox has limitations, so I could only verify that initiating a payment works and gets saved in the database. The callback/confirmation side couldn't be tested properly (more below).

---

## Known Limitations / What I Didn't Finish

The original brief was much bigger than what I built. What's missing or incomplete:

- **MoMo payments aren't fully working end to end.** Starting a payment works and is saved, and there's a status check, but because of the sandbox limits I couldn't rely on it to confirm payments. I added a manual override for payment status in the service layer, but it isn't exposed through an endpoint yet.
- **Refunds are only flagged**, not actually sent back through MoMo.
- **Seat/table booking wasn't built.** The booking UI from early on was never connected to a backend.
- **Notifications are email only and synchronous.** The brief asked for RabbitMQ-based async messaging (SMS, push), plus reminders before bookings and orders. RabbitMQ is in the Docker Compose setup but isn't used in the code.
- **No audit logging.** The brief wanted login attempts (success/failure, browser, IP) logged to MongoDB. MongoDB is in the Compose file but not wired up. JPA auditing only gives timestamps.
- **No OTP / lockout flow** after failed logins, and **no Google (OAuth2) sign-in**.
- **No reports** for staff/admin, and no "similar items" suggestions on the menu.
- **No internationalization** (English/French was in the brief).
- **CI is a skeleton.** The workflow only builds; the test and 90% coverage steps are commented out, and the workflow file isn't in `.github/workflows`, so GitHub doesn't run it yet.
- **Error handling is inconsistent.** Most controllers catch exceptions themselves; there's no global `@ControllerAdvice` yet.
- **Repository tests** only exist for the user and menu domains so far.

---

## Lessons Learned

- Getting something working and getting it stable are different jobs, and the second one takes longer
- Check which version a tutorial was written for before following it, and treat the official docs as the source of truth
- When learning from several sources, pick one set of conventions early and adapt everything to it, instead of copying each source's style as-is
- Package by feature once a project has more than a few domains
- Test slices (`@WebMvcTest`, `@DataJpaTest`) make tests way faster and more focused than loading the whole context every time
- Manual testing in Postman and automated tests catch different things, and I needed both
- Third-party payment APIs take much more time than they look like they will: sandbox quirks, async callbacks, and a lot of failure states
- Scope honestly. The brief was huge, and finishing fewer things properly beat half-finishing everything

---

## Future Improvements

- [ ] Merge the feature branches into `main`
- [ ] Finish MoMo: callback handling, an endpoint for manual confirmation, and real refunds
- [ ] Global exception handler with a consistent error response format
- [ ] Get CI running for real, with tests and a coverage gate
- [ ] Seat booking
- [ ] Move notifications onto RabbitMQ, and add order/booking reminders
- [ ] Login audit logging in MongoDB, OTP after failed logins, and Google sign-in
- [ ] Reports for admins
- [ ] English/French internationalization
- [ ] Pagination and filtering on menu and order lists

---

## Author

Ori built this during a software development internship at Wys Ltd as a follow-up to an earlier Spring Boot CRUD project, to learn what a production-style full-stack app actually involves.
