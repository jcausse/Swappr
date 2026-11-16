# Swappr — `dev` code review 3

**Scope:** `dev` at `42540bb7`, reviewed against the TP1 Group 8 feedback, the TP2 2026c1 feedback, the lecture criteria, and `AGENTS.md`.

**Verification performed:** full source review of `model`, `persistence`, `service`, `webapp`, JSPs/tags, tests, migrations and POMs. `mvn -o test` passes; `make troubleshoot` reports no unmanaged URLs. No application code was changed. This review, `AGENTS.md` and the OSIV deploy marker were updated; nothing was committed.

**How to use the tracker:** every finding has one row. Links go to the full item below.

**Status legend:** 🕓 Open · ⚠️ Decision needed · ⚠️ Bigger refactor needed · ✅ Resolved · ❌ False positive.

- 🕓 **Open** — actionable now.
- ⚠️ **Decision needed** — a human must confirm the intended behaviour before changing anything.
- ⚠️ **Bigger refactor needed** — valid issue, but the fix is a broader redesign rather than a local patch.
- ✅ **Resolved** — addressed here, accepted as policy, or confirmed non-actionable.
- ❌ **False positive** — not a defect; do not flag again.

## Status tracker

| Item | Brief description | Status |
|---|---|---|
| 1.1 | [Admin credentials in README (deliberate for professors)](#11-production-admin-credentials-are-committed-in-the-readme) | ❌ False positive |
| 1.2 | [CSRF protection disabled globally (likely deliberate)](#12-csrf-protection-is-globally-disabled) | ⚠️ Decision needed |
| 1.3 | [Stored XSS through unescaped page-title messages](#13-stored-xss-through-unescaped-page-title-messages) | ✅ Resolved |
| 1.4 | [Admin cancellation leaves active offers and can resurrect canceled listings](#14-admin-cancellation-leaves-active-offers-and-can-resurrect-canceled-listings) | ⚠️ Bigger refactor needed |
| 1.5 | [Trade acceptance does not reject offers on the offered listing](#15-trade-acceptance-does-not-reject-offers-on-the-offered-listing) | ✅ Resolved |
| 1.6 | [Rating uniqueness constraint was dropped without replacement](#16-rating-uniqueness-constraint-was-dropped-without-replacement) | ✅ Resolved |
| 1.7 | [Listing creator is nullable in schema and mapping](#17-listing-creator-is-nullable-in-schema-and-mapping) | ✅ Resolved |
| 1.8 | [Rating counters use boxed Integer on NOT NULL columns](#18-rating-counters-use-boxed-integer-on-not-null-columns) | ✅ Resolved |
| 2.1 | [Five paginated DAOs bypass the mandatory native 1+1 model](#21-five-paginated-daos-bypass-the-mandatory-native-11-model) | ✅ Resolved |
| 2.2 | [N+1 on the main listing, offer and rating lists](#22-n1-on-the-main-listing-offer-and-rating-lists) | ✅ Resolved |
| 2.3 | [Proof-file blobs loaded eagerly just to show metadata](#23-proof-file-blobs-are-loaded-eagerly-just-to-show-metadata) | ✅ Resolved |
| 2.4 | [OSIV deploy marker (added; OSIV accepted as discouraged)](#24-osiv-is-enabled-and-its-deploy-todo-lacks-the-required-marker) | ✅ Resolved |
| 2.5 | [Notification advice queries the DB on every request including images](#25-notification-advice-queries-the-db-on-every-request-including-images) | 🕓 Open |
| 3.1 | [Admin user-management error paths render a view missing filterForm](#31-admin-user-management-error-paths-render-a-view-missing-filterform) | 🕓 Open |
| 3.2 | [Missing or tampered hidden IDs produce 500 instead of 400](#32-missing-or-tampered-hidden-ids-produce-500-instead-of-400) | 🕓 Open |
| 3.3 | [Profile listing pagination is broken and shares page with ratings](#33-profile-listing-pagination-is-broken-and-shares-page-with-ratings) | 🕓 Open |
| 3.4 | [Duplicate tracking-number DOM id copies the wrong offer](#34-duplicate-tracking-number-dom-id-copies-the-wrong-offer) | ✅ Resolved |
| 3.5 | [Admin listing table shows raw OFFERED_IN_TRADE status](#35-admin-listing-table-shows-raw-offered_in_trade-status) | ✅ Resolved |
| 3.6 | [Proof-of-payment back link uses the wrong label](#36-proof-of-payment-back-link-uses-the-wrong-label) | 🕓 Open |
| 3.7 | [Duplicate cancel/sold banners (intended)](#37-canceledsold-banner-rendered-twice-on-listing-detail) | ❌ False positive |
| 3.8 | [Brand sort links in admin catalog drop model filters and page](#38-brand-sort-links-in-admin-catalog-drop-model-filters-and-page) | 🕓 Open |
| 3.9 | [Unsupported HTTP methods fall into the 500 handler](#39-unsupported-http-methods-fall-into-the-500-handler) | 🕓 Open |
| 3.10 | [No container error-page mappings for render-time exceptions](#310-no-container-error-page-mappings-for-render-time-exceptions) | 🕓 Open |
| 3.11 | [Error JSPs declare DOCTYPE twice](#311-error-jsps-declare-doctype-twice) | ✅ Resolved |
| 4.1 | [Emails/notifications dispatched before the transaction commits](#41-emails-and-notifications-are-dispatched-before-the-transaction-commits) | 🕓 Open |
| 4.2 | [OfferService injects foreign DAOs and mutates user counters directly](#42-offerservice-injects-foreign-daos-and-mutates-user-counters-directly) | 🕓 Open |
| 4.3 | [OfferService.rate accepts a detached entity and relies on OSIV](#43-offerservicerate-accepts-a-detached-entity-and-relies-on-osiv) | 🕓 Open |
| 4.4 | [Notification loops query preferences once per recipient](#44-notification-loops-query-preferences-once-per-recipient) | 🕓 Open |
| 4.5 | [Business-action logging lives in controllers, before the action succeeds](#45-business-action-logging-lives-in-controllers-before-the-action-succeeds) | 🕓 Open |
| 4.6 | [ListingDao purchase/pendingTransaction have unused parameters and fake returns](#46-listingdao-purchasependingtransaction-have-unused-parameters-and-fake-returns) | 🕓 Open |
| 4.7 | [Move AdminServiceImpl bulk cleanup into owning services](#47-move-adminserviceimpl-bulk-cleanup-into-the-owning-services) | 🕓 Open |
| 5.1 | [Clearing display name on profile edit causes a 500](#51-clearing-display-name-on-profile-edit-causes-a-500) | 🕓 Open |
| 5.2 | [Missing users throw IllegalArgumentException instead of NotFoundException](#52-missing-users-throw-illegalargumentexception-instead-of-notfoundexception) | 🕓 Open |
| 5.3 | [Shipping tracking number has no max length](#53-shipping-tracking-number-has-no-max-length) | 🕓 Open |
| 5.4 | [Rating review text has no length bound](#54-rating-review-text-has-no-length-bound) | 🕓 Open |
| 5.5 | [Admin suspend/grant forms reject emails longer than 100 chars](#55-admin-suspendgrant-forms-reject-emails-longer-than-100-chars) | 🕓 Open |
| 5.6 | [Resend-verification validation errors are silently discarded](#56-resend-verification-validation-errors-are-silently-discarded) | 🕓 Open |
| 5.7 | [ListingService does not validate negative prices](#57-listingservice-does-not-validate-negative-prices) | 🕓 Open |
| 5.8 | [Proof of shipping accepts a zero-byte file](#58-proof-of-shipping-accepts-a-zero-byte-file) | 🕓 Open |
| 5.9 | [Raw filename and unvalidated content type in response headers](#59-raw-filename-and-unvalidated-content-type-in-response-headers) | 🕓 Open |
| 5.10 | [Emails/usernames logged in clear text](#510-emails-and-usernames-are-logged-in-clear-text) | 🕓 Open |
| 6.1 | [Comma-containing display names are truncated in message arguments](#61-comma-containing-display-names-are-truncated-in-message-arguments) | 🕓 Open |
| 6.2 | [Hardcoded “KB” unit in proof components](#62-hardcoded-kb-unit-in-proof-components) | 🕓 Open |
| 6.3 | [Translated fragments concatenated instead of parameterized](#63-translated-fragments-concatenated-instead-of-parameterized) | 🕓 Open |
| 6.4 | [Icon-only links, hidden radios and preference checkboxes are inaccessible](#64-icon-only-links-hidden-radios-and-preference-checkboxes-are-inaccessible) | 🕓 Open |
| 6.5 | [Duplicate DOM ids in admin user forms](#65-duplicate-dom-ids-in-admin-user-forms) | 🕓 Open |
| 6.6 | [Reusable tags render raw attributes without c:out](#66-reusable-tags-render-raw-attributes-without-cout) | 🕓 Open |
| 7.1 | [No tests for the riskiest service flows](#71-no-tests-for-the-riskiest-service-flows) | 🕓 Open |
| 7.2 | [Missing DAO tests and only negative UserJpaDao coverage](#72-missing-dao-tests-and-only-negative-userjpadao-coverage) | 🕓 Open |
| 7.3 | [Pagination tests do not exercise page 2 or RatingJpaDao](#73-pagination-tests-do-not-exercise-page-2-or-ratingjpadao) | 🕓 Open |
| 7.4 | [HSQLDB test schema diverges from PostgreSQL](#74-hsqldb-test-schema-diverges-from-postgresql) | 🕓 Open |
| 7.5 | [Migration edited before production push (unaffected DB)](#75-an-already-committed-migration-was-edited) | ✅ Resolved |
| 7.6 | [Test Hibernate config dumps SQL and misspells format_sql](#76-test-hibernate-config-dumps-sql-and-misspells-format_sql) | 🕓 Open |
| 7.7 | [Outdated SLF4J/Logback and redundant logging deps](#77-obsolete-slf4jlogback-versions-and-redundant-logging-deps) | ⚠️ Decision needed |
| 7.8 | [Plugin versions duplicated in child POMs](#78-plugin-versions-duplicated-in-every-child-pom) | ⚠️ Decision needed |
| 7.9 | [Redundant Hibernate/JAXB dependencies and production JDBC declaration](#79-redundant-hibernatejaxb-dependencies-and-production-jdbc-declaration) | 🕓 Open |
| 7.10 | [Existence checks use COUNT or setMaxResults instead of EXISTS](#710-existence-checks-use-count-or-setmaxresults-instead-of-exists) | 🕓 Open |
| 7.11 | [Dead code, unused collection mapping and unused message keys](#711-dead-code-unused-collection-mapping-and-unused-message-keys) | 🕓 Open |
| 8.1 | [Request dedup is global (AGENTS updated)](#81-agents-requires-subcategory-scoped-request-dedup-code-and-prior-review-say-global) | ✅ Resolved |
| 8.2 | [OSIV discouraged but not a hard fail (policy settled)](#82-lecture-allows-osiv-agents-says-do-not-rely-on-it) | ✅ Resolved |
| 8.3 | [Transactional rule scoped; EmailNotification exception documented](#83-agents-requires-transactional-on-all-services-then-forbids-it-on-emailnotificationserviceimpl) | ✅ Resolved |
| 8.4 | [AdminService multi-DAO guidance corrected; code fix is 4.7](#84-tp2-says-one-dao-per-service-agents-explicitly-allows-adminserviceimpl-to-use-two) | ✅ Resolved |
| 8.5 | [AGENTS now points at this review](#85-agents-points-to-a-review-tracker-file-that-does-not-exist) | ✅ Resolved |
| 8.6 | [OSIV deploy marker added](#86-the-deploy-comment-rule-is-violated-by-the-osiv-todo) | ✅ Resolved |

---

## 1. Critical security and data integrity

### 1.1 Production admin credentials are committed in the README

**False positive.** This is a university assignment; the credentials are deliberately left in `README.md` so professors can use them during testing. Do not flag this in future reviews. No action.

### 1.2 CSRF protection is globally disabled

`WebAuthConfig.java:100` calls `csrf().disable()`. Every state-changing POST is therefore forgeable from another site: offer accept/reject/withdraw, checkout, profile and password changes, admin suspension, catalog writes and takedowns. The session cookie is enough to authorize the request.

**Decision needed.** This is likely disabled deliberately (e.g. a course or deployment constraint); confirm the reason and document it. If the disable is not deliberate, remove it and add CSRF tokens to every POST form.

### 1.3 Stored XSS through unescaped page-title messages

`head.tag:16` renders `<spring:message code="${titleKey}" arguments="${titleParams}"/>` directly inside `<title>`. `profile.jsp:27` and `profileEdit.jsp:7` pass `${user.displayName}` as `titleParams`, and `account/index.jsp:8` passes it to another message. Spring's `spring:message` tag is not HTML-escaping by default in this configuration (`RequestContext.isDefaultHtmlEscape()` is false because no default-html-escape setting is configured).

A display name such as `</title><script>alert(1)</script>` fits the 50-character limit and executes on every public profile view.

**Action:** resolve the message into a variable and render it with `<c:out>`, or enable default HTML escaping and audit for double escaping. Pass user-controlled values through nested `<spring:argument>` elements, not the comma-delimited `arguments` string. Same fix for `account/index.jsp`.

### 1.4 Admin cancellation leaves active offers and can resurrect canceled listings

`ListingCancellationServiceImpl.cancelByAdmin` (`:44-57`) allows canceling `PENDING_TRANSACTION` and `OFFERED_IN_TRADE` listings (`Listing.canBeCanceledByAdmin`, `Listing.java:126-128`), but `OfferService.rejectPendingOffersForListing` only rejects offers whose status is `PENDING` (`OfferServiceImpl:347-364`, `OfferJpaDao:152-176`).

Consequences:

- Canceling a `PENDING_TRANSACTION` listing leaves its accepted `PENDING_PAYMENT` offer alive. The buyer is never notified; the seller's later `confirmPayment` fails because the listing is no longer pending.
- Canceling an `OFFERED_IN_TRADE` listing leaves the trade offer that references it alive. `OfferServiceImpl.accept` (`:258-261`) then unconditionally sets the offered listing to `SOLD`, so a canceled listing becomes sold.
- `ListingJpaDao.releaseOfferedListingsForParticipant` (`:208-231`) and `releaseListingsHeldByBuyerPaymentOffers` (`:286-294`) set listings back to `ACTIVE` without checking the source status, so those stale offers can later resurrect a `CANCELED` listing.

**Action:** centralize the cancellation state machine in the service/model. Cancel or reject every active offer on the listing, including `PENDING_PAYMENT`, reset the offered listings, and reject pending trade offers that use the canceled listing as their offered item. Add explicit status predicates to the bulk release queries, and reject `accept` if the offered listing is no longer `OFFERED_IN_TRADE`.

### 1.5 Trade acceptance does not reject offers on the offered listing

`OfferServiceImpl.accept` (`:258-265`) marks the target listing `PENDING_TRANSACTION`, rejects competing offers on the *target* listing, and marks the buyer's offered listing `SOLD`. It never rejects pending offers that other users made on the offered listing. Those offers remain `PENDING` on a sold listing and their buyers are not notified.

**Action:** reject pending offers for `offer.getOfferedListingId()` when the trade is accepted, or reject them when the listing first becomes `OFFERED_IN_TRADE`.

### 1.6 Rating uniqueness constraint was dropped without replacement

`V20__Add_unique_ratings_offer_id.sql` added `UNIQUE (offer_id)`, and `V22__Drop_unique_ratings_offer_id_constraint.sql` drops it. `Rating.java:49-51` has no replacement constraint. The application-level check in `OfferServiceImpl.rate` is read-then-write and can race, allowing two buyer ratings or two seller ratings for the same offer.

**Action:** add a new migration with `UNIQUE (offer_id, role)` (not `UNIQUE (offer_id)`, because each offer needs one rating per role). Add a concurrent/duplicate-rating test at the service or DAO level.

### 1.7 Listing creator is nullable in schema and mapping

`V1__Original_schema.sql:43` declares `creator_id INTEGER REFERENCES users(user_id)` with no `NOT NULL`, and `Listing.java:58-60` maps it as `@ManyToOne(fetch = FetchType.LAZY)` without `optional = false`. The domain treats the creator as mandatory (`Listing.isCreator` dereferences `creator` without a guard), so a legacy or manually inserted row can cause a `NullPointerException`.

**Action:** add a new migration that backfills and sets `creator_id NOT NULL`, and annotate the association `optional = false`.

### 1.8 Rating counters use boxed Integer on NOT NULL columns

`User.java:69-91` declares all six rating counters as `Integer` even though the columns are `NOT NULL`. This creates an artificial null state and forces null checks in `User.java:161-185` and `UserJpaDao.java:166-207`, contrary to the primitive-for-non-null rule.

**Action:** change the six fields to `int` with `@Builder.Default` value `0`, remove the null-coalescing logic, and let the database constraint be the only source of truth.

---

## 2. High-priority persistence and performance

### 2.1 Five paginated DAOs bypass the mandatory native 1+1 model

The project rule is strict: every paginated query must use a native ID query with filters, order and limit/offset, followed by a population query by `id IN :ids`, plus a separate count. These DAOs still paginate a JPQL query directly:

- `ListingJpaDao.search` (`:151-155`)
- `OfferJpaDao.search` (`:83-87`)
- `RatingJpaDao.search` (`:57-61`)
- `BrandJpaDao.getWithStats` (`:62-73`) — doc comment at `ModelJpaDao.java:98-100`
- `ModelJpaDao.getWithInfo` (`:98-119`)

None currently fetch-joins a collection, so Hibernate does not yet emit `HHH000104`, but the rule is still violated and any future mapping change can silently turn these into in-memory pagination.

**Action:** implement each as a native ID query plus a population query (or aggregate/dto query restricted to the selected IDs), keeping filters, ordering, count and a unique tie-breaker in the ID query. Do not rely on “there is no join today.”

### 2.2 N+1 on the main listing, offer and rating lists

The paginated entity queries above return bare entities with no fetch plan:

- `ListingJpaDao.search` returns `Listing` with lazy `model` and `model.subcategory`; `listingCard.tag:19-20`, `product.tag` and `profile.jsp:176` traverse them per card.
- `OfferJpaDao.search` returns `Offer` with lazy `listing`, `buyer`, `offeredListing`, `listing.creator` and proof files; `offerCard.tag:13-37,85-88,139-149` traverses all of them per card.
- `RatingJpaDao.search` returns `Rating` with lazy `listing` and `creator`; `profile.jsp:128-133` traverses them per rating.

Under OSIV this becomes a query storm; without OSIV it becomes `LazyInitializationException`. `CategoriesInterceptor` already demonstrates the right approach for view-only data.

**Action:** use 1+1 population queries with `JOIN FETCH` for the many-to-one associations the view needs, or project a flat DTO. Never fetch-join a collection while paginating.

### 2.3 Proof-file blobs are loaded eagerly just to show metadata

`File.java:46-54` maps `data` as a plain `byte[]` basic field. Hibernate loads the whole blob whenever a `File` entity is initialized. `Offer.java:132-165` exposes filename, size and content-type getters that initialize the lazy `proofOfPayment`/`proofOfShipping` proxies, and `offerCard.tag:130-149` plus `offerProofOfPayment.tag:15-23` and `offerProofOfShipping.tag:30-38` use those getters for every offer with an attachment.

The `@Formula("octet_length(data)")` comment claims to avoid reading the blob, but the entity load already includes `data`.

**Action:** make the binary lazy with Hibernate bytecode enhancement, or expose a metadata-only projection/DAO method and only load the bytes in the authenticated download path. Add a test asserting that listing offers does not load `data`.

### 2.4 OSIV is enabled and its deploy TODO lacks the required marker

**Resolved for this review.** `web.xml:89` now carries the exact `IMPORTANT: REVIEW BEFORE DEPLOY. DO NOT CHANGE THIS COMMENT AS IT IS SEARCHED BY GREP` marker. OSIV remains enabled as a discouraged-but-accepted trade-off (see 8.2); do not add new reliance on it, and keep fetching view data inside service transactions where practical.

### 2.5 Notification advice queries the DB on every request including images

`NotificationControllerAdvice:22-27` is an unscoped `@ControllerAdvice` `@ModelAttribute`. It runs for every handler, including `ImageController.getImage`, so an authenticated page with 20 `<img>` tags issues 20 notification queries. The comment in `CategoriesInterceptor` explains why that pattern was avoided there.

**Action:** populate the notification preview from an interceptor that only runs for page-rendering handlers (similar to `CategoriesInterceptor`), or restrict the advice to the controllers that render the navbar.

---

## 3. User-facing bugs and UX

### 3.1 Admin user-management error paths render a view missing `filterForm`

`AdminController.suspendUser` (`:627-629,634-638`), `unsuspendUser` (`:655-657`) and `grantAdminRole` (`:674-676`) return `new ModelAndView("admin/users")` on validation or self-suspend errors. `admin/users.jsp:20` binds `<form:form modelAttribute="filterForm">`, but the error paths never add `filterForm` or `targetUser`. Spring throws “Neither BindingResult nor plain target object for bean name 'filterForm'”, so a blank username or an attempted self-suspend produces a 500 instead of the intended error message.

**Action:** accept `@ModelAttribute("filterForm") AdminUserFilterForm` in the POST handlers and pass it to the view, or redirect back to the GET with a flash error.

### 3.2 Missing or tampered hidden IDs produce 500 instead of 400

`ListingController.detailsPost` (`:300-303`) and both `editListing` error paths (`:341-344,362-365`) call `catalogService.getModelById(form.getModelId())` when binding fails. `modelId` is a hidden field; if it is absent or tampered to null, `em.find(Model.class, null)` throws `IllegalArgumentException` and the catch-all handler returns 500. `CheckoutController.checkoutPost` (`:57-60`) has the same problem with `listingId` and `listingService.getById(null)`.

**Action:** only look up the entity when the ID is non-null; otherwise return 400 or re-render a safe error view. Add a test that posts the form without the hidden field.

### 3.3 Profile listing pagination is broken and shares `page` with ratings

`UserController.profile` (`:67-75`) always requests `page(1)` for listings, but `profile.jsp:194` renders `paw:pagination` for `listingPage` with the default `page` parameter. Clicking next sends `?page=2`, which the controller only applies to the ratings query (`:85`). Listing pages after the first are therefore unreachable, and the ratings section changes instead.

**Action:** either remove listing pagination or add a `listingPage` parameter and honor it in the controller. Give the ratings pagination `pageParam="ratingPage"` so the two sections cannot collide.

### 3.4 Duplicate tracking-number DOM id copies the wrong offer

`offerProofOfShipping.tag:17,19` hardcodes `id="trackingNumberText"` and the copy button uses `document.querySelector('#trackingNumberText')`. On `/account/my-offers` or `/account/incoming-offers` with multiple shipping offers, every copy button copies the first offer's tracking number.

**Action:** include `${offer.id}` in the id and query within the button's closest container, or pass the value to the handler directly.

### 3.5 Admin listing table shows raw `OFFERED_IN_TRADE` status

`admin/listings.jsp:90-93` falls into `<c:otherwise>` and renders `${listing.status.name()}`. `account/listings.jsp:65-68` correctly maps `OFFERED_IN_TRADE` to a localized label. Admins therefore see the raw enum value for trade listings.

**Action:** add the missing `c:when` with `listing.status.OFFERED_IN_TRADE`.

### 3.6 Proof-of-payment back link uses the wrong label

`account/proofOfPaymentUpload.jsp:9,13` links back to `/account/my-offers` but uses `offer.detail.back`, which reads “Back to Incoming Offers”. The user is sent to My Offers with a contradictory label.

**Action:** use an existing “Back to My Offers” key or add one to both bundles.

### 3.7 Canceled/sold banner rendered twice on listing detail

**False positive.** Intended behaviour: one banner is a general visibility notice above the card, and the second replaces the “make an offer” call-to-action in the action column. No action.

### 3.8 Brand sort links in admin catalog drop model filters and page

`admin/catalog.jsp:115-126` builds brand sort URLs with only `sort`. The model sort links preserve the subject filters (`filterBrandId`, `filterCategoryId`, `filterSubcategoryId`, `modelPage`), but sorting brands resets the models table state.

**Action:** include the model filters and `modelPage` in the brand sort URLs, or build the sort URLs server-side like the listing/offer return URLs.

### 3.9 Unsupported HTTP methods fall into the 500 handler

`GlobalExceptionHandler:97-102` catches `Exception` and returns 500. Because there is no specific handler for `HttpRequestMethodNotSupportedException` (or `HttpMediaTypeNotSupportedException`), posting to a GET-only route (e.g. `POST /`) returns 500 “Something Went Wrong” instead of 405.

**Action:** add explicit handlers for `HttpRequestMethodNotSupportedException` (405), `HttpMediaTypeNotSupportedException` (415) and similar MVC 4xx exceptions. Keep the catch-all for genuine unexpected failures.

### 3.10 No container error-page mappings for render-time exceptions

`web.xml` has no `<error-page>` entries. Exceptions thrown after the handler returns, or exceptions in the view layer, bypass `GlobalExceptionHandler` and show Jetty's raw error page with stack trace and internal paths. The missing `filterForm` case in 3.1 is one example.

**Action:** add `<error-page>` mappings for 404, 403 and 500 pointing at the application error views, and verify a deliberately broken view does not leak a stack trace.

### 3.11 Error JSPs declare DOCTYPE twice

`error/badRequest.jsp:1,7`, `forbidden.jsp:1,7`, `internalServerError.jsp:1,7`, `notFound.jsp:1,11` and `unauthorized.jsp:1,7` each contain two `<!DOCTYPE html>` declarations.

**Action:** keep only the first one.

---

## 4. Architecture, transactions and layering

### 4.1 Emails and notifications are dispatched before the transaction commits

Several services call `NotificationDispatcherService` while the surrounding transaction is still open:

- `AdminServiceImpl.suspendUser` (`:85-88`) sends the suspension email before `cancelAllByCreator`.
- `ListingCancellationServiceImpl.cancel` (`:35-38`) dispatches offer-rejection notifications before `listingDao.cancel`.
- `BrandModelRequestServiceImpl.accept` (`:135-146`) deletes the request and dispatches emails inside the same transaction.
- `UserServiceImpl.create` (`:89`) sends the verification email before commit.

If the transaction rolls back, site notifications roll back but the async email has already been queued or sent. Users can receive mail for a user/listing/offer that does not exist.

**Action:** dispatch email notifications from an after-commit hook or a `@TransactionalEventListener(AFTER_COMMIT)` while keeping the async email service DB-free. Site notifications can stay inside the transaction because they roll back with it.

### 4.2 OfferService injects foreign DAOs and mutates user counters directly

`OfferServiceImpl:48-50` injects `UserDao` and `FileDao` in addition to `OfferDao`. It also increments user rating counters directly (`:578,585,624,626`), crossing the offer/user aggregate boundary.

**Action:** move user counter mutations behind `UserService` or `RatingService`; move proof-file creation behind a file/image service. Keep `OfferServiceImpl` dependent on its own DAO plus service contracts. (But see 8.4: `AdminServiceImpl` is an intentional exception in `AGENTS.md`.)

### 4.3 OfferService.rate accepts a detached entity and relies on OSIV

`OfferService.rate` (`OfferServiceImpl:548-554`) receives an `Offer` loaded by `AccountController` (`:283-292`) and immediately traverses `offer.getBuyer()` and `offer.getListing().getCreator()`. Those associations are lazy, so this only works because OSIV keeps the session open. With OSIV removed, or if the entity is detached, it throws `LazyInitializationException`. It also makes business decisions on a potentially stale object supplied by the web layer.

**Action:** change the contract to accept `offerId` and reload the offer inside the service transaction, then validate ownership from the fresh instance.

### 4.4 Notification loops query preferences once per recipient

`NotificationDispatcherServiceImpl:239-256` and `:265-281` call `shouldSendSite`/`shouldSendEmail` inside a loop over recipients. Each call performs `notificationPreferenceService.isNotificationEnabled`, which queries the preference table. A request with many additional requesters becomes an N+1.

**Action:** load the recipients' preferences in one query or add a bulk `isNotificationEnabled` operation and reuse the result inside the loop.

### 4.5 Business-action logging lives in controllers, before the action succeeds

Examples: `ListingController:370`, `OfferController:29,38,49,64`, `AdminController:481,572`. These log “accepted/withdrew/canceled/took down” before the service call. If the service throws, the audit log is wrong. Business logging belongs in the service where the outcome is known.

**Action:** log after a successful service return, or log the outcome in the service.

### 4.6 ListingDao purchase/pendingTransaction have unused parameters and fake returns

`ListingJpaDao.purchase` and `pendingTransaction` (`:180-195`) ignore `buyerId`, return a constant status even when the listing does not exist, and duplicate the state transition that the service should own. The contract still exposes the unused parameter.

**Action:** remove `buyerId`, return `Optional`/the updated listing or make the methods `void`, and keep the transition rule in `Listing`/the service.

### 4.7 Move AdminServiceImpl bulk cleanup into the owning services

`AdminServiceImpl.suspendUser` (`:76-88`) calls `ListingDao` and `OfferDao` directly for the bulk release/reject/cancel sequence. `AGENTS.md` now records that this is the wrong pattern and must not be copied.

**Action:** move each bulk operation into its owning service (`ListingService`, `ListingCancellationService`, `OfferService`) and keep `AdminServiceImpl` orchestrating service contracts only, preserving the required order: release offered listings, release held listings, reject participant offers, suspend, cancel remaining listings.

---

## 5. Validation and error handling

### 5.1 Clearing display name on profile edit causes a 500

`UserEditForm.displayName` has only `@Size(max = 50)`, so it can be blank. `UserServiceImpl.update` (`:110-137`) maps a blank display name to `null`, and if no password or image changed either, `UserJpaDao.update` (`:76-79`) returns `Optional.empty()`. The service then throws `IllegalArgumentException("Non-valid User received")`, which becomes a 500. A user who clears the field while changing only location or language hits this.

**Action:** treat “no core fields changed” as a successful no-op using the already-loaded user, or make display name mandatory. Do not use `Optional.empty()` to mean both “not found” and “nothing to update.”

### 5.2 Missing users throw IllegalArgumentException instead of NotFoundException

`UserServiceImpl:166,174,229,259,269` throws `IllegalArgumentException` when a user ID disappears between the load and the update. These are public service methods; a missing user should produce 404, not 500.

**Action:** throw `NotFoundException.createFor("User with ID " + id)` and update `UserServiceImplTest:207-214`, which currently codifies the wrong expectation.

### 5.3 Shipping tracking number has no max length

`ProofOfShippingUploadForm.trackingNumber` (`:20-23`) has no `@Size`. The database column is `VARCHAR(255)` (`V8__Add_proof_of_shipping.sql:4`), so a longer value reaches the database and causes a constraint violation, resulting in 500.

**Action:** add `@Size(max = 255)` to the form field.

### 5.4 Rating review text has no length bound

`RateForm.reviewText` (`:18`) is persisted to a `TEXT` column with no `@Size`. This allows unbounded input and unnecessary storage/rendering cost.

**Action:** add an explicit `@Size(max = ...)` appropriate for reviews.

### 5.5 Admin suspend/grant forms reject emails longer than 100 chars

`AdminSuspendUserForm` and `AdminGrantRoleForm` use `@Size(min = 3, max = 100)` on a field that is passed to `getByUsernameOrEmail`. Emails may be up to 254 characters (`AdminUserFilterForm` correctly allows 254). An admin cannot suspend or grant a role to a user with a long email, even though the same user is searchable.

**Action:** use `@Size(max = 254)` consistently.

### 5.6 Resend-verification validation errors are silently discarded

`EmailVerificationController.resendVerificationCode` (`:76-78`) redirects to `/verify` when `errors.hasErrors()`, with no flash attribute and no preserved value. The user gets no feedback.

**Action:** re-render the verify view with the binding errors, or redirect with a flash error code.

### 5.7 ListingService does not validate negative prices

`ListingServiceImpl.create` (`:181`) and `update` (`:277`) construct `new Price(dto.price())`; `Price` (`:24`) only enforces non-null. The web form has `@DecimalMin("0.0")`, but service callers can create negative prices, which break free/trade semantics and filtering.

**Action:** validate non-negative price in `ListingService` or in `Price` itself.

### 5.8 Proof of shipping accepts a zero-byte file

`OfferServiceImpl.uploadProofOfShipping` (`:444-458`) checks the content type but, unlike `uploadProofOfPayment` (`:396-398`), never checks `data.length == 0`. An empty proof can be stored and notified.

**Action:** share the non-empty and allowed-type validation between both upload methods.

### 5.9 Raw filename and unvalidated content type in response headers

`AccountController.getResource` (`:251-258`) puts the uploaded original filename directly into `Content-Disposition`, and `ImageController:50-53` plus `AccountController:255` call `MediaType.parseMediaType` on a stored content type that was only checked for an `image/` prefix. Quotes/semicolons in a filename or an invalid media type can corrupt the header or throw.

**Action:** sanitize/encode the filename with Spring's `ContentDisposition` builder and catch `InvalidMediaTypeException`, falling back to `application/octet-stream`.

### 5.10 Emails and usernames are logged in clear text

`PasswordRecoveryController:61`, `EmailVerificationController:57,79` and `UserController:201` log usernames/emails at INFO. This is unnecessary PII in production logs.

**Action:** log user IDs instead, or downgrade to DEBUG with redaction.

---

## 6. Internationalization and accessibility

### 6.1 Comma-containing display names are truncated in message arguments

`head.tag:16` and `account/index.jsp:8` pass a `String` to `spring:message arguments=`. Spring treats that string as a comma-delimited argument list. A display name like `Smith, John` renders only `Smith`, and a name containing `{0}` can also be interpreted by `MessageFormat`.

**Action:** use nested `<spring:argument value="${displayName}"/>` (or a one-element collection) wherever a user-controlled value is passed to a message.

### 6.2 Hardcoded KB unit in proof components

`offerProofOfPayment.tag:19` and `offerProofOfShipping.tag:34` render `<c:out value="${...SizeKb}"/> KB`. The unit is not localizable and the number is not locale-formatted.

**Action:** add a message such as `file.size.kb={0} KB` and pass the size as an argument.

### 6.3 Translated fragments concatenated instead of parameterized

Examples: `product.tag:16` joins category and subcategory with ` / `; `listing/index.jsp:44` joins condition title and label with a space; `offerCard.tag:61` passes `${currencySymbol} ${offer.amount}` as one `spring:message` argument. Each assumes a fixed word order and cannot be reordered by a translator.

**Action:** use one message with ordered placeholders and separate arguments for currency and amount.

### 6.4 Icon-only links, hidden radios and preference checkboxes are inaccessible

- `pagination.tag:58-66` and several account pages render `paw:linkButton` with only an icon. `linkButton.tag` has no `ariaLabel` attribute, so the link has no accessible name.
- `formButtonToggle.tag:16` renders the radios with `cssClass="hidden"` (display:none). They cannot receive keyboard focus, and the visible `paw:fakeButton` labels are not real controls.
- `notifications/preferences.jsp:63-68` renders checkboxes without a `label` or `aria-label`; the type/channel association is only visual.

**Action:** add accessible names for icon-only actions, use `sr-only`/`peer` instead of `hidden` for the radios, and associate every checkbox with its type and channel.

### 6.5 Duplicate DOM ids in admin user forms

`admin/users.jsp:24,112,125` and `admin/dashboard.jsp:118,131` use `paw:formInput` with the default id equal to `path`, so multiple `username` inputs share `id="username"`. Labels focus the wrong field and the HTML is invalid.

**Action:** pass explicit unique ids (`filterUsername`, `suspendUsername`, `grantUsername`).

### 6.6 Reusable tags render raw attributes without `c:out`

Examples: `imageGallery.tag:21,27,52` (`src`, `id`), `listingCard.tag:29,34`, `listingImage.tag:30`, `offerProofOfPayment.tag:24`, `offerProofOfShipping.tag:39`, `button.tag:97` (`aria-label`), `listingCarousel.tag:36,62`. Current callers mostly pass `c:url` output or messages, so this is not currently exploitable, but the tag contract is not XSS-safe.

**Action:** wrap attribute values in `<c:out>` inside the tags.

---

## 7. Tests, migrations, build and hygiene

### 7.1 No tests for the riskiest service flows

`ListingCancellationServiceImplTest` covers only negative paths (`:50-106`); there is no successful admin cancellation of `PENDING_TRANSACTION` or `OFFERED_IN_TRADE`. There is no `AdminServiceImplTest`, `RatingServiceImplTest` or `PasswordRecoveryServiceImplTest`. `OneTimePasswordServiceImplTest` only covers `create`, not `verify`/expiry. `UserServiceImplTest` does not cover `create`, `update`, `updateLocation`, `addRole`, `suspend`, `unsuspend` or `getByUsernameOrEmail`.

**Action:** add focused Mockito tests for these flows, especially the cancellation state machine and rating/OTP side effects.

### 7.2 Missing DAO tests and only negative UserJpaDao coverage

There is no `RatingJpaDaoTest`, `ProvinceJpaDaoTest` or `UserRoleJpaDaoTest`, even though `RatingJpaDao.search` has pagination logic and `UserRoleJpaDao` mutates roles. `UserJpaDaoTest:106-143` only tests missing users for `update`, `verifyEmail` and `updateLocation`; there are no positive tests for those methods, `suspend`, `unsuspend`, `getByUsername` or `getByEmail`.

**Action:** add one-method-per-test DAO integration tests, seeding reads with SQL and asserting writes with `JdbcTestUtils`/`jdbcTemplate`. This overlaps 7.3 on `RatingJpaDao`; add that test once and cover both concerns together.

### 7.3 Pagination tests do not exercise page 2 or RatingJpaDao

`ModelJpaDaoTest:152-206` asserts `totalPages == 3` but only requests page 1. `ModelListingInfoJpaDaoTest:126-137` has only one matching model and only page 1. `RatingJpaDao.search` has no pagination test at all.

**Action:** add second-page and out-of-range tests with multiple matches and a unique tie-breaker. Overlaps 7.2 on `RatingJpaDao`; resolve both items with the same new `RatingJpaDaoTest`, covering page 2 and out-of-range pages.

### 7.4 HSQLDB test schema diverges from PostgreSQL

The PostgreSQL `V18__Convert_enums_to_uppercase.sql:11-15` drops `ratings_role_check` and `ratings_type_check` and never recreates them. The HSQLDB counterpart (`V18:11-13`) drops and recreates `ratings` with uppercase checks. `V21__Add_site_notifications.sql` also omits production's partial index (`WHERE is_read = FALSE`) in the HSQLDB copy.

Tests therefore run against a stricter schema than production, so invalid enum data is rejected in tests but accepted in production.

**Action:** add a new production migration re-adding the checks (or intentionally remove them from both), and mirror the index shape where HSQLDB permits.

### 7.5 An already-committed migration was edited

**Resolved.** `V35__Add_notification_preferences.sql` was introduced in `81c0ac81` and changed in `571702b9`, but this was before the migration was ever pushed to production, so the real database is unaffected. No action; continue to never edit a migration after it has been applied anywhere.

### 7.6 Test Hibernate config dumps SQL and misspells format_sql

`persistence/src/test/java/.../TestConfig.java:49-50` sets `hibernate.show_sql=true` and `format_sql=true`. The second key is missing the `hibernate.` prefix, so it has no effect. The result is enormous test output (`mvn -o test` prints every statement) and slower CI.

**Action:** remove `show_sql` or set it false, and use `hibernate.format_sql` if formatted SQL is genuinely wanted.

### 7.7 Obsolete SLF4J/Logback versions and redundant logging deps

`pom.xml:60-62` pins SLF4J `1.7.5` and Logback `1.1.2`, which are very old. Thymeleaf 3.1.3 expects SLF4J 2.0.x, and the root pin overrides it. `logback-ext-spring` has no listener/import, and `logback-core` is already pulled by `logback-classic`. The `service` module uses SLF4J directly but does not declare `slf4j-api`.

**Decision needed.** The versions are largely mandated by the course, which is likely the reason for the outdated stack. Confirm whether they may be upgraded; if not, document the constraint. The redundant/unused declarations can still be reviewed separately.

### 7.8 Plugin versions duplicated in every child POM

The same Maven plugin versions are copy-pasted in `model/pom.xml:44-83`, `persistence-contract/pom.xml:27-66`, `persistence/pom.xml:70-109`, `service-contract/pom.xml:31-70`, `service/pom.xml:78-117` and `webapp/pom.xml:159-214`.

**Decision needed.** The plugin versions may be course-mandated; confirm before consolidating them into the parent `<pluginManagement>` block.

### 7.9 Redundant Hibernate/JAXB dependencies and production JDBC declaration

`persistence/pom.xml:56-65` declares `hibernate-entitymanager` (deprecated compatibility artifact) and `jaxb-api` (already pulled by Hibernate core). `webapp/pom.xml:41-44` declares `spring-jdbc` at compile scope solely for `SimpleDriverDataSource` in `WebConfig`, while no JDBC DAO code remains.

**Action:** remove the redundant Hibernate/JAXB declarations. Keep `spring-jdbc` only if the `DataSource` configuration truly requires it, and document why; otherwise use the JPA/Spring configuration without a direct JDBC dependency.

### 7.10 Existence checks use COUNT or setMaxResults instead of EXISTS

`UserJpaDao.java:152-163` (username/email), `BrandJpaDao.java:34-41`, `ModelJpaDao.java:134-152` and `BrandModelRequestJpaDao.java:43-52` use `SELECT COUNT(...) > 0`; `BrandJpaDao.hasListings` and `ModelJpaDao.hasListings` use a `SELECT id ... setMaxResults(1)` query. The project convention is `SELECT EXISTS(SELECT 1 FROM ...)`.

**Action:** replace with `SELECT EXISTS(...)` returning `Boolean.class`.

### 7.11 Dead code, unused collection mapping and unused message keys

- `BrandModelRequest.isNewBrand()` (`:84-86`) has no callers.
- `Category.subcategories` (`:41-43`) is an inverse collection with no callers.
- `.gitkeep` files remain in `persistence/src/test`, `service/src/test` and `webapp/src/main/webapp/js` even though the directories are populated or unused.
- Unused keys in both bundles: `listing.new.submit` (line 123), `discovery.filter.query` (662), `offer.proofOfPayment.uploaded` (823), `offer.proofOfShipping.fileLabel` (832), `offer.proofOfShipping.uploaded` (838), `profile.location` (884, `messages.properties` numbering).

**Action:** remove dead code, the unused inverse mapping and the unused keys from both bundles.

---

## 8. Contradictions and decisions

These were contradictions between the code, the grading feedback, the lectures and `AGENTS.md`. Per the review request, the documents took precedence over `AGENTS.md`. All 8.x items are resolved below, and the only remaining code work is tracked as 4.7.

### 8.1 AGENTS requires subcategory-scoped request dedup; code and prior review say global

**Resolved.** `AGENTS.md:150` was outdated. It now documents `BrandModelRequestDao#findMatching(brandName, modelName)` as global across subcategories, matching the current contract, implementation and `V34` constraints and the prior review decision that brand + model are unique regardless of subcategory. No code change.

### 8.2 Lecture allows OSIV; AGENTS says do not rely on it

**Resolved as policy.** Relying on OSIV is discouraged but not a hard failure. `AGENTS.md` keeps its existing wording so the project does not encourage more OSIV use. The current OSIV filter is accepted for now, with the deploy marker added under 8.6. New code should still fetch view data inside the service transaction where practical.

### 8.3 AGENTS requires @Transactional on all services, then forbids it on EmailNotificationServiceImpl

**Resolved.** `AGENTS.md:108` now scopes the rule to services that access persistence and names `EmailNotificationServiceImpl` as the documented exception, consistent with the notifications section. The code needs no change.

### 8.4 TP2 says one DAO per service; AGENTS explicitly allows AdminServiceImpl to use two

**Resolved as guidance; code fix tracked as 4.7.** `AGENTS.md:243` no longer presents the multi-DAO `AdminServiceImpl` as an acceptable exception. It now says the current direct `ListingDao`/`OfferDao` calls are wrong, must not be copied, and must be moved into the owning services. The code change is tracked as item 4.7 in this review; no code change was made under 8.4.

### 8.5 AGENTS points to a review tracker file that does not exist

**Resolved.** `AGENTS.md:252` now points at `doc/review_3.md` for the current tracker and at `doc/review_summary_*.md` for the history of earlier reviews. No file was created under the old name.

### 8.6 The deploy-comment rule is violated by the OSIV TODO

**Resolved.** `web.xml:89` now contains the exact string `IMPORTANT: REVIEW BEFORE DEPLOY. DO NOT CHANGE THIS COMMENT AS IT IS SEARCHED BY GREP` alongside the OSIV removal TODO, so the pre-deploy grep will flag it.

---

## 9. Re-verified and deliberately not flagged

The following were checked and are **not** findings, usually because they match prior reviews, a deliberate decision, or verified behaviour:

- `ImageController` is public on purpose; proof documents use the authenticated `AccountController` endpoints.
- Formula columns (`Listing.coverImageId`, `pendingOffersCount`, `recentPendingOfferAt`) generate correctly correlated SQL; Hibernate qualifies the outer `listing_id` to `listing0_.listing_id`.
- `SiteNotificationJpaDao`, `BrandModelRequestJpaDao` and `ModelJpaDao#getListingInfoSummary` implement the 1+1 model correctly.
- Brand/model replacement moving listings across subcategories is intended admin behaviour.
- Free listings in the price summary, unformatted maximum price, clone-script trust, model replacement across subcategories, and offer-proof filename getters remain false positives from prior reviews.
- Brand/model request merging across subcategories is a deliberate prior decision (see 8.1).
- `mvn -o test` passes; `make troubleshoot` reports no unmanaged URLs.
- No `Mockito.verify`, `System.out`, `printStackTrace`, production `show_sql`, `mail.debug` or `hibernate.hbm2ddl.auto=update` was found.
- The production Logback configuration uses file appenders with rotation and no console appender.
- Spring Security's `CurrentUserArgumentResolver` correctly handles optional versus required users, and `RedirectHelper` rejected the open-redirect cases reviewed.
