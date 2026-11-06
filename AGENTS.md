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
- **Never** import or throw Spring Security authentication/authorization classes (e.g., `AccessDeniedException`, `Authentication`, `SecurityContextHolder`) in `service` or `service-contract` (`service` only depends on `spring-security-crypto` for `PasswordEncoder`, never `spring-security-core`). Services must throw custom `service-contract` exceptions (such as `ForbiddenException`), while `GlobalExceptionHandler` in `webapp` handles both `ForbiddenException` (from `service`) and `AccessDeniedException` (from `@PreAuthorize` in `webapp`).

## JSP Best Practices (Mandatory)

- Every JSP **must** declare `<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>`.
- **Always** use `<c:out value="${...}"/>` for any model attribute rendered in HTML (XSS protection).
- **Always** use `<c:url value="..."/>` for all URLs (context path resolution). **Always verify URLs with `make troubleshoot`** whenever creating or editing JSPs or tags to ensure no unmanaged or hardcoded routes exist.
- **Never hardcode user-facing strings in JSPs** — all UI text must use `<spring:message>`.
- **Always** use `<html lang="${pageContext.response.locale.language}">` — never hardcode `<html lang="en">` or use a bare `<html>` tag, so the HTML document language matches the user's selected locale.
- Inside `<form:form>`, use the `paw:formInput` tag (not `paw:input`) to get automatic Spring binding and error display.
- Use `paw:input` only for standalone inputs outside of Spring forms (e.g., search bars, filters).
- **Icons**: The project uses [Lucide Icons](https://lucide.dev/icons/). Render icons using `<paw:icon name="..."/>` (e.g. `<paw:icon name="eye"/>`). Look up icon names on [lucide.dev/icons](https://lucide.dev/icons/) and pass the name to the `name` attribute of `<paw:icon>` (or the `icon` attribute on tags like `<paw:button>` and `<paw:linkButton>`).
- **Auth pages** (login, register, email verification, password recovery) must use `<paw:authLayout>` (`authLayout.tag`), which renders the shared banner background, logo, title, card container and "Return to Home" button. Pass page-specific footer links via `linkHref`/`linkTextKey` (and optionally `secondaryLinkHref`/`secondaryLinkTextKey`); put the form inside the tag body.
- **Select option labels**: Controllers must not resolve select option labels with `MessageSource`. Instead, pass the message keys as option labels and set `translateLabels="true"` on `paw:formSelect` / `paw:formButtonToggle`, which resolves each label with `<spring:message>`. For plain string options whose label equals their value, pass a `List<String>` and use `plainStrings="true"` on `paw:formSelect` rather than building `StringSelectOption` pairs.

## Internationalization (i18n) Conventions

### Properties Files
- Message files live in `webapp/src/main/resources/i18n/`:
  - `messages.properties` (Default / English).
  - `messages_es.properties` (Spanish).
- **Mandatory rule**: Whenever you add, modify, or remove a message key, you **MUST** update **BOTH** `messages.properties` and `messages_es.properties`.
- **Apostrophes / single quotes depend on whether the message takes arguments** (`MessageSource` is configured with the default `alwaysUseMessageFormat = false`):
  - Messages **without** `{n}` placeholders are returned verbatim (never passed through `MessageFormat`), so use a **single** `'` (e.g. `You don't have any notifications yet.`). Doubling it would render `''` literally.
  - Messages **with** `{n}` placeholders go through `MessageFormat`, which treats `'` as an escape character, so the apostrophe **must be doubled** (e.g. `{0} didn''t accept your offer.`). A single `'` there is silently dropped and quotes out the rest of the message, including later placeholders.
  - When adding or removing placeholders from an existing message, re-check its apostrophes.

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
- **Never concatenate translated fragments** in JSPs or controllers. Put the placeholder(s) inside the message value and pass them via `arguments`, so each language controls word order.
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
- **Safe Redirects (Open Redirect Prevention)**: **Never** pass unvalidated user-controlled input (such as the `Referer` header or a `@RequestParam("redirect")` query/form parameter) directly to `"redirect:" + ...`. Always resolve dynamic redirect targets through the stateless `ar.edu.itba.paw.webapp.controller.RedirectHelper` utility (`RedirectHelper.resolveReferer(...)` for `Referer` headers and `RedirectHelper.resolvePath(...)` for relative redirect parameters, optionally with `RedirectHelper.hasPathPrefix(...)`). `RedirectHelper` returns only normalized context-relative paths (`path[?query]`) without the `"redirect:"` prefix, so controllers explicitly concatenate `"redirect:" + target`.

### Enum Binding

- Form and service DTO fields whose values come from a fixed set must use the corresponding domain enum (e.g. `Condition`, `ListingSort`, `ListingStatus`, `OfferType`, `OfferStatusGroup`, `RatingRole`, `OfferRating`, `Language`), never raw `String`.
- Global `String -> enum` conversion relies on Spring MVC's built-in default `StringToEnumConverterFactory` (enabled by `@EnableWebMvc`). **Do not register custom enum converters in `WebConfig` or use `@InitBinder` in controllers** for enum parsing. Empty input (`""`) converts to `null` so optional filters and `@NotNull` bean validation work naturally, while invalid values fail conversion (`400 Bad Request` on GET filters / `typeMismatch` field errors on POST forms).
- **Never change existing enum constant names or stored string values**, as some are persisted in the database.
- All JSP `<option>` / `<form:option>` / `<form:radiobutton>` values, query parameters, and redirect URLs bound to enum fields **must** use uppercase enum constant names (`name()`). Legacy lowercase enum query/form values are not supported.

### Product Discovery by Brand/Model

- `/listing/by-product` (`ListingController#product`) renders `listing/byProduct.jsp` using `ProductSearchForm` as `productForm`.
- It uses `CatalogService#getListingInfoSummary` and displays, per model, the price range for each condition that has active listings. The page is paginated by model (10 per page) following the 1+1 query model: the DAO paginates model IDs first and then aggregates listings for those IDs by condition.
- Category and subcategory are chosen from button grids (mirroring the landing page) with breadcrumbs to step back. Once both are selected, brand/model/year filters appear (`brandId`/`modelId`); brand changes auto-submit and clear the model. The filter clear action keeps category and subcategory intact.
- Each condition row links to `/listing` with category, subcategory, `brandId`, `modelId`, condition, and any year filters applied.

### Catalog: Brands and Models

- The legacy `products` table/entity/DAO were removed. Catalog data now lives in `Brand` and `Model` entities (`models` has `brand_id` and `subcategory_id`; `(model_name, brand_id)` is unique).
- `Listing` references a `Model` and stores its own `int year`. Listings **must not** store a redundant brand column: use the custom `Listing#getBrand()` getter, which delegates to `model.getBrand()`, and query through `l.model.brand` in JPQL/native SQL.
- `ListingFilter` brand/model filters use `brandId`/`modelId` (not names). `BrandDao#getBySubcategoryId` and `ModelDao#getBySubcategoryIdAndBrandId` expose the catalog.
- `ModelDao#getListingInfoSummary` paginates model IDs and then aggregates listings with the 1+1 query model; DTOs are `ModelListingInfo` / `ModelListingInfoSummary`. Its aggregation uses entity grouping (`GROUP BY brand, model, condition`), which relies on PostgreSQL functional dependency and is therefore intentionally **not** covered by the HSQLDB DAO tests.
- A single `CatalogService` (`CatalogServiceImpl`) owns all category/subcategory/brand/model reads and the model listing summary; there are no separate per-entity services. Listing creation/update DTOs carry `modelId` + `year`, and `ListingServiceImpl` resolves the model through `CatalogService` (never find-or-create catalog rows).
- Users only select existing brands/models when creating listings. Admin catalog management lives at `/admin/catalog`; brand management is implemented there through paginated `CatalogService#getBrands` with header-click ID/brand asc/desc sorting (`BrandSort`), plus `#getAllBrands` (non-paginated id/name options), `#createBrand`, `#renameBrand`, `#replaceBrand` and `#deleteBrand` (model management is still pending).
- Brand creation and rename must reject duplicate names, brand replacement must reject self-replacement and reassign all models to the target brand, and brand deletion must be rejected when any listing references one of the brand's models. `BrandDao#getWithStats` and `#hasListings` support the admin table and its disabled delete action. Dialogs that need form fields use the reusable `formDialog.tag` (body rendered inside the form), while destructive confirmations use `confirmDialog.tag`.
- Listing creation keeps `year` in the choose-product step for UX and passes `modelId` + `year` to the details step via hidden form fields. The choose-product form has no custom/"other" brand or model option; brand and model selects are populated from `CatalogService`, and the details page's "change" link rehydrates category, subcategory, brand, model and year.

## Spring Security

- Spring Security configuration is at `ar.edu.itba.paw.webapp.config.WebAuthConfig` and that file should remain the only source of truth about security configurations, especially
  related to access control lists.
- Authentication is made using `username` and `password` and is handled by service `ar.edu.itba.paw.webapp.auth.AuthUserDetailsService`.
- User details for Spring Security uses class `ar.edu.itba.paw.webapp.auth.AuthUserDetails`, which contains a domain user (`ar.edu.itba.paw.model.User`).
- `ar.edu.itba.paw.webapp.auth.AuthHelper` is the single source of truth in `webapp` for reading or mutating `SecurityContextHolder` / `AuthUserDetails` (`getCurrentUser`, `isAuthenticated`, `login`, `update`). Do not duplicate `SecurityContextHolder` / `instanceof AuthUserDetails` checks elsewhere, and keep `WebPermissionService` strictly focused on `@PreAuthorize` authorization predicates.
- Passwords must be BCrypt-encoded.

### Current User Resolution (`@CurrentUser`)

- **In Controllers**: **Never** use `@ModelAttribute("currentUser") Optional<User>` as a controller method parameter (receiving `Optional` as a parameter is a code smell). Instead, inject the authenticated `User` via the `@CurrentUser` annotation (`ar.edu.itba.paw.webapp.auth.CurrentUser`):
  - **Protected routes** (default): `@CurrentUser User currentUser` — resolved by `CurrentUserArgumentResolver`. If unauthenticated, it automatically throws `UserNotAuthenticatedException` (caught by `GlobalExceptionHandler` to return 401 Unauthorized). Controllers do not need manual `.orElseThrow(...)` boilerplate.
  - **Public / Optional routes**: `@CurrentUser(required = false) User currentUser` — resolves to `null` if unauthenticated, allowing public endpoints (e.g., `/listing/{id}`, `/profile/{id}`) to safely inspect the current user without throwing.
- **In JSPs & Views**: `CurrentUserControllerAdvice` exposes `${currentUser}` (as `Optional<User>`) globally to all views and custom tags (such as `navbar.tag`). **Never** re-add `"currentUser"` to `ModelAndView` in controllers — the controller advice already supplies it to the view.
- **Why `@ModelAttribute` in `@ControllerAdvice` cannot enforce authentication**: `@ModelAttribute` methods in a `@ControllerAdvice` run globally for **every request** across the entire application before any controller handler method is selected. A `@ModelAttribute` method that throws an exception when unauthenticated would break all public routes (including `/login`, `/register`, etc.). Argument resolvers, by contrast, are lazy and run on-demand only for the specific parameters declared by a handler.

## JPA / Hibernate Conventions

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
- **Boxed vs. Primitive Types**:
  - For non-nullable columns (`NOT NULL` in SQL, such as boolean flags like `is_read`, counters, or fixed numbers), always use Java primitive types (`boolean`, `long`, `int`). Never use boxed wrappers like `Boolean` for non-nullable columns, as they introduce an artificial third state (`null`) and risk `NullPointerException`s during unboxing.
  - Boxed wrapper types (`Long`, `Integer`, `Boolean`) are reserved strictly for:
    1. Nullable database columns (`NULL` in SQL represents absence).
    2. Auto-generated primary key IDs (`Long id`), which start as `null` before the entity is first persisted via `GenerationType.IDENTITY`.
- **Relationships & Collections**:
  - Ratings and offers have a 1:1 relationship mapped with `@OneToOne` and backed by a DB unique constraint on `ratings.offer_id` (migrated via `V20`).
  - In-site notifications (`SiteNotification`) belong to a `User` (`@ManyToOne(fetch = FetchType.LAZY)`), identified by surrogate primary key `notification_id SERIAL` (migrated via `V21` and internationalized via `V23`), storing message bundle keys (`title_key`, `body_key`, optional `action_label_key`) and ordered language-neutral template arguments (`@ElementCollection` on `site_notification_args`) so titles and bodies are resolved dynamically in the active request locale at render time rather than pre-rendered at creation time. They optionally reference an `Offer` (`@ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "offer_id")`, migrated via `V24`) to support offer state-dependent actions.
  - Relationships are lazy by default (`fetch = FetchType.LAZY`) unless eager fetching is strictly justified (e.g., user roles, given that a User might have two or three roles at most).
- **Embeddables & Value Objects**:
  - Domain value objects like `Price` are mapped as `@Embeddable` with a protected no-args constructor, `@AllArgsConstructor`, and Lombok `@Builder`.
  - Owning entities embed them with `@Embedded` (and `@AttributeOverride` where needed). In JPQL/HQL queries, navigate embeddable attributes using property dot notation (e.g., `l.price.amount`).

### Pagination (Mandatory)

- **Never paginate a JPQL/HQL entity query with `setFirstResult`/`setMaxResults` when it joins a collection or uses a fetch join.** Hibernate may silently fall back to in-memory pagination (warning `HHH000104`), loading the entire result set before slicing it, causing excessive memory use and severe performance degradation. This is one of the most heavily penalized mistakes in the course.
- **Use the 1+1 query model for every paginated query:**
  1. **ID query**: **always use a native query**, select only entity IDs, apply every filter and `ORDER BY`, and apply `setFirstResult`/`setMaxResults`. This query must not join collections, so one row always maps to one entity.
  2. **Population query**: fetch the entities with `WHERE e.id IN :ids`, without limit or offset, preserving the ID query order (re-apply the same `ORDER BY` or reorder in memory).
- Apply filters and ordering **only on the ID query**. The population query must not filter or paginate.
- Run a separate `SELECT COUNT(...)` query with the same filters to compute the total.
- Always use a deterministic order with a unique tie-breaker (e.g., `id`) when paginating.
- **Never paginate by traversing an entity collection** (`@OneToMany`/`@ManyToMany`). Collections are unbounded and load fully; query the owning "many" entity directly from its DAO instead.

### Query, Persistence and Mapping Principles

- Model associations as **object references** (e.g., `User creator`), never as raw IDs or collections of IDs.
- Keep associations **`LAZY`**; do not switch to `EAGER` without strict justification. Never traverse lazy associations after the transaction ends (e.g., during view rendering) — avoid `LazyInitializationException` by fetching what the view needs inside the transaction instead of relying on Open Session In View.
- Do not map the inverse `@OneToMany` side for potentially unbounded collections unless the relation truly requires it; `mappedBy` must reference the owning field on the "many" side. Never traverse these collections.
- Avoid N+1 queries and over-fetching: write explicit queries/fetch joins for exactly the data needed.
- Never return `null` collections; initialize them to empty collections.
- Use `@Enumerated(EnumType.STRING)`; never store enum ordinals.
- Choose `cascade`/`orphanRemoval` deliberately; do not enable broad cascading by default.
- Remember that dirty checking flushes changes automatically at transaction commit, even without an explicit save. Keep write intent explicit in services and avoid accidental hidden writes.
- Prefer standard JPA annotations/API. Use Hibernate-specific features (e.g., `@Formula`) only when there is no standard equivalent.
- Do not rely on `hibernate.hbm2ddl.auto=update` to manage the schema (best-effort and non-destructive); schema changes go through Flyway. Keep schema generation disabled or validated outside local development.
- Never leave SQL/Hibernate query logging enabled in production: it writes to stdout/Catalina logs and can fill the disk.
- Entities require a no-argument constructor (any visibility) and a nullable ID. Add JPA annotations only when overriding convention.
- When using inheritance, choose the strategy deliberately (`SINGLE_TABLE`, `JOINED`, `TABLE_PER_CLASS`) based on access patterns. For `SINGLE_TABLE`, subclass-only columns must be nullable and a discriminator column is required. Remember the database can accept rows that are invalid for the Java model.
- When using sequences, configure generator names explicitly; be aware that Hibernate otherwise uses one global default sequence.

## Other Conventions

- Class names are singular (e.g. `UserService`, `UserJpaDao`, `User` (model), `Listing`, `Product`).
- Identifier fields (IDs) are spelled `Id` when using `camelCase` (e.g. `productId`).
- Prefer functional-style code and the use of Java Streams.
- Use Optional for return values which may not be present, but DO NOT use Optional as method parameters, as it is a code smell.
- Prefer using `var` for type inference when possible.
- DAOs (in `persistence` layer): Each DAO should manage one domain aggregate entity. DAOs must never call other DAOs, nor have them injected as dependencies. If DAO A needs entities or data from entity B, it queries it directly via `EntityManager`.
- **Business rules belong in the model/services, not controllers**: Query logic such as "is this listing sold/canceled", "is this user the creator/buyer/seller", "can this offer be rated/edited" belongs on the domain model (`Listing`, `Offer`, `User`, etc.) as boolean helper methods. Services and controllers must call those helpers instead of re-implementing status or ownership comparisons. Core examples: `Listing.isActive()`, `Listing.isSoldForDisplay()`, `Listing.isCreator(...)`, `Offer.isPending()`, `Offer.isBuyer(...)`, `Offer.canUploadDocuments()`, `Offer.canBeRatedBy(...)`, `User.isSameUser(...)`, `User.matchesUsernameOrEmail(...)`.
- **DTOs carry raw input; services build domain value objects**: Service DTOs should store raw values coming from the web layer (e.g. `BigDecimal price`) and the service constructs value objects (`new Price(...)`) so that controllers do not perform domain transformations.
- **Testing DAO methods in isolation (Strict Rule)**: In DAO integration tests (`*JpaDaoTest`), **each test method must ONLY invoke and test that specific DAO method**:
  - **Golden rule: Unit tests must exercise one DAO method at a time.**
  - **Never use other DAO methods to verify write operations**: When testing a write operation (`create`, `update`, `delete`, `markAllAsReadForUser`), **never** assert or verify the result by calling other DAO methods (such as `dao.getById(...)` or `dao.getMatching(...)`). Always assert directly against the database using `JdbcTestUtils` (e.g. `JdbcTestUtils.countRowsInTableWhere(...)`) or `jdbcTemplate` queries (`queryForMap`, `queryForObject`). Using another DAO method to verify a write operation is a severe conceptual mistake in PAW evaluations.
  - **Seed test data directly**: When arranging fixtures for read methods (`getMatching`, `getById`), seed the database directly via SQL (`jdbcTemplate.update(...)`) or test SQL fixtures, rather than calling `dao.create(...)`. This ensures read tests remain completely decoupled from and unaffected by bugs in `create(...)`.
- **Important: never use `Mockito.verify()`**. The usage of `Mockito.verify()` is strictly forbidden by the PAW course rules. Tests should NEVER use it.
- **Commit messages**: Always prefix commit messages with a bracketed type tag in title case, e.g. `[Feat]`, `[Fix]`, `[Refactor]`, `[Test]`, `[Chore]`, followed by a capitalized imperative summary (e.g. `[Refactor] Extract SiteNotificationCreationDtoFactory in notification package`). Never use Conventional Commits style (`feat(scope): ...`). Never `git commit --amend` a commit that may have already been pushed or authored by someone else; check `git log`/`git status` against the remote first.
- **Commit and push when done**: After completing any requested changes, commit them and push the current branch without waiting for a separate instruction, unless the developer explicitly says otherwise.

## Notifications, Mailing, and Email Templates

- **Package Organization (`ar.edu.itba.paw.service.notification`)**: All notification service implementations (`NotificationDispatcherServiceImpl`, `SiteNotificationServiceImpl`, `EmailNotificationServiceImpl`) and the package-private `SiteNotificationCreationDtoFactory` live in `ar.edu.itba.paw.service.notification` (scanned automatically by `@ComponentScan` on `ar.edu.itba.paw.service`), while their public contracts (`NotificationDispatcherService`, `SiteNotificationService`, `EmailNotificationService`) live in `ar.edu.itba.paw.service` inside `service-contract`.
- **Unified Dispatch (`NotificationDispatcherService`)**: Domain services (`OfferServiceImpl`, `ListingServiceImpl`, `UserServiceImpl`, `PasswordRecoveryServiceImpl`) **must** trigger notifications exclusively through `NotificationDispatcherService` (`NotificationDispatcherServiceImpl`), never by calling `EmailNotificationService` directly. `NotificationDispatcherServiceImpl` stays strictly focused on dispatch orchestration (SRP): it runs synchronously on the caller's transactional thread, checks channel applicability via `NotificationType#supportsSite()` and `NotificationType#supportsEmail()` (designed to incorporate per-user notification preferences later), delegates DTO construction to package-private `SiteNotificationCreationDtoFactory` to persist language-neutral message keys (`titleKey`, `bodyKey`) and ordered `bodyArgs` via `SiteNotificationService#create` (never pre-rendered strings, so switching the UI locale immediately translates existing notifications), and snapshots immutable `*MailDto`s before delegating to `@Async` `EmailNotificationService`.
- **In-Site Notifications (`SiteNotificationService` & UI)**:
  - `SiteNotificationService` (`SiteNotificationServiceImpl`) stays strictly focused on `SiteNotification` persistence and UI inbox operations (`getById`, `getRecentUnreadForUser`, `getMatching`, `create`, `markAsRead`, `markAllAsReadForUser`, `delete`), enforcing recipient ownership via `SiteNotification#isRecipient` (throwing `ForbiddenException` / `NotFoundException`), paired with `PermissionService#canManageNotification` and `WebPermissionService#canManageNotification` for `@PreAuthorize`.
  - Every `SiteNotification.actionUrl` **must** be a context-relative path starting with `/` (never prefixed with `baseUrl` and never `mailto:`), so JSP views can safely wrap it with `<c:url value="${...}"/>`.
  - `NotificationControllerAdvice` globally exposes `recentUnreadNotificationsPage` (`Page<SiteNotification>`, `binding = false`, limit 10) for authenticated users so both the unread badge (`totalCount`) and the 10-card preview (`content`) are served without redundant queries.
  - `navbar.tag` renders the notification bell with an inline flex `<paw:badge>` and a native HTML Popover (`popover="auto"` anchored via `position: fixed` + `getBoundingClientRect()`, **never** `position: absolute`) containing a scrollable list of `<paw:notificationCard>` components and, when `totalCount > 10`, a full-width link button to `/notifications` (`NotificationController`, filtered via `NotificationFilterForm`). Navigation links to `/notifications` are also exposed in the navbar user menu and `account/sidebar.tag`.
  - **Dynamic Call-to-Action (CTA) Visibility**: Whether a notification displays its Call to Action button is governed by `SiteNotification#showCallToAction()` (and bean getter `isShowCallToAction()`), which delegates to `NotificationType#shouldShowCallToAction(Offer offer)` via a lambda `Predicate<Offer>` defined on `NotificationType`. By default, CTAs are always shown (`true`). For offer-related proof uploads (`OFFER_ACCEPTED`, `PROOF_OF_PAYMENT_UPLOADED`, `PROOF_OF_SHIPPING_UPLOADED`, `PENDING_TRANSACTION`), the CTA is shown only if `offer != null && offer.isPendingPayment()`. In `SiteNotificationJpaDao#getMatching`, `LEFT JOIN FETCH n.offer` and `LEFT JOIN FETCH n.user` are used in the population query to eagerly initialize the associations within the transactional boundary and prevent N+1 queries or `LazyInitializationException` in views.
- **Email Implementation**: The application uses `JavaMailSender` and `SpringTemplateEngine` from Thymeleaf to send HTML emails asynchronously via `EmailNotificationService` (`EmailNotificationServiceImpl`).
- **Async Sending**: All methods that send emails inside `EmailNotificationServiceImpl` **must** be annotated with `@Async`. The `MailConfig` must have `@EnableAsync`.
- **Never Pass JPA Entities to `EmailNotificationService` (use Mail DTOs instead)**: Because `EmailNotificationServiceImpl` methods run on an `@Async` thread outside the caller's thread-bound Hibernate `Session`, **never** pass managed/detached JPA `@Entity` objects (`User`, `Listing`, `Offer`) to `EmailNotificationService` (which causes `LazyInitializationException`), and **never** inject DAOs, add `@Transactional` to `EmailNotificationServiceImpl`, or use `TransactionSynchronizationManager` callbacks. Always snapshot the required scalar fields into immutable Mail DTOs (`UserMailDto`, `ProductMailDto`, `ListingMailDto`, `OfferMailDto` in `ar.edu.itba.paw.service.dto.mail`) using `.from(...)` synchronously inside `NotificationDispatcherServiceImpl` on the caller's `@Transactional` thread before calling `EmailNotificationService`. Keep standard JavaBean getters (`@Value`) on Mail DTOs so Spring 5.3 SpEL (`thymeleaf-spring5`) can resolve properties in templates.
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
- **Production DB Clone**: `.script/clone_prod_db.py` (or `make prod-db-clone`) clones the production DB into a fresh local Docker container (`postgres:latest`) on a random port and container name to prevent collisions. Requires `.script/deploy_secrets.properties`.
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
