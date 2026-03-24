# MailNest

MailNest is a backend service for managing email subscriptions, inspired by the architecture and practices from *Zero to Production in Rust*, implemented using Java and Spring Boot.

The goal of this project is to learn how to build **production-ready systems** with proper testing, structure, and reliability.


## Tech Stack

* Java 24+
* Spring Boot (4.x)
* Gradle (Kotlin DSL)
* JUnit 5
* Spotless (formatting)
* SpotBugs (static analysis)
* OWASP Dependency-Check (security scanning)


## Running the Application

```bash
./gradlew bootRun
```

App will start on:

```
http://localhost:8080
```


## Development Workflow

Typical loop:

```bash
./gradlew spotlessApply
./gradlew test
./gradlew build
```


## Roadmap

* [x] Health check endpoint
* [x] Integration testing setup
* [x] Subscription endpoint (validation)
* [x] PostgreSQL integration
* [ ] Email sending (MailHog)
* [ ] Confirmation tokens (double opt-in)
* [ ] Production configuration
* [ ] CI/CD pipeline


## Inspiration

This project is inspired by:

* *Zero to Production in Rust* by Luca Palmieri

The aim is to apply the same engineering principles in the Java ecosystem.

---

