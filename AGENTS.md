# Repository Guidelines

## Project Structure & Module Organization

The Gradle-based Spring Boot application is in `demo/`. Production code is in
`demo/src/main/java/com/example/demo/`, resources in `demo/src/main/resources/`,
and tests in `demo/src/test/java/`. Build files are `demo/build.gradle` and
`demo/settings.gradle`.

## Architecture References & Response Language

For backend design, implementation, or review, read
`docs/AGENTS.backend.template.md` and `docs/ARCHITECTURE.md` before making
relevant decisions. Apply them only when the task concerns backend behavior or
structure. Write user-facing explanations, questions, plans, review comments,
and summaries in Korean; preserve code, identifiers, and quoted text unchanged.

## Agent Skills

Use the skill definitions in `.codex/skills/` when performing relevant workflows:
- Apply `karpathy-guidelines` whenever planning, writing, or refactoring code.
- Apply `commit` when staging changes or generating commit messages.
- Apply `PR` when preparing pull-request titles and bodies.

## Build, Test, and Development Commands

Run commands from `demo/`:

- `./gradlew bootRun` starts the application.
- `./gradlew test` runs the JUnit test suite.
- `./gradlew build` runs tests and builds the artifact.
- `./gradlew clean` removes generated output.

Use the included Gradle wrapper. The project targets Java 17.

## Coding Style & Naming Conventions

Follow the existing Java and Gradle style: tabs, declaration-line braces, and
one public top-level class per file. Use PascalCase classes, camelCase methods
and fields, and lowercase packages. Prefer constructor injection. Keep settings
in `application.properties` and use kebab-case property keys.

Make the smallest change that satisfies the request. State material assumptions,
avoid speculative abstractions, and leave unrelated code untouched. Define a
verifiable outcome before multi-step work, then run the relevant check.

## Testing Guidelines

Tests use JUnit Jupiter. Name classes `*Tests` and methods for the behavior,
such as `createsOrderWhenInputIsValid()`. Add focused unit tests; use
`@SpringBootTest` only for required context or integration behavior. Run
`./gradlew test` before submitting changes.

## Commit & Pull Request Guidelines

Commit messages are concise English imperatives, such as `Add health endpoint`.
Do not use a type prefix or scope; capitalize the opening verb. Keep each
commit focused on one change.

Pull-request titles use `Type: Description`: English, imperative, capitalized,
without scope or a trailing period. Allowed types are `Feat`, `Fix`, `Add`,
`Remove`, `Refactor`, `Docs`, `Chore`, `Test`, `Style`, and `Implement`.
Pull-request bodies are Korean and preserve every section in
`.github/PULL_REQUEST_TEMPLATE.md`. Summarize implementation units in `작업 사항`;
record only actually run commands and results in `테스트 여부`; use `없음` in
`리뷰어에게 한마디` unless meaningful review context exists. Link relevant issues
and include API examples or screenshots for user-visible changes.

## Configuration & Security

Never commit secrets, database credentials, or environment-specific values.
Use local environment variables or ignored configuration files for sensitive
settings, and document required variables in the pull request or project docs.
