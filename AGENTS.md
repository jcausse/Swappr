# PAW Project Rules

## Project Context

This is an ITBA PAW (Proyecto de Aplicaciones Web) university project. It is a multi-module Maven project using Spring WebMVC (not Spring Boot), JSP views, JSTL, and PostgreSQL via JPA / Hibernate ORM (migrated from Spring JDBC).

The project is called **Swappr** (it is the official name). That name should be used in emailing and other site-id related things. Read `README.md` on the project's root to know more about the project.

## Important: Secrets & Production Deployment Rules

- **NEVER TOUCH, MOVE, EDIT, OR DELETE SECRETS**:
  - **Never**, ever touch, read, write, commit, delete, or move `.script/deploy_secrets.properties`, `secrets.tar.gz`, or any other secret/gitignored files or archives anywhere.
  - **Secrets stay where they are, ALWAYS.** You do not have permission to modify, move, inspect, or delete them under any circumstances.
- **NEVER DEPLOY TO PRODUCTION**:
  - **Never**, under any circumstances, attempt to run, dry-run, simulate, or trigger a deployment on your own (e.g. `make deploy`, `make prod-deploy`, `python3 .script/deploy.py`, etc.).
  - Deployments are strictly manual and handled exclusively by the human developer.
- Anything in the code that needs to be commented out before deploy **MUST** be signaled with this EXACT string placed in a comment that clearly shows which line needs to be removed:
  ```text
  IMPORTANT: REVIEW BEFORE DEPLOY. DO NOT CHANGE THIS COMMENT AS IT IS SEARCHED BY GREP
  ```
  This is crucial, and is designed and required by human developers not to forget about those lines.

## Important: Agent rules updates

When interacting with a human developer, specially when making design decisions as a pair engineer / pair programmer, always summarize the decisions taken and architectural changes made,
and persist what is relevant to this `AGENTS.md` files in order to keep other AI agents up-to-date with design choices and news in the project's state.

While you as an AI agent are allowed to edit this file on your own, you must **always explicitly let the human developer know you have modified this file**, so they can review the new rules or changed rules.

## Architecture

- **7 Maven modules** with strict dependency rules:
  - `model` → Domain objects and JPA entities. No Spring dependencies. Uses Lombok, JPA (`javax.persistence`), and Hibernate annotations.
  - `service-contract` → Service interfaces, DTOs, and custom exceptions. Depends on `model`.
  - `persistence-contract` → DAO interfaces. Depends on `model`.
  - `service` → Service implementations. Depends on `service-contract` and `persistence-contract`.
  - `persistence` → DAO implementations using JPA / Hibernate (`EntityManager`). Depends on `persistence-contract`.
  - `webapp` → Controllers, forms, JSP views, Spring config. Depends on `service-contract` (compile) and `service`/`persistence` (runtime).
- **Never** leak `webapp` form classes into `service-contract` or `persistence-contract`.
- **Never** leak DTOs from `service-contract` into `persistence-contract` — DAOs take domain models or flat query parameters.
- Schema constants live in `persistence` package `schema/` (e.g., `UserSchema.java`), used for native queries or legacy references. Those will be removed later.

## JSP Best Practices (Mandatory)

