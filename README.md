# LMS - Learning Management System

A production-ready Learning Management System built with microservices architecture using Java Spring Boot.

## Architecture Overview

The LMS platform consists of 11 microservices:

| Service | Port | Description |
|---------|------|-------------|
| API Gateway | 8080 | Single entry point, JWT validation, rate limiting |
| Auth Service | 8081 | Keycloak integration, login/logout, sessions |
| User Service | 8082 | User management, roles, enrollments |
| Course Service | 8083 | Course, module, lesson management |
| Content Service | 8084 | File uploads with MinIO |
| Assignment Service | 8085 | Assignments and grading with Kafka |
| Quiz Service | 8086 | Quizzes with auto/manual grading |
| Attendance Service | 8087 | Attendance tracking with notifications |
| Messaging Service | 8088 | Email/push notifications |
| Analytics Service | 8089 | Progress tracking and reporting |
| Search Service | 8090 | Full-text search with Elasticsearch |

## Tech Stack

- **Backend**: Java 17, Spring Boot 3.2, Spring Cloud 2023
- **API Gateway**: Spring Cloud Gateway
- **Authentication**: Keycloak with JWT/OAuth2
- **Databases**: PostgreSQL 15, Redis 7
- **Search**: Elasticsearch 8.x
- **Messaging**: Apache Kafka
- **Object Storage**: MinIO
- **Container**: Docker, Kubernetes

## Quick Start

### Prerequisites

- Docker & Docker Compose
- Java 17+ (for local development)
- Maven 3.8+

### Run with Docker Compose

```bash
# Start all services
docker-compose up -d

# View logs
docker-compose logs -f

# Stop all services
docker-compose down
```

### Access Points

- **API Gateway**: http://localhost:8080
- **Keycloak Admin**: http://localhost:8180 (admin/admin_password)
- **MinIO Console**: http://localhost:9001 (minio_admin/minio_secret_password)

### Default Users

| Username | Password | Role |
|----------|----------|------|
| admin | admin123 | ADMIN |
| instructor1 | instructor123 | INSTRUCTOR |
| student1 | student123 | STUDENT |
| parent1 | parent123 | PARENT |

## API Endpoints

### Authentication
```
POST /api/v1/auth/login          - Login
POST /api/v1/auth/refresh        - Refresh token
POST /api/v1/auth/logout         - Logout
GET  /api/v1/auth/me             - Current user
```

### Users
```
GET    /api/v1/users             - List users
POST   /api/v1/users             - Create user
GET    /api/v1/users/{id}        - Get user
PUT    /api/v1/users/{id}        - Update user
DELETE /api/v1/users/{id}        - Delete user
```

### Courses
```
GET    /api/v1/courses           - List courses
POST   /api/v1/courses           - Create course
GET    /api/v1/courses/{id}      - Get course
PUT    /api/v1/courses/{id}      - Update course
POST   /api/v1/courses/{id}/publish - Publish course
```

### Assignments
```
GET  /api/v1/assignments/{id}           - Get assignment
POST /api/v1/assignments/{id}/submit    - Submit assignment
PUT  /api/v1/submissions/{id}/grade     - Grade submission
```

### Search
```
GET /api/v1/search?q={query}           - Global search
GET /api/v1/search/courses?q={query}   - Search courses
GET /api/v1/search/users?q={query}     - Search users
```

## Kubernetes Deployment

```bash
# Apply all manifests
kubectl apply -k k8s/

# Check deployment status
kubectl get pods -n lms

# View logs
kubectl logs -f deployment/api-gateway -n lms
```

## Project Structure

```
LMS/
├── docker-compose.yml          # Local development setup
├── pom.xml                     # Parent POM
├── services/
│   ├── lms-common/            # Shared library
│   ├── api-gateway/           # API Gateway
│   ├── auth-service/          # Authentication
│   ├── user-service/          # User management
│   ├── course-service/        # Course management
│   ├── content-service/       # Content/Media
│   ├── assignment-service/    # Assignments
│   ├── quiz-service/          # Quizzes
│   ├── attendance-service/    # Attendance
│   ├── messaging-service/     # Notifications
│   ├── analytics-service/     # Analytics
│   └── search-service/        # Search
├── infrastructure/
│   ├── keycloak/              # Keycloak config
│   ├── kafka/                 # Kafka topics
│   └── postgres/              # DB init scripts
└── k8s/                       # Kubernetes manifests
```

## Building

```bash
# Build all services
mvn clean package -DskipTests

# Build Docker images
docker-compose build
```

## Configuration

Environment variables can be configured in:
- `docker-compose.yml` for local development
- `k8s/configmaps/` for Kubernetes
- `k8s/secrets/` for sensitive data

## License

MIT License
