# Backend Architecture

## Technical baseline

- Spring Boot 4.1.1, Java 17, Gradle, executable JAR
- Spring MVC, Spring Data JPA, Flyway, PostgreSQL, Lombok
- Spring Boot-managed dependencies do not declare individual versions without a documented compatibility reason.

## Package structure

Organize code by feature first, then by layer.

```text
com.projectx.backend
├── global/                 # common response, exception, configuration
└── {feature}/              # e.g. user, storage, ingredient
    ├── api/                # Controller, Request/Response DTO
    ├── application/        # Service, Command
    ├── domain/             # Entity, Value Object, domain rules
    └── infrastructure/     # JPA persistence and external integrations
```

## Layer responsibilities

- **Controller**: validates HTTP input, converts Request DTOs, and creates responses. It does not contain business logic or use JPA Entities directly.
- **Application Service**: coordinates a single use case, controls transactions, and calls repositories or external integrations.
- **Domain**: owns business rules, state changes, and invariants.
- **Infrastructure**: implements persistence and external technical concerns.

## Domain model

- JPA Entities do not use `@Setter`.
- Use a protected no-args constructor: `@NoArgsConstructor(access = AccessLevel.PROTECTED)`.
- Create an Entity through a named static factory method such as `Ingredient.create(...)`.
- Keep required-value checks and creation rules in the Entity.
- Change state only through methods with an explicit intent, such as `changeName(...)` or `updateExpiryDate(...)`.
- Do not use `@Builder` on Entities. Builders are allowed only for test fixtures or input objects whose optional fields make construction unclear.
- Do not return JPA Entities from an API.

## API DTO and service input

- Request and Response DTOs use Java `record`.
- Request DTOs own Bean Validation annotations such as `@NotBlank`, `@Size`, and `@Valid`.
- Convert a Request DTO to a use-case-specific Command before calling a Service.
- Commands are immutable Parameter Objects, not GoF Command pattern objects. Do not introduce `execute()`, a command interface, undo/redo, or queues unless the feature needs them.
- A single-value query may receive its value directly; use a Command when an input represents a meaningful group of values.

```text
Request DTO → Command → Application Service → Entity domain method → Response DTO
```

## API, transactions, and errors

- Controller endpoints return `ResponseEntity<ApiResponse<T>>` and use `ApiResponse.onSuccess()` for successful responses.
- Write use cases use `@Transactional`; read-only use cases use `@Transactional(readOnly = true)`.
- Use constructor injection with `@RequiredArgsConstructor`; do not use field `@Autowired`.
- Express expected business failures with `BusinessException` and `ErrorCode`, not raw `RuntimeException`.
- Use `@Slf4j`; do not use `System.out.println()` or log secrets.

## Database and configuration

- Flyway is the source of truth for schema changes.
- Store migrations in `src/main/resources/db/migration` as `V{version}__{description}.sql`.
- Do not depend on `ddl-auto` for production schema management.
- Keep secrets out of `application.yml`; provide them through environment variables.
- Maintain `local`, `test`, and `prod` profiles. DevTools is local-only.

## Testing

- Use `spring-boot-starter-test` as the common test base.
- Test controllers with `@WebMvcTest` and MockMvc.
- Test JPA repositories with `@DataJpaTest`.
- Verify Flyway migrations against a test database.
- Use `@SpringBootTest` only when a full application context is required.