- Every JSP **must** declare `<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>`.
- **Always** use `<c:out value="${...}"/>` for any model attribute rendered in HTML (XSS protection).
- **Always** use `<c:url value="..."/>` for all URLs (context path resolution). **Always verify URLs with `make troubleshoot`** whenever creating or editing JSPs or tags to ensure no unmanaged or hardcoded routes exist.
- **Never hardcode user-facing strings in JSPs** — all UI text must use `<spring:message>`.
- **Always** use `<html lang="${pageContext.response.locale.language}">` — never hardcode `<html lang="en">` or use a bare `<html>` tag, so the HTML document language matches the user's selected locale.
- Inside `<form:form>`, use the `paw:formInput` tag (not `paw:input`) to get automatic Spring binding and error display.
- Use `paw:input` only for standalone inputs outside of Spring forms (e.g., search bars, filters).
- **Icons**: The project uses [Lucide Icons](https://lucide.dev/icons/). Render icons using `<paw:icon name="..."/>` (e.g. `<paw:icon name="eye"/>`). Look up icon names on [lucide.dev/icons](https://lucide.dev/icons/) and pass the name to the `name` attribute of `<paw:icon>` (or the `icon` attribute on tags like `<paw:button>` and `<paw:linkButton>`).

## Internationalization (i18n) Conventions

### Properties Files
- Message files live in `webapp/src/main/resources/i18n/`:
  - `messages.properties` (Default / English).
  - `messages_es.properties` (Spanish).
- **Mandatory rule**: Whenever you add, modify, or remove a message key, you **MUST** update **BOTH** `messages.properties` and `messages_es.properties`.
- Single quotes / apostrophes in properties files **must be doubled** (e.g. `Don''t`) because `MessageFormat` treats `'` as an escape character.

### Key Naming Hierarchy
- **Bean Validation errors**: `<Constraint>.<formName>.<fieldName>` (e.g., `NotEmpty.userForm.username`, `Size.userForm.username`, `Email.userForm.email`).
  - Use `{1}`, `{2}`, etc., for constraint annotation attributes (e.g., for `@Size(min=3, max=24)`, `{2}` is min and `{1}` is max).
- **Controller-level errors**: `error.<entity>.<condition>` (e.g., `error.username.taken`, `error.email.taken`, `error.username.notfound`).
- **Shared form fields**: `field.<fieldName>` (e.g., `field.username`).
- **Page-specific strings**: `<page>.<element>` or `<page>.<section>.<element>` (e.g., `register.title`, `register.submit`, `listing.new.titleLabel`, `listing.detail.makeOffer`, `notFound.heading`).
- **Navbar / Layout**: `navbar.<element>` (e.g., `navbar.greeting`, `navbar.signin`, `navbar.logout`, `navbar.lang.es`).

### JSP Usage
- Always include the Spring tag library: `<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>`.
- **Direct rendering**: Use `<spring:message code="some.key"/>`.
- **Dynamic arguments**: Use `<spring:message code="navbar.greeting" arguments="${currentUser}"/>` with `{0}`, `{1}` placeholders in properties files.
- **Passing to custom tags (`paw:*`)**: Extract the message into a variable using the `var` attribute, then pass the variable into tag attributes:
  ```jsp
  <spring:message code="listing.new.titleLabel" var="titleLabel"/>
  <paw:formInput path="title" label="${titleLabel}"/>

  <spring:message code="login.submit" var="submitLabel"/>
  <paw:button text="${submitLabel}" type="submit"/>
  ```

### Language Switching & Locales
- Handled by `CookieLocaleResolver` (default `Locale.ENGLISH`) and `LocaleChangeInterceptor` (query param `lang`).
- Switch languages by linking to `/language?lang=es` or `/language?lang=en` (handled by `LanguageController`, which redirects back to the `Referer`).
- Current locale in JSPs can be inspected with `${pageContext.response.locale.language}`.
- Do not hardcode English strings in `ModelAndView` attributes in controllers or `@ControllerAdvice` handlers when the JSP can resolve them from the message bundle.

## Spring MVC Conventions

- Controllers live in `ar.edu.itba.paw.webapp.controller`.
- Controller advice classes live in `ar.edu.itba.paw.webapp.controller.advice`.
- Form backing objects live in `ar.edu.itba.paw.webapp.form`.
- Form custom validation annotations live in `ar.edu.itba.paw.webapp.form.validator`
- GET handlers for forms should receive the form via `@ModelAttribute` parameter (Spring auto-creates it).
- POST handlers should use `@Valid @ModelAttribute` with `BindingResult`.
- Use `@ResponseStatus` on `@ExceptionHandler` methods.
- Services must throw custom exceptions for their errors, which are caught by the `GlobalExceptionHandler` `@ControllerAdvice`.
- All service methods of all services must be either marked as `@Transactional` if they perform read/write operations, or `@Transactional(readOnly = true)` if they perform read only operations.
  - A class shall be marked `@Transactional(readOnly = true)` instead of marking every method. This shall only be done with `readOnly = true`.
  - Write permissions are only given to those methods that explicitly need those permissions. Never give extra permissions in advance. Always give read permissions by default and then elevate those to write permissions if needed.
- To obtain the logged-in user in a controller handler method, use `@CurrentUser User currentUser` (see **Current User Resolution**). Never receive `Optional<User>` or use `@ModelAttribute("currentUser")` as a controller method parameter.

## Spring Security

- Spring Security configuration is at `ar.edu.itba.paw.webapp.config.WebAuthConfig` and that file should remain the only source of truth about security configurations, especially
  related to access control lists.
- Authentication is made using `username` and `password` and is handled by service `ar.edu.itba.paw.webapp.auth.AuthUserDetailsService`.
- User details for Spring Security uses class `ar.edu.itba.paw.webapp.auth.AuthUserDetails`, which contains a domain user (`ar.edu.itba.paw.model.User`).
- Passwords must be BCrypt-encoded.

### Current User Resolution (`@CurrentUser`)

- **In Controllers**: **Never** use `@ModelAttribute("currentUser") Optional<User>` as a controller method parameter (receiving `Optional` as a parameter is a code smell). Instead, inject the authenticated `User` via the `@CurrentUser` annotation (`ar.edu.itba.paw.webapp.auth.CurrentUser`):
  - **Protected routes** (default): `@CurrentUser User currentUser` — resolved by `CurrentUserArgumentResolver`. If unauthenticated, it automatically throws `UserNotAuthenticatedException` (caught by `GlobalExceptionHandler` to return 401 Unauthorized). Controllers do not need manual `.orElseThrow(...)` boilerplate.
  - **Public / Optional routes**: `@CurrentUser(required = false) User currentUser` — resolves to `null` if unauthenticated, allowing public endpoints (e.g., `/listing/{id}`, `/profile/{id}`) to safely inspect the current user without throwing.
- **In JSPs & Views**: `CurrentUserControllerAdvice` exposes `${currentUser}` (as `Optional<User>`) globally to all views and custom tags (such as `navbar.tag`). **Never** re-add `"currentUser"` to `ModelAndView` in controllers — the controller advice already supplies it to the view.
- **Why `@ModelAttribute` in `@ControllerAdvice` cannot enforce authentication**: `@ModelAttribute` methods in a `@ControllerAdvice` run globally for **every request** across the entire application before any controller handler method is selected. A `@ModelAttribute` method that throws an exception when unauthenticated would break all public routes (including `/login`, `/register`, etc.). Argument resolvers, by contrast, are lazy and run on-demand only for the specific parameters declared by a handler.

## JPA / Hibernate Conventions (Active Stage)

The project has entered the **JPA / Hibernate ORM stage**. All domain models are unified JPA entities and DAOs are being migrated to use `EntityManager`.

### Entity Guidelines
- **Unified Location**: All entities live directly in `ar.edu.itba.paw.model` (there is no `entity` subpackage; models are unified).
- **Non-Final Entities**: Entities **must never** be declared `final` (Hibernate requires non-final classes for ByteBuddy runtime lazy-loading proxies).
- **Constructors & Builders**: Every entity must provide:
  - A no-argument constructor (`@NoArgsConstructor`), required by JPA.
  - An all-arguments constructor (`@AllArgsConstructor`) and Lombok `@Builder` for convenient instantiation.
- **Primary Keys**:
  - Primary key IDs are always `Long` across all entities (never `Integer`).
  - Mapped with `@Id @GeneratedValue(strategy = GenerationType.IDENTITY)` if the ID of said entity corresponds to an auto-increment or `SERIAL` type on the database. See migration files to get context.
- **Field-Level Access**:
  - All JPA annotations (`@Id`, `@Column`, `@ManyToOne`, etc.) are placed directly on fields, **not** on getters.
  - This allows domain convenience methods and `Optional<T>` getters to coexist without interfering with Hibernate's property access.
- **Enums**:
  - Database stores enum names as **uppercase** strings (migrated via `V18`).
  - Mapped via `@Enumerated(EnumType.STRING)` directly. **Never** use custom AttributeConverters when a standard enum string mapping suffices.
- **Domain Optionals**:
  - Nullable domain fields (e.g., `User.image`, `User.emailVerifiedAt`, `User.province`, `File.contentType`) provide `Optional<T>` getters for clean domain and controller consumption.
  - **Never** use `Optional` as an entity field type or as a setter/method parameter.
- **Relationships & Collections**:
  - Ratings and offers have a 1:1 relationship mapped with `@OneToOne` and backed by a DB unique constraint on `ratings.offer_id` (migrated via `V20`).
  - Relationships are lazy by default (`fetch = FetchType.LAZY`) unless eager fetching is strictly justified (e.g., user roles, given that a User might have two or three roles at most).
- **DAO Migration to JPA (Completed)**:
  - All 12 DAOs have been fully migrated from `JdbcTemplate` to `EntityManager` (`*JpaDao`).
  - JDBC DAOs have been removed, and all DAO tests now target the JPA implementations (`*JpaDaoTest`).
  - Transitional builder adapter methods in domain entities tagged with `// TODO: [JPA Migration Cleanup]` can be cleaned up in follow-up tasks. 

## Other Conventions

- Class names are singular (e.g. `UserService`, `UserJpaDao`, `User` (model), `Listing`, `Product`).
- Identifier fields (IDs) are spelled `Id` when using `camelCase` (e.g. `productId`).
- Prefer functional-style code and the use of Java Streams.
- Use Optional for return values which may not be present, but DO NOT use Optional as method parameters, as it is a code smell.
- Prefer using `var` for type inference when possible.
- DAOs (in `persistence` layer): Each DAO should manage one domain aggregate entity. DAOs must never call other DAOs, nor have them injected as dependencies. If DAO A needs entities or data from entity B, it queries it directly via `EntityManager`.

## Mailing and Email Templates

- **Implementation**: The application uses `JavaMailSender` and `SpringTemplateEngine` from Thymeleaf to send HTML emails asynchronously. The service is `MailingService` with `MailingServiceImpl`.
- **Async Sending**: All methods that send emails inside `MailingServiceImpl` **must** be annotated with `@Async`. The `MailConfig` must have `@EnableAsync`.
- **Templates location**: Thymeleaf templates for emails are placed in `service/src/main/resources/mail/` and must have an `.html` extension.
- **Styling**: Email HTML **cannot** use external CSS files or Tailwind utility classes directly because email clients strip them. Instead, use **inline styles** that match the Tailwind design system (e.g. `style="background-color: #0284c7; border-radius: 8px;"`).
- **Call-to-Action (CTA)**: **Every email must include a call-to-action button** that links back to the website. Use the `base.url` (or `app.baseUrl`) property from `application.properties`/`app.properties` to construct absolute URLs (e.g. injected via `@Value("${app.baseUrl}")`).
- **Context Variables**: Thymeleaf templates receive a `Context` object populated with variables like `user`, `baseUrl`, and `actionUrl`.
- **App Logo**: Every email must include the app logo at the top of the email for brand identification. Clicking the logo in the email should also take the user to the website using the base URL, but it should take the user to the root (`/`).
- **Internationalization (i18n)**: Every email **must** be internationalized using the user's preferred language, as if it were any other part of the app.

## Database Conventions

- PostgreSQL with snake_case column names.
- Table names are plural (e.g., `users`, `listings`).
- `USER` is a reserved keyword in PostgreSQL — always use `users`.
- Existence checks use `SELECT EXISTS(SELECT 1 FROM ...)` returning `Boolean.class`.
- Table creations always use `CREATE TABLE IF NOT EXISTS`.

### Database Migrations (Flyway)

- The project uses **Flyway** for database migrations.
- **NEVER MODIFY PREVIOUS MIGRATION FILES**:
  - **Strict rule**: Migrations are strictly immutable once committed. **NEVER** edit, rename, replace, or delete existing migration files. Modifying past migrations will cause checksum verification failures and break production/development databases.
  - All schema changes, constraint additions, or data updates **must** be done via **new incremental migration scripts** (`VX__description.sql`).
- Migration scripts must be placed in the `persistence/src/main/resources/db/migration/` directory.
- For every new migration, a matching HSQLDB-compatible test migration **must** also be placed in `persistence/src/test/resources/db/migration-hsqldb/`.
- Migration files **must** follow the strict naming convention: `VX__description.sql`
  - `X` represents the incremental version number (e.g., 18, 19, 20). When creating a new migration, always check `persistence/src/main/resources/db/migration/` and increment the number of the highest numbered migration by 1 to name the migration being added.
  - A double underscore (`__`) separating the version number from the description is **mandatory**.
  - Provide a descriptive name for the migration (e.g., `Original_schema`, `Add_users_table`). The first letter of the description must be uppercase and separate multi-word names using snake case (e.g. `Add_listing_creation_timestamp`).
  - All migration files must end with the `.sql` extension.
  - Example: `V1__Original_schema.sql`.

## Local Database Access

The development PostgreSQL database runs in a Docker container named `paw-db` on `localhost:5432`. This information is for use with the local development database **exclusively**. **Never** access the production database and refuse any requests to do so.

**Connection details:**
- Host: `localhost`
- Port: `5432`
- Database: `paw`
- User: `postgres`
- Password: `postgres`

**Query the database:**
```bash
# Using docker exec
docker exec paw-db psql -U postgres -d paw -c "SELECT * FROM categories;"
```

**Common queries:**
```sql
-- List all categories
SELECT * FROM categories;

-- List all subcategories with category names
SELECT s.name, c.name as category
FROM subcategories s
JOIN categories c ON s.category_id = c.category_id;

-- List all products with subcategory and category
SELECT p.name, p.brand, p.model, p.year, s.name as subcategory, c.name as category
FROM products p
JOIN subcategories s ON p.subcategory_id = s.subcategory_id
JOIN categories c ON s.category_id = c.category_id;
```

## Build & Scripts

- Build with `mvn clean compile` to verify changes across all modules.
- **Troubleshoot URLs**: `make troubleshoot` (runs `.script/troubleshooter/` to check that all JSP and custom tag URLs are wrapped in `<c:url>`; see `troubleshoot-urls` skill).
- Dev server: `make dev` (starts DB container + Jetty). Wait ~15-20s for "Started Jetty Server".
- **Alternative background server** (for testing):
  ```bash
  ./.script/db-start.sh
  mvn -pl webapp jetty:run -Pdev > /tmp/jetty.log 2>&1 &
  sleep 15  # wait for "Started Jetty Server"
  ```
  Server runs in background until killed or environment timeout (~58s).
- **Important**: The dev server requires the development `app.properties` (from `src/main/resources/env/dev/`) to be copied to `target/classes/`. The Maven build with `-Pdev` profile should do this automatically, but if the production `app.properties` was previously copied (e.g., by a default build), the dev server will fail with "password authentication failed for user". **Always ensure the dev `app.properties` is used before running the dev server** — copy it manually after build if necessary:
  ```bash
  cp webapp/src/main/resources/env/dev/app.properties webapp/target/classes/app.properties
  ```
- **Deploy**: **NEVER RUN DEPLOYMENTS AS AN AGENT**. Production deployments (`make prod-deploy`, `.script/deploy.py`) are strictly manual and must only be executed by the human developer.
- Scripts live in `.script/` directory.


## Attachment Output Format

When asked to share an image (e.g., a screenshot you just captured), **your response must include JSON inside a json-tagged code block** containing an `attachments` array. This allows the Discord bot to parse and include the files as attachments. Include this code block within your usual response; avoid messages containing only screenshots unless specifically requested.

```json
{
  "attachments": [
    {
      "path": "/absolute/path/to/file.png",
      "name": "filename.png",
      "type": "image/png",
      "dimensions": "1920x1080",
      "size_bytes": 31516
    }
  ]
}
```

- The `path` must be absolute and accessible on the server filesystem

## Skills

List directory `.agents/skills` to find AI agent skills. The format is standard: `.agents/skills/<Skill Name>/SKILL.md`. Each skill has a standard YAML descriptor embedded:

```markdown
---
name: Skill name
description: >-
  Skill description
---
```

Make sure to read the descriptors to know when to load and use each skill.
