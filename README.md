# Contact Management System (Java Spring Boot + React.js)

A complete, production-structured Fullstack **Contact Management System** organized into separate **`frontend/`** and **`backend/`** directories.

---

## 📁 Project Structure

```text
├── frontend/                   # React.js + TypeScript + Tailwind CSS SPA
│   ├── public/                 # Static assets & rounded white-background favicon
│   ├── src/
│   │   ├── components/         # AuthModal, ContactList, ContactFormModal, DeleteConfirmModal, ContactDetailModal, UserProfileView, ChangePasswordModal, Navbar, Toast
│   │   ├── data/               # Country list & sample contacts
│   │   ├── services/           # Spring Boot REST API client (api.ts), storage, Slf4j-style logger
│   │   └── types/              # TypeScript interfaces
│   ├── index.html
│   ├── package.json
│   ├── tsconfig.json
│   └── vite.config.ts          # Includes /api proxy -> http://localhost:8080
│
├── backend/                    # Java 17 + Spring Boot 3.3.4 + JPA/Hibernate + SQL Server
│   ├── database/
│   │   └── sqlserver_schema.sql
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/enterprise/cms/
│   │   │   │   ├── ContactManagementApplication.java
│   │   │   │   ├── controller/ # AuthController, UserController, ContactController
│   │   │   │   ├── service/    # AuthServiceImpl, ContactServiceImpl
│   │   │   │   ├── repository/ # UserRepository, ContactRepository (Spring Data JPA)
│   │   │   │   ├── entity/     # User, Contact, ContactEmail, ContactPhone
│   │   │   │   ├── dto/        # Validated Request/Response DTOs
│   │   │   │   ├── exception/  # GlobalExceptionHandler (@RestControllerAdvice) & custom exceptions
│   │   │   │   └── security/   # SecurityConfig, JwtTokenProvider, JwtAuthenticationFilter
│   │   │   └── resources/
│   │   │       ├── application.yml
│   │   │       └── logback-spring.xml
│   │   └── test/
│   │       └── java/com/enterprise/cms/
│   │           ├── controller/ # AuthControllerTest, ContactControllerTest (MockMvc + Mockito)
│   │           ├── service/    # AuthServiceImplTest, ContactServiceImplTest (JUnit 5 + Mockito)
│   │           └── repository/ # ContactRepositoryTest (@DataJpaTest)
│   └── pom.xml                 # Maven dependencies, JaCoCo, SonarQube plugin
│
└── sonar-project.properties    # SonarQube quality rules for Java & React/TypeScript
```

---

## 🛠️ Technology Stack

- **Backend**: Java 17, Spring Boot 3.3.4, Spring Web, Spring Data JPA, Hibernate 6, Spring Security + JWT
- **Database**: Microsoft SQL Server (`sqlserver_schema.sql`)
- **Logging**: SLF4J + Logback (`logback-spring.xml` with console + rolling file appenders)
- **Testing**: JUnit 5, Mockito, Spring MockMvc, `@DataJpaTest`
- **Code Quality**: SonarQube (`sonar-project.properties` + `sonar-maven-plugin` + JaCoCo)
- **Frontend**: React 19, TypeScript, Tailwind CSS, Vite, Lucide Icons
- **Version Control**: Git (feature branching, pull requests, and `main`/`develop` merge workflow)

---

## 🚀 Running Locally

### 1. Database Setup (Microsoft SQL Server)
Execute `backend/database/sqlserver_schema.sql` in SQL Server Management Studio (SSMS) or `sqlcmd`, and configure credentials in `backend/src/main/resources/application.yml` (or via `DB_HOST`, `DB_USER`, `DB_PASSWORD`).

### 2. Run Java Spring Boot Backend & Tests
```bash
cd backend
mvn clean test          # Executes JUnit 5 & Mockito tests + generates JaCoCo coverage report
mvn spring-boot:run     # Starts REST API on http://localhost:8080
```

### 3. Run SonarQube Analysis
```bash
mvn -f backend/pom.xml sonar:sonar -Dsonar.host.url=http://localhost:9000 -Dsonar.login=<TOKEN>
```

### 4. Run React.js Frontend
```bash
cd frontend
npm install
npm run dev             # Starts Vite dev server on http://localhost:3000 (proxies /api -> localhost:8080)
```
To connect the frontend directly to the running Spring Boot API, set `VITE_USE_BACKEND_API=true` in `frontend/.env`.

---

## 🌿 Recommended Git Branching Strategy
- `main`: Production-ready releases
- `develop`: Integration branch for completed features
- `feature/<ticket-name>`: Isolated feature branches (e.g., `feature/contact-pagination`, `feature/jwt-auth`)
- `bugfix/<issue>`: Targeted fixes merged back via Pull Request after SonarQube & JUnit checks pass
