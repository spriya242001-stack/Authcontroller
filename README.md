# SmartSpend backend

Spring Boot REST API for expense tracking, budgets, account verification,
password recovery, and PDF/Excel reports. The project also includes the static
frontend served by Spring Boot or deployed separately to Netlify.

- Live frontend: https://smartspendtracking.netlify.app
- Live backend login: http://98.94.12.136:8080/login
- Backend source: `src/main/java/com/example/Smartspend_backend`
- Frontend source: `src/main/resources/static`

## Run locally

Requires Java 22, MySQL/MariaDB, and the `smartspend_db` database.
Create the database before starting the app and provide your own credentials:

```bash
export DB_URL='jdbc:mysql://localhost:3306/smartspend_db?useSSL=false&serverTimezone=UTC'
export DB_USERNAME='your_database_user'
export DB_PASSWORD='your_database_password'
export MAIL_USERNAME='your_email_address'
export MAIL_PASSWORD='your_smtp_app_password'
export APP_FRONTEND_URL='http://localhost:8080'
./mvnw spring-boot:run
```

The mail settings default to Gmail SMTP with STARTTLS on port 587. A Gmail app
password is required. For local work without registration emails, set
`VERIFICATION_EMAIL_ENABLED=false`; password recovery still requires working SMTP.

An optional `.smartspend-local.properties` file can provide local settings.
It is ignored by Git. Never commit that file or actual credentials.

## Build and test

```bash
./mvnw clean package
java -jar target/Smartspend-backend-0.0.1-SNAPSHOT.jar
```

Tests use an isolated H2 database and mocked mail delivery. They cover browser
login, token revocation, single-use and expired recovery links, verification,
expense CRUD, budget totals, and PDF/Excel output.

## API routes

| Method | Route | Purpose |
| --- | --- | --- |
| POST | `/api/auth/register` | Register and send a verification link |
| POST | `/api/auth/login` | Log in; returns token, email, role, and user ID |
| GET | `/api/auth/verify?code=...` | Consume a verification link |
| POST | `/api/auth/forgot-password?email=...` | Request password recovery |
| POST | `/api/auth/reset-password` | Reset using email, token, and newPassword |
| GET, POST | `/api/expenses` | List or create expenses |
| GET | `/api/expenses/filter` | Filter the authenticated user's expenses |
| GET, PUT, DELETE | `/api/expenses/{id}` | Read, update, or delete an owned expense |
| POST | `/api/budgets` | Create or update a category/month/year budget |
| GET | `/api/budgets/me` | List the authenticated user's budgets |
| GET | `/api/budgets/user/{userId}` | List budgets with owner/admin checks |
| GET | `/api/reports/pdf?userId=...` | Export the owner's PDF report |
| GET | `/api/reports/excel?userId=...` | Export the owner's Excel report |
| POST | `/logout` | Revoke the login token and browser session |

Protected routes accept `Authorization: Bearer <token>` or a valid browser
session. Verification links expire in 24 hours; reset links expire in 30 minutes.
Issuing a new email link replaces the previous active link. Existing accounts
can continue to log in without mandatory email verification.

See [deployment instructions](deploy/README.md) for EC2 and Netlify updates.
