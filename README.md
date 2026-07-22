# School ERP — Starter Project

A working starter for a School ERP system: **Spring Boot (Java) + MySQL** backend,
**React (Vite)** frontend, JWT authentication, and CRUD modules for:

- **Students** (admissions, class assignment, guardian info, status)
- **Staff** (employee records, designation, department, salary)
- **Classes & Sections** (with an assigned class teacher)
- **Attendance** (daily register per class, bulk marking)
- **Fees / Finance** (fee structures per class, payment recording, student balance lookup)

## Project structure

```
school-erp/
├── backend/     Spring Boot 3 + Spring Security + JPA + MySQL
└── frontend/    React 18 + Vite + React Router + Axios
```

## 1. Backend setup

**Requirements:** Java 17+, Maven, a running MySQL server.

1. Create a MySQL user/password (or use root) and make sure MySQL is running on `localhost:3306`.
   The app will auto-create the `school_erp` database on first run (`createDatabaseIfNotExist=true`).

2. Edit `backend/src/main/resources/application.properties` and set your DB credentials:
   ```properties
   spring.datasource.username=root
   spring.datasource.password=your_password
   ```
   Also change `app.jwt.secret` to a random string before deploying anywhere real.

3. Run it:
   ```bash
   cd backend
   mvn spring-boot:run
   ```
   The API starts on **http://localhost:8080**.

4. A default admin user is seeded automatically on first run:
   ```
   username: admin
   password: admin123
   ```
   (Configurable via `app.seed.admin.username` / `app.seed.admin.password`.)

### Key API endpoints

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/auth/login` | Log in, returns JWT |
| POST | `/api/auth/register` | Create a new staff/admin login |
| GET/POST/PUT/DELETE | `/api/students` | Student CRUD (supports `?classId=` and `?search=`) |
| GET/POST/PUT/DELETE | `/api/staff` | Staff CRUD |
| GET/POST/PUT/DELETE | `/api/classes` | Class/section CRUD |
| GET/POST | `/api/attendance` | Get by `?date=&classId=`, mark single record |
| POST | `/api/attendance/bulk` | Mark a whole class's attendance in one call |
| GET/POST/DELETE | `/api/fees/structures` | Fee structure CRUD |
| GET/POST/DELETE | `/api/fees/payments` | Record/list payments |
| GET | `/api/fees/summary/student/{id}` | Total due / paid / balance for a student |

All endpoints except `/api/auth/**` require an `Authorization: Bearer <token>` header.

## 2. Frontend setup

**Requirements:** Node.js 18+.

```bash
cd frontend
npm install
cp .env.example .env      # adjust VITE_API_BASE_URL if needed
npm run dev
```

Opens on **http://localhost:5173**. Log in with the seeded admin account above.

## Notes on what's included vs. what to add next

This is a working **starter**, not a finished product. It gives you real auth, real CRUD
against MySQL, and a usable UI for the modules you asked for (core + fees). Natural next steps:

- **Exams/Grades module** (marks entry, report cards)
- **Role-based UI restrictions** (backend already issues a `role` claim; you can gate routes/buttons by it — currently any authenticated user can hit any endpoint)
- **Server-side field validation** (`@NotBlank`, `@Email`, etc. on entities) — currently only the auth DTOs validate input
- **Pagination** on the students/payments tables once data grows
- **Receipts/PDF generation** for fee payments
- **Docker Compose** file to spin up MySQL + backend + frontend together

I wasn't able to run `mvn compile`/`npm run build` against your real MySQL instance in this
environment (no network access to Maven Central here), but the frontend build was verified to
compile cleanly, and I did a manual pass over the backend for correctness — worth running
`mvn spring-boot:run` locally and letting me know if anything needs a fix.
