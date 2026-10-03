# Archivum

Archivum is a personal backend-oriented worldbuilding and knowledge-management application built around structured fictional worlds, characters, locations, and their relationships.

Although Archivum includes a React frontend, I primarily use the project as a platform for learning and applying backend engineering, software architecture, security, DevOps, and eventually DataOps concepts.

The objective is not simply to build another CRUD application. Archivum is intended to evolve into a larger system in which I can experiment with authentication, data integrity, migrations, deployment, observability, asynchronous processing, search, data pipelines, and distributed infrastructure while maintaining a usable application around them.

I'm doing this project during my studies, hence why i'm focusing my time onto aspects of tech that I currently wish to orientate a career around. 

## Project Focus

Archivum is deliberately backend-first.

The frontend exists to provide a practical interface to the system, but most of the engineering focus is placed on the behavior and architecture behind it that I've designed. 

Current areas of focus include:

- REST API design with Spring Boot
- relational data modelling with MySQL and JPA/Hibernate
- authentication and authorization
- resource ownership and access control
- transactional consistency
- integration and repository testing
- Docker-based local infrastructure
- separation between DTOs, entities, services, repositories, and controllers
- maintaining clear domain boundaries between users, worlds, characters, and locations



## Current Architecture

### Backend

- Java 21
- Spring Boot
- Spring Data JPA / Hibernate
- Spring Security
- MySQL
- H2 for automated persistence/integration tests
- Maven
- Docker Compose

The backend currently manages:

- user registration
- authenticated access
- world ownership
- characters and locations scoped to worlds
- prevention of cross-user resource access
- cascade-style deletion of World-owned resources
- DTO-based API boundaries
- unit, controller, repository, and integration testing

### Frontend

- React
- TypeScript
- Vite

The frontend provides the interface for authentication and management of worlds,
characters, and locations.

The frontend is intentionally treated as a consumer of the backend API rather than
the architectural centre of the project.




## Planned Evolution

Archivum is being developed incrementally, with later stages intended to introduce
more production-oriented backend, DevOps, and DataOps concerns.

### Backend and Security

Planned work includes:

- email verification
- password reset
- a proper authentication lifecycle replacing the current HTTP Basic implementation
- richer account management
- stronger API error handling
- further validation and domain constraints
- most likely, a way to sign in through a google account. 

### Database and Data Management

Planned work includes:

- Flyway database migrations
- versioned schema evolution
- safer production database upgrades
- stronger database constraints
- larger and more interconnected world datasets
- more advanced querying and search

The long-term goal is to treat the stored world data as something that must be
managed, migrated, validated, processed, and potentially indexed rather than simply
persisted through CRUD endpoints.

### DevOps

Archivum is also intended to become a practical environment for learning deployment
and operational engineering.

Potential work includes:

- improved Dockerisation
- environment-specific configuration
- CI/CD pipelines
- automated test execution
- deployment pipelines
- secrets management
- health checks
- application logging
- monitoring and metrics
- container orchestration, potentially with Kubernetes

### DataOps / Data Processing

As the amount and complexity of stored world data grows, Archivum may introduce
data-oriented infrastructure such as:

- asynchronous processing
- event-driven workflows
- data validation pipelines
- indexing and search
- import/export pipelines
- derived metadata and statistics
- background jobs
- audit/event data
- potentially message-based infrastructure such as Kafka where it solves a real
  architectural problem

These components will be introduced only when the application has a concrete need
for them rather than simply to increase the technology count. I'm also currently following courses on this at university, stgill at the beginning of the semester, I'm still accumulating knowledge. 

## Why Archivum?

Archivum began as a relatively conventional Spring Boot CRUD backend, but its purpose
has gradually shifted. I started the project whilst doing a Udemy course on Spring Boot. 

I use the project as a long-running engineering environment where increasingly complex
problems can be introduced without repeatedly starting disposable tutorial projects.

For example, adding multiple users created the need for authentication and ownership.
Ownership introduced authorization boundaries and integration testing. World deletion
introduced transactional consistency and dependent-resource lifecycle management.
Future schema evolution creates a natural reason to introduce database migrations.

This allows the architecture to grow in response to actual requirements rather than
being designed around technologies in isolation.

it also works hand in hand with my courses, reinforcing what I'm learning, as right now I have : Software Testing, Concurrent Programming, Big Data and a course focused on a full-stack project. 

