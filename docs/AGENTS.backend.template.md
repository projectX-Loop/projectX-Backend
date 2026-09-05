# Backend agent rules

- Follow `docs/ARCHITECTURE.md`.
- Write explanations and review comments in Korean; preserve quoted code and identifiers.
- Do not use `@Setter` or `@Builder` on JPA Entities.
- Create Entities with named static factory methods and change them through explicit domain methods.
- Keep Controllers thin. Use Request/Response records and convert multi-value service input to Command records.
- Commands are Parameter Objects, not GoF Commands. Do not add `execute()`, undo/redo, queues, or command interfaces without a requirement.
- Do not return JPA Entities from APIs. Use `ResponseEntity<ApiResponse<T>>`.
- Use constructor injection, `@Transactional` correctly, `BusinessException`/`ErrorCode`, `@Slf4j`, and Flyway migrations.
- Do not put secrets in configuration files. Do not rely on `ddl-auto` for production schema changes.
