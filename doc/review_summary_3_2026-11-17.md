# Swappr — `dev` code review, summary 3 (2026-11-17)

Condensed summary of the third review (`doc/review_3.md`, items 1–8, checked at `42540bb7`), plus
the DAO/Hibernate preflight (`doc/dao_hibernate_review.md`) and the test audit
(`doc/test_audit_review.md`). **Every actionable item was fixed and merged**; the remaining audit
items are explicitly won't-fix, latent-risk notes, or accepted. The detailed per-item notes are no
longer maintained; this file keeps only what future reviews need. Fixes landed through `2c259549`.

## What was done

- **Security and data integrity.** Escaped page-title messages (`<c:out>` + nested
  `<spring:argument>`); centralized the listing-cancellation state machine so admin cancellation
  rejects pending and pending-payment offers, resets offered listings and cannot resurrect a
  `CANCELED` listing; trade acceptance now rejects offers on both the target and the offered
  listing; added `UNIQUE (offer_id, role)` (`V36`); made `listings.creator_id NOT NULL` with
  `optional = false` (`V37`); restored the rating enum checks (`V38`); changed the six rating
  counters to primitive `int` with `@Builder.Default`.
- **Persistence and performance.** Converted the five remaining paginated DAOs
  (`Listing`, `Offer`, `Rating`, `Brand#getWithStats`, `Model#getWithInfo`) to the native 1+1 model
  and added fetch graphs for the associations their views render; proof-file listings now read a
  `FileMetadata` projection instead of loading blobs, and bytes are loaded only by authenticated
  downloads; notification previews moved from the unscoped `@ControllerAdvice` to
  `NotificationInterceptor` so image requests no longer query notifications. Resolved the
  DAO/Hibernate audit (see below).
- **User-facing bugs and UX.** Admin user-management error paths bind `filterForm`; missing or
  tampered hidden IDs produce 400 instead of 500; profile listings and ratings use separate page
  parameters; tracking-number copy is scoped per offer; the admin listing table localizes
  `OFFERED_IN_TRADE`; proof-of-payment uses the correct back label; brand sort links preserve model
  filters and page; explicit 405/415 handlers; container `<error-page>` mappings; single DOCTYPE in
  error JSPs.
- **Architecture and transactions.** Notifications are dispatched only after every fallible
  transactional write; `OfferServiceImpl` no longer injects foreign DAOs or mutates user counters
  directly; `OfferService#rate` reloads the offer by id inside its transaction; notification
  preference checks are bulk-loaded per recipient batch; business-action logging moved to the
  services; `purchase`/`pendingTransaction` lost their unused parameters and fake returns;
  `AdminServiceImpl` orchestrates owning services only, in the documented order.
- **Validation and errors.** Clearing the display name is a successful no-op; missing users throw
  `NotFoundException`; bounds added for tracking numbers and rating reviews; admin suspend/grant
  accept 254-char emails; resend-verification errors reach the view; `Price` rejects negatives;
  zero-byte shipping proofs share the payment-proof validation; filenames/content types are
  sanitized for response headers; PII is no longer logged.
- **i18n and accessibility.** Translated fragments are parameterized (currency included), the `KB`
  unit is a bundle key, icon-only controls have localized `ariaLabel`s, hidden radios are
  `sr-only peer` and preference checkboxes have `sr-only` labels, admin forms use unique DOM ids,
  and reusable tags escape attributes with `<c:out>`.
- **Tests, migrations and hygiene.** Added coverage for the riskiest service flows (cancellation
  state machine, admin/rating/password-recovery, OTP verify/expiry, user create/update/suspend),
  the missing DAO tests (including `RatingJpaDao` page 2 and out-of-range), aligned the HSQLDB
  schema (`V38` checks), removed test SQL dumping and the `format_sql` typo, and removed dead code,
  the unused inverse collection and unused message keys. Resolved the test audit (see below).

## DAO / Hibernate audit

Audit question: is there *zero* chance of N+1, lazy-init, in-memory pagination or other unexpected
Hibernate behaviour even if entities change later? Initially **no**; after the fixes below, the
remaining items are explicitly accepted.

- **Fixed: in-memory pagination order.** `SiteNotificationJpaDao#getMatching` re-applies the same
  `ORDER BY createdAt DESC, id DESC` in the population query; no Java reordering.
- **Fixed: hidden eager-roles N+1.** `User.roles` is `LAZY`; admin user pages fetch roles through
  `UserService#getRoles` once instead of touching the collection after the transaction.
- **Fixed: unfetched creator on admin listings.** `ListingService#searchForAdmin` adds
  `JOIN FETCH l.creator`; discovery/account/profile searches keep the previous shape.
- **Fixed: rejection-notification N+1.** `OfferJpaDao.rejectPendingOffers` fetch-joins buyer,
  listing (+creator/model/brand), offered listing (+model/brand) and both `imageIds`.
- **Fixed: `hasListings` pagination.** Both use native `SELECT EXISTS(...)`; `AGENTS.md` records
  native `EXISTS` as an allowed native use.
- **Fixed: count-query consistency.** All eight paginated counts are native `COUNT(*)` built from
  the same `FROM`/`WHERE`/parameter definitions as their native ID queries; collection joins would
  require `COUNT(DISTINCT ...)`/`EXISTS`.
- **Fixed: bulk-write invariant.** Every bulk update method documents that managed instances
  affected by it must be re-fetched before later reads. No current stale-data bug was found.
