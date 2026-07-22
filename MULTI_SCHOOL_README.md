# School ERP — Multi-School (Multi-Tenant) Setup

This is your original single-school ERP, converted so **one deployment can serve many
schools**, each with its own database, its own users, and its own data — with logins
like `admin@svm` where `svm` is the school's short code.

## What changed, in one paragraph

There are now two kinds of database: one fixed **master** database (`school_erp_master`)
that only stores the *list* of schools, active-session tracking, and the platform
super-admin account; and one **tenant** database per school (e.g. `school_svm`,
`school_abc`), holding that school's own students, staff, classes, attendance, fees, etc.
— exactly the tables your original app already had. When a request comes in, the backend
looks at the school code (from the login username, then from the JWT on every later
request) and transparently points all your existing repositories/queries at that school's
database. None of your existing controllers/services/entities needed to change.

## New login scheme

- **School staff**: `username@schoolcode`, e.g. `admin@svm`. Routed to that school's own
  `users` table.
- **Platform super-admin**: a plain username with no `@`, e.g. `sysadmin`. Seeded
  automatically on first run — see `app.seed.superadmin.*` in `application.properties`
  (default `sysadmin` / `sysadmin123`, **change this**). Logs in to `/schools` in the
  frontend to register new schools.

## Single active session

If `admin@svm` logs in while already logged in elsewhere, the login now returns **HTTP 409**
with `{ conflict: true, message: "..." }` instead of a token. The frontend shows a
confirmation dialog ("Log out other session & continue?"). If confirmed, the login is
resent with `force: true`, which invalidates the old session — its token stops working on
its very next request (checked via a `jti` claim against the `active_sessions` table).

## Before you run it

1. **Backend config** — `backend/src/main/resources/application.properties`:
   - `app.master-datasource.*` — connection details for the master DB (created
     automatically on first run via `createDatabaseIfNotExist=true`).
   - `app.jwt.secret` — change this for anything beyond local testing.
   - `app.seed.superadmin.username` / `.password` — change the default super-admin
     password before deploying anywhere real.

2. **Start the backend** (`run-backend.bat`, or `mvn spring-boot:run` from `backend/`).
   On first run it creates `school_erp_master` and seeds the super-admin account — watch
   the console log for the seeded username/password.

   > This project has no Maven cache in the sandbox it was built in, so **this code has
   > not been compiled or run yet** — please build it locally (`mvn clean package` or
   > your IDE) as your first step, and fix up anything your specific Maven/Hibernate
   > patch version disagrees with. The riskiest piece is
   > `TenantSchemaInitializer`, which uses Hibernate 6's
   > `SchemaManagementToolCoordinator` (the modern replacement for the older, now-removed
   > `SchemaExport` class) to create tables in a brand-new school's database — flagged
   > below with what to check if it doesn't compile against your exact Hibernate version.

3. **Start the frontend** as before (`run-frontend.bat`).

4. **Log in as the super-admin** (`sysadmin` / whatever you configured) and register your
   first school from the **Schools** page: pick a short code (e.g. `svm`), a display name,
   and the DB connection details for that school (host/port/db name/username/password).
   Leave "Create tables" checked for a brand-new database. This automatically:
   - creates the school's database if it doesn't exist,
   - creates all the tables in it,
   - seeds a default `admin` / `admin123` user for that school (change the password
     immediately after first login),
   - optionally seeds 5 demo records per module if you tick that box.

5. **Log in as the school** using `admin@<code>` (e.g. `admin@svm`) / `admin123`.

## Migrating your existing single-school data

If you already have a `school_erp` database with real data in it, register it as a school
too, but **uncheck "Create tables"** (`initializeSchema: false`) and point the DB fields at
your existing database/credentials — its existing `users`, `students`, etc. tables are used
as-is, nothing is touched or recreated.

## Key new files (backend)

| File | Purpose |
|---|---|
| `tenant/TenantContext.java` | Thread-local holding "which school" for the current request |
| `tenant/TenantRoutingDataSource.java` | Routes every DB connection to the right school's pool |
| `tenant/TenantDataSourceProvider.java` | Lazily creates/caches one HikariCP pool per school |
| `tenant/TenantPersistenceConfig.java` | Wires the tenant (per-school) JPA persistence unit |
| `master/config/MasterPersistenceConfig.java` | Wires the fixed master JPA persistence unit |
| `master/entity/School.java` | Registry row: code, name, DB connection info |
| `master/entity/ActiveSession.java` | One row per logged-in (school, username) — powers single-session enforcement |
| `master/entity/SuperAdmin.java` | Platform account that registers schools |
| `master/service/SchoolService.java` | Registers a school: creates DB, tables, default admin |
| `master/service/TenantSchemaInitializer.java` | Creates tables in a brand-new school DB |
| `master/service/SessionService.java` | Enforces "one active session per user" |
| `master/controller/SchoolController.java` | `/api/schools` — super-admin only |
| `security/JwtAuthFilter.java` | Now also sets tenant context + validates the session is still the active one |
| `controller/AuthController.java` | Parses `user@school`, handles the 409 conflict + `force` flow |

## Known limitations / things to review before production

- **DB credentials in plaintext**: `School.dbPassword` is stored as plain text in the
  master DB. For production, encrypt this column or move credentials to a secrets manager.
- **Uploaded logos share one folder** (`backend/uploads/logos/`) across all schools —
  filenames are UUID'd so they won't collide, but it's not per-tenant isolated on disk.
- **`TenantSchemaInitializer`** uses Hibernate's `SchemaManagementToolCoordinator` API
  (confirmed to exist in Hibernate 6.x, which Spring Boot 3.3.2 uses) rather than the
  older `SchemaExport` class, which was removed from Hibernate 6. If your Maven build
  resolves a different Hibernate version and this doesn't compile, the fallback is to
  generate a `schema.sql` per school (via `hibernate.hbm2ddl.auto=create` against a
  throwaway `SessionFactory`, `.sql` export) and run it with `JdbcTemplate` instead.
- **This code has not been compiled** in the environment it was written in (no Maven/
  Hibernate jars available there). Please build locally first and let me know what (if
  anything) doesn't compile — happy to fix it up.
- No automated tests were added for the new login/session/tenant-routing logic.
