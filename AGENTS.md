# AGENTS.md

## Purpose
- Fast-start guide for AI coding agents working in this Spring Boot starter (`store`) project.
- Prefer project-observed patterns over generic Spring conventions.

## Big Picture Architecture
- Entry point: `src/main/java/com/codewithmosh/store/StoreApplication.java`.
- Package layout is role-based under `com.codewithmosh.store`: `controllers`, `entities`, `repositories`.
- Current HTTP surface is intentionally minimal:
  - MVC page route `GET /` in `HomeController#index`, rendered by `templates/index.html`.
  - REST route `GET /messages` in `MessageController#hello`, returns plain text.
- No service layer exists yet; domain behavior currently lives in entities and future controller/repository usage.

## Persistence + Data Flow
- ORM stack: Spring Data JPA + Hibernate (`pom.xml`).
- DB: MySQL (`mysql-connector-j`) with Flyway migrations enabled.
- Schema source of truth: `src/main/resources/db/migration/` (`V1__initial_migration.sql`, `V2__create_messages_table.sql`).
- Entities map 1:1 to tables (`User`, `Address`, `Profile`, `Product`, `Category`, `Message`).
- Relationship patterns to preserve:
  - `User` -> `Address` is bidirectional one-to-many with helper methods `addAddress/removeAddress`.
  - `Profile` uses shared primary key one-to-one via `@MapsId`.
  - `User` <-> `Product` favorites via join table `wishlist`.

## Project-Specific Conventions
- Repositories are interface-only Spring Data types in `repositories/`:
  - `CrudRepository` for most entities; `JpaRepository` for `ProductRepository`.
- Lombok is heavily used in entities (`@Builder`, `@Getter`, `@Setter`, constructors).
- Keep lazy relationships out of string rendering (`Address` excludes `user` from `toString`).
- Keep column/table names explicit with JPA annotations (`@Table`, `@Column`, `@JoinColumn`).

## Critical Workflows
- Run app (wrapper): `./mvnw spring-boot:run`.
- Run tests: `./mvnw test` (note: `@SpringBootTest` loads full context and uses configured datasource).
- Apply DB migrations via Maven plugin: `./mvnw flyway:migrate`.
- Flyway plugin has `cleanDisabled=false` in `pom.xml`; treat `flyway:clean` as destructive.

## Integration Points and Gotchas
- Runtime datasource is in `src/main/resources/application.yaml` (local MySQL `store_api`).
- Flyway Maven plugin credentials in `pom.xml` differ from app credentials in `application.yaml`; keep them intentionally aligned when changing environments.
- `script.sql` appears to be older bootstrap SQL (e.g., `zipcode` vs entity/migration `zip`); prefer Flyway migrations for current schema.
- Add schema changes as new migration files (`V3__...sql`), do not rewrite existing applied migrations.

## Where to Extend Next
- New API endpoints: add controller methods in `controllers/`, then introduce repository usage.
- New persistent models: add entity + repository + forward-only Flyway migration in same change.
- UI adjustments for `/`: update `HomeController` model attributes and `templates/index.html` together.

