You are an expert backend architect and Java/Spring Boot engineer. Your task is to **generate a full production-ready LMS backend** as a **microservices architecture** with Docker and Kubernetes support. Follow these requirements exactly:

---

1️⃣ **Architecture & Tech Stack**
- All microservices implemented in **Java Spring Boot** (Maven projects).  
- Each microservice runs in a **separate Docker container**; provide a **Dockerfile per service** with production best practices (multi-stage build, non-root user, health check).  
- Provide **docker-compose.yml** for local development including:
  - All microservices
  - Postgres (relational data)
  - Redis (session caching, course/attendance caching)
  - Kafka (asynchronous jobs)
  - Elasticsearch/OpenSearch (full-text search)
  - Keycloak (authentication + RBAC)
  - API Gateway
- Provide **Kubernetes manifests** for production deployment including:
  - Deployments, Services, Ingress, ConfigMaps, Secrets
  - Persistent volumes for Postgres, Elasticsearch
  - Horizontal Pod Autoscaling setup

---

2️⃣ **Microservices & Responsibilities**

| Service | Responsibilities | Database / Cache / Messaging |
|---------|-----------------|-----------------------------|
| **API Gateway** | Single entry point for frontend; validates JWT; routes requests to backend services; optional response aggregation | N/A |
| **Auth Service** | Integrate with Keycloak; handle login, JWT validation, roles, RBAC | Postgres for user profile metadata, Redis for session caching |
| **User Service** | Manage users, roles, enrollments, parent-student relationships | Postgres, Redis caching for frequent user queries |
| **Course Service** | CRUD courses/modules/lessons; manage course metadata | Postgres, Redis caching for frequently accessed courses |
| **Content/Media Service** | Upload/download videos, PDFs; integrate with CDN; track usage | Object storage (MinIO/S3), Redis for hot data |
| **Assignment/Grading Service** | Handle assignment creation, submission, grading, plagiarism detection | Postgres, Kafka for async grading jobs |
| **Attendance Service** | Track student attendance; instructors mark; notify parents | Postgres, Redis, Kafka for async notifications |
| **Quiz/Exam Service** | Create quizzes/exams; auto/manual grading; analytics per student/course | Postgres, Kafka for async grading |
| **Messaging/Notification Service** | Send emails/push/SMS to students, parents, instructors asynchronously | Kafka, optional Postgres for logs |
| **Analytics & Reporting Service** | Track student progress, course completion, engagement metrics | Postgres/data warehouse, Redis caching, Kafka for events |
| **Search Service** | Full-text search for courses, users, content | Elasticsearch/OpenSearch |

---

3️⃣ **Key Requirements**
- **Caching:** Redis for session tokens, attendance, course metadata, frequent queries.  
- **Async processing:** Kafka topics for grading, notifications, analytics, and other background tasks.  
- **Database:** Postgres with read replicas for scaling.  
- **Search:** Elasticsearch/OpenSearch for full-text search.  
- **Security:** Keycloak + JWT for authentication; implement RBAC across all services.  
- **Async jobs:** Workers implemented in Spring Boot; handle idempotency and retries.  

---

4️⃣ **Frontend Integration**
- React frontend communicates **only with API Gateway**.  
- All microservices trust internal requests from API Gateway.  

---

5️⃣ **Deliverables**
1. Full Spring Boot projects (Maven) for all microservices.  
2. Dockerfile per microservice (production-ready).  
3. docker-compose.yml including all dependencies.  
4. Kubernetes manifests for production deployment.  
5. Kafka topic configurations for async jobs.  
6. Redis caching integrated for session tokens, attendance, course metadata.  
7. Keycloak integration for JWT authentication and RBAC.  
8. Elasticsearch/OpenSearch integration for search functionality.  
9. Example API routes for each service with request/response structure.  
10. Async job examples for grading, attendance notifications, and analytics.

---

⚠️ **Instructions for AI**
- Do **not assume anything**; generate all services, configs, and Docker/Kubernetes files.  
- Include **production-ready best practices**: security, scalability, health checks, and logging.  
- Include **async Kafka consumer/producer examples** for Assignment/Grading, Attendance, Messaging, Analytics.  
- Ensure microservices are **loosely coupled**, independently deployable, and follow microservices architecture principles.  

project  deployment:Docker/K8s.