- **Won't fix / accepted:** unfetched associations on returned entities (latent risk only;
  fix routes targetedly if OSIV is removed — never "join fetch everything"); OSIV reliance
  across views/controllers (accepted, deploy marker present); eager `File`/`Image` blobs
  (metadata path already uses a projection; all `getById` callers need bytes); `@Formula` cost
  (potential performance only — a supporting `offers(listing_id, status, created_at)` index is the
  suggested future action); unbounded auto-rate list; `findFirst` without a limit (relies on
  uniqueness; `setMaxResults(1)` would be banned JPQL pagination); per-pair preference upserts
  (fixed 21 pairs, explicit save only).
- **Clean:** every paginated method pages a native ID-only query; display aggregates project
  scalars; collection fetch joins are never paginated.

## Test audit

Scope: 41 test files / 620 `@Test` methods. No critical findings.

- Deleted `NotificationDispatcherServiceImplTest` entirely: it contained every multi-method test
  and was maximally coupled to internal DTO/mail composition.
- Removed 14 implementation-coupled tests elsewhere (internal `ListingFilter` captors,
  `acceptsTrade` DAO arguments, image-alt literals, collaborator `doThrow`/`AtomicBoolean` checks,
  increment maps and recipient/id captors, hardcoded labels and passthrough order).
- Removed 21 useless/redundant/misplaced tests, deleting `FileServiceImplTest`,
  `ImageServiceImplTest` and `ProvinceServiceImplTest`; `FileService`, `ImageService` and
  `ProvinceService` intentionally have no test class now.
- Confirmed no `Mockito.verify` anywhere, no assertion-less tests, DAO tests follow the
  one-method-per-test rule and assert writes through `JdbcTestUtils`/`jdbcTemplate`, and no test
  mixes DAO classes. `EmailNotificationServiceImpl` staying untested is accepted.

## False positives — do not flag these again

- Admin credentials in the README (deliberate for professors) and globally disabled CSRF
  (deliberate assignment choice).
- Duplicate canceled/sold banner on listing detail (one general banner plus the action-column
  replacement).
- Old pinned SLF4J/Logback versions, duplicated plugin versions in child POMs, and redundant
  Hibernate/JAXB/`spring-jdbc` declarations: out of scope for this project.
- `SELECT EXISTS` in JPQL (it does not exist); use native `EXISTS` where short-circuiting matters,
  as `hasListings` now does.
- Carried over from earlier summaries: clone-script trust, `ImageController` public access, free
  listings in the price summary, unformatted maximum price, model replacement moving listings
  across subcategories, offer-proof filename getters, brand/model request merging across
  subcategories, and success-path delegation tests for thin catalog write methods.

## Recurring risks to re-check in future reviews

- **Pagination:** native ID query + population query + native count, equivalent `ORDER BY` in both,
  deterministic unique tie-breaker, filters shared between ID and count builder. Never rebuild page
  order in memory and never paginate JPQL.
- **Fetch plans:** the population query must fetch exactly what the view reads; admin listings use
  `searchForAdmin`. Never fetch-join a collection in a paginated query. Inner fetch joins silently
  drop rows if an association becomes nullable.
- **Native SQL:** only ID queries, `COUNT(*)` and `EXISTS` are native; everything else stays typed
  JPQL. Keep PostgreSQL/HSQLDB parity and a matching test migration for every new migration.
- **Bulk writes:** loaded entities are stale after a bulk update; re-fetch before reading and keep
  the invariant comments.
- **Transactions/notifications:** dispatch after all fallible writes; site notifications roll back
  with the transaction, emails do not. Bulk-load preferences for recipient batches.
- **OSIV:** still enabled with the deploy marker; keep fetching view data inside service
  transactions instead of adding new reliance.
- **Validation:** unconditional checks with Bean Validation, conditional rules class-level, raw
  input in DTOs and value objects built in services (`Price` rejects negatives); validate file size,
  content type and free-text lengths.
- **i18n:** both bundles, apostrophe rule, parameterized messages (never concatenate translated
  fragments), nested `<spring:argument>` for user-controlled values, no hardcoded UI text or units.
- **Tests:** no `Mockito.verify`, one DAO/service method per test, seed reads with SQL and assert
  writes with `JdbcTestUtils`, and assert observable outcomes rather than collaborator arguments.

## Notes for future reviews

- Review scope was `dev` at `42540bb7`; review-3 fixes and both audits landed through `2c259549`.
- The current tracker is `doc/review_3.md`; `4.1` is marked resolved. Earlier history lives in
  `doc/review_summary_1_*.md` and `doc/review_summary_2_*.md`.
- Migrations `V36`–`V38` are the review-3 additions (unique rating per role, creator NOT NULL,
  restored rating enum checks), each mirrored for HSQLDB.
- Proof metadata uses `FileJpaDao#getMetadataByIds` / `FileMetadata`; keep blobs out of listing
  queries and only read `data` in the download paths.
- Accepted residual DAO-audit items: latent unfetched associations, OSIV reliance, eager
  `File`/`Image` bytes, `@Formula` index opportunity, unbounded auto-rate list, `findFirst` without
  a limit, and per-pair preference upserts. Do not reopen them without a new requirement.
- Verified clean at review time: `mvn -o test`, `make troubleshoot`, no `Mockito.verify` /
  `System.out` / `printStackTrace`, no production `show_sql` / `mail.debug` /
  `hibernate.hbm2ddl.auto=update`, and production Logback uses rotating file appenders only.
