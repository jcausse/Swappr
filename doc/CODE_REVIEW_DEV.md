# Swappr — code review of `dev`

## Status tracker

Status values: `todo`, `done`, `underspecified`, `needs bigger refactor`, `false positive`. Update the status column whenever an item is fixed.

| Item | Description | Status |
| --- | --- | --- |
| 1.1 | [Listing card images trigger an N+1 query per card](#11-listing-cards-load-every-image-of-every-listing-one-query-per-card-n1-graded--reproduced) | ✅ done |
| 1.2 | [Same brand/model requested in another subcategory returns 500](#12-requesting-the-same-brandmodel-in-a-different-subcategory-returns-a-500--reproduced) | ✅ done |
| 1.3 | [Reflected XSS through pagination links](#13-reflected-xss-through-the-pagination-links-public-pages--reproduced-earlier-code-unchanged) | ✅ done |
| 1.4 | [`suspendUser`: bulk operations in loops and swallowed exceptions](#14-adminserviceimplsuspenduser-bulk-operations-in-loops-and-swallowed-exceptions-inside-the-transaction-graded-still-open-from-the-e1-feedback) | ✅ done |
| 1.5 | [Writes done one row at a time in loops](#15-writes-done-one-row-at-a-time-in-a-loop-graded) | ✅ done |
| 1.6 | [Sorting done in Java](#16-sorting-done-in-java-graded) | ✅ done |
| 1.7 | [Business logic in the DAO](#17-business-logic-in-the-dao-graded) | ✅ done |
| 1.8 | [`Mockito.verify` used in a test](#18-mockitoverify-in-a-test-forbidden-by-the-course-rules) | ✅ done |
| 1.9 | [Boxed `Boolean` for always-present fields](#19-boxed-boolean-for-fields-that-are-always-present-graded-offerisfullprice-was-flagged-to-us) | ✅ done |
| 2.1 | [Session id not rotated on login (session fixation)](#21-login-without-rotating-the-session-id-session-fixation) | ✅ done |
| 2.2 | [`OpenEntityManagerInViewFilter` runs before Spring Security](#22-openentitymanagerinviewfilter-runs-before-spring-security) | ✅ done |
| 2.3 | [Free-text fields without a maximum length](#23-free-text-fields-without-a-maximum-length) | ✅ done |
| 2.4 | [Listing year not validated on the server](#24-the-listing-year-is-not-validated-on-the-server) | ✅ done |
| 2.5 | [`clone_prod_db.py` exposes production data on the LAN](#25-scriptclone_prod_dbpy-exposes-a-copy-of-production-data-on-the-local-network) | ❌ false positive |
| 2.6 | [User input written to logs as-is (log forging)](#26-user-input-written-to-the-logs-as-is-log-forging) | ✅ done |
| 3.1 | [Password recovery POST does not redirect (no PRG)](#31-password-recovery-post-does-not-redirect-no-prg) | ✅ done |
| 3.2 | ["Withdraw" offered for offers that cannot be withdrawn](#32-withdraw-is-offered-for-offers-that-cannot-be-withdrawn) | ✅ done |
| 3.3 | [Replacing/editing a model can move listings across subcategories](#33-replacing-or-editing-a-model-can-move-listings-to-another-subcategory) | ❌ false positive |
| 3.4 | [Brand/model names compared case-sensitively on rename/create/edit](#34-brand-and-model-names-are-still-compared-case-sensitively-when-renaming-a-brand-and-creating-or-editing-a-model) | ✅ done |
| 3.5 | [Price summary includes free listings and the listing being edited](#35-the-price-summary-in-the-listing-form-includes-free-listings-and-the-listing-being-edited) | ✅ done |
| 3.6 | [Admin catalog shows raw subcategory codes](#36-admin-catalog-shows-raw-subcategory-codes) | ✅ done |
| 3.7 | ["Already exists" page breaks names with commas and does not escape](#37-brandmodel-already-exists-page-breaks-names-with-commas-and-does-not-escape-them--reproduced) | ✅ done |
| 3.8 | [Price range "max" printed unformatted](#38-the-max-of-the-price-range-is-printed-unformatted) | ❌ false positive |
| 3.9 | [Takedown redirects built without URL-encoding](#39-takedown-redirects-in-the-admin-are-built-without-url-encoding) | ✅ done |
| 3.10 | [Admin catalog actions lose page, sort and filters; generic errors](#310-admin-catalog-actions-lose-page-sort-and-filters-and-report-generic-errors) | ✅ done |
| 3.11 | [Duplicate-request redirect empties the form](#311-the-duplicate-request-redirect-empties-the-form) | ❌ false positive |
| 3.12 | [Brand/model requests table `colspan` out of date](#312-brandmodel-requests-table-colspan-out-of-date) | ✅ done |
| 3.13 | [Changing category wipes the request form fields](#313-changing-the-category-in-the-brandmodel-request-form-wipes-what-the-user-typed) | ✅ done |
| 4.1 | [Circular service dependency broken with `@Lazy`](#41-circular-dependency-between-services-broken-with-lazy) | ✅ done |
| 4.2 | [`PermissionServiceImpl` write transactions for read-only checks; unused methods](#42-permissionserviceimpl-write-transactions-for-read-only-checks-and-methods-exposed-for-nothing) | ✅ done |
| 4.3 | [Authorization failures thrown as `BadParameterException`](#43-authorization-failures-thrown-as-badparameterexception) | ✅ done |
| 4.4 | [Domain exception in the web module](#44-domain-exception-in-the-web-module) | ✅ done |
| 4.5 | [A public model method returns `null`](#45-a-public-model-method-returns-null) | ✅ done |
| 4.6 | [Business rules decided in the views](#46-business-rules-decided-in-the-views) | ✅ done |
| 4.7 | [Categories queried on every request](#47-categories-queried-on-every-request) | ✅ done |
| 4.8 | [Smaller layering issues](#48-smaller-layering-issues) | ✅ done |
| 4.9 | [Duplicated code](#49-duplicated-code) | ✅ done |
| 4.10 | [Replacement dialogs build N² options on every request](#410-replacement-dialogs-build-n-options-on-every-request) | ✅ done |
| 5.1 | [Pagination without a tiebreaker](#51-pagination-without-a-tiebreaker) | ✅ done |
| 5.2 | [Unused average price](#52-unused-average-price) | ✅ done |
| 5.3 | [`MIN`/`MAX` over the embeddable](#53-minmax-over-the-embeddable) | ✅ done |
| 5.4 | [Mutable result types](#54-mutable-result-types) | ✅ done |
| 6.1 | [Tests with no assertion](#61-tests-with-no-assertion) | ✅ done |
| 6.2 | [Missing coverage of the brand/model request flow](#62-missing-coverage-of-the-brandmodel-request-flow) | ✅ done |
| 7.1 | [Default messages that do not exist](#71-default-messages-that-do-not-exist) | ✅ done |
| 7.2 | [Brand/model request validation messages](#72-brandmodel-request-validation-messages) | ✅ done |
| 7.3 | [Pluralization](#73-pluralization) | ✅ done |
| 8.1 | [Offer-rejected email CTA](#81-offer-rejected-email-cta) | ✅ done |
| 8.2 | [Pure-trade pending-payment email copy](#82-pure-trade-pending-payment-email-copy) | ✅ done |
| 9 | [Dead code cleanup (whole section)](#9-dead-code) | ✅ done |
| 10 | [Build and style cleanup (whole section)](#10-build-and-style) | ✅ done |
| 11.1 | [`ImageController` access control and caching](#111-imagecontroller-access-control-and-caching) | ✅ done |
| 11.2 | [Listing/Image relation](#112-listingimage-relation) | ✅ done |
| 11.3 | [JDBC dependencies in test scope](#113-jdbc-dependencies-in-test-scope) | ✅ done |
| 11.4 | [Form `.todto()` helpers](#114-form-todto-helpers) | ✅ done |
| 11.5 | [Prefer JPQL over `createNativeQuery`](#115-prefer-jpql-over-createnativequery) | ✅ done |
| 11.6 | [Avoid `Object[]` row mapping for non-entity models](#116-avoid-object-row-mapping-for-non-entity-models) | ✅ done |
| 12.1 | [`OfferServiceImpl.getById` runs `getPendingCounts` on `@PreAuthorize` checks (important)](#121-offerserviceimplgetbyid-runs-getpendingcounts-on-every-preauthorize-check-important) | 🕓 todo |
| 12.2 | [3 controllers query `getAllCategories()` twice per rendered request](#122-3-controllers-still-query-getallcategories-twice-per-rendered-request) | 🕓 todo |
| 12.3 | [`incomingOffers` computes transient flags for non-pending tabs](#123-accountcontrollerincomingoffers-computes-transient-flags-for-non-pending-tabs) | ✅ done |
| 12.4 | [`WebConfig` injects `@Lazy CatalogService` in its constructor](#124-webconfig-injects-lazy-catalogservice-in-its-constructor) | 🕓 todo |
| 12.5 | [Small test gaps in `CategoriesInterceptorTest` and `OfferJpaDaoTest`](#125-small-test-coverage-gaps-in-categoriesinterceptortest-and-offerjpadaotest) | 🕓 todo |
| 12.6 | [`CategoriesInterceptor.rendersView` does not skip `forward:` view names](#126-categoriesinterceptorrendersview-does-not-skip-forward-view-names) | 🕓 todo |
| 12.7 | [Remove `p.amount > o.amount` comparison from `OfferJpaDao.getPendingCounts` (important)](#127-remove-pamount--oamount-comparison-from-offerjpadaogetpendingcounts-important) | 🕓 todo |

Things to fix before the final delivery. Items 1-10 were checked against `dev` (`a80c04fd`, 2026-11-11); section 11 was checked against `dev` (`a385aa63`, 2026-11-13); section 12 was added against `dev` (`ea1b01a1`, 2026-11-14). Nothing here is a guess: each one points to the file and line where it happens.

- **Reproduced** means it was executed and observed (a throwaway test or the running app). Everything else was confirmed by reading the code of `dev`.
- The full build (`mvn clean install`) passes on `dev`. None of these are caught by the build.
- Items marked **(graded)** match a pattern the cátedra penalized as a conceptual error in the TP1 review (ours or another group's).
- Paths are shortened: `service/…` means `service/src/main/java/ar/edu/itba/paw/service/…`, and so on.

---

## 1. Highest priority

### 1.1 Listing cards load every image of every listing, one query per card (N+1) (graded) — reproduced

- `Listing.getImageIds()` (`model/…/Listing.java:82-87`) maps over the `images` collection. `listingCard.tag:15-16` and `listingImage.tag:23-24` call `listing.imageIds[0]` to get the cover.
- `ListingJpaDao.search` (`persistence/…/ListingJpaDao.java:154-160`) does not fetch the images, so each card triggers its own query when the tag reads `imageIds`.
- That query also selects the `data` column, so it loads the full bytes of every image of the listing just to read the first id.
- Reproduced with a throwaway DAO test and Hibernate SQL logging: a page of 4 listings ran 1 search query plus 4 extra queries against `listing_images` + `images`, each one selecting `image1_.data`.
- Impact: discovery runs 12 extra queries per page, and the landing up to 36 (three carousels of 12), each pulling whole images into memory. The same happens on every page that uses `paw:listingImage` (my listings, offers, profile, admin listings, checkout).
- Fix: get the cover id without loading images. For example, a `@Formula` or a separate batched query `SELECT listing_id, image_id FROM listing_images WHERE listing_id IN (:ids) AND display_order = 0`. Never select `images.data` outside `/image/{id}`.

### 1.2 Requesting the same brand/model in a different subcategory returns a 500 — reproduced

- `BrandModelRequestJpaDao.findMatching` (`persistence/…/BrandModelRequestJpaDao.java:28-42`) filters by `r.subcategory.id = :subcategoryId` (`:33`).
- `V34__Create_brand_model_request_additional_users.sql:12` and `:15` add `UNIQUE (brand_id, model_name)` and `UNIQUE (brand_name, model_name)` without the subcategory.
- If a pending request exists in another subcategory, `findMatching` returns empty, `BrandModelRequestServiceImpl.create` (`service/…/BrandModelRequestServiceImpl.java:84-97`) tries to insert a second row, and the database rejects it. The user sees the 500 page.
- Reproduced in the DAO for both request types (`UNIQUE_BRAND_MODEL_REQUESTS_NEW_BRAND_MODEL` and `UNIQUE_BRAND_MODEL_REQUESTS_BRAND_MODEL`).
- Real example: ASUS «ROG Strix» requested by one user in Laptops and by another in Graphics cards. The second one gets the 500.
- Fix: make `findMatching` ignore the subcategory. The catalog already treats brand + model as unique regardless of subcategory (`models` has `UNIQUE (model_name, brand_id)`), so the second requester should be added as an additional requester, and the admin picks the subcategory when accepting. Add a test with two requests of the same model in different subcategories.

### 1.3 Reflected XSS through the pagination links (public pages) — reproduced earlier, code unchanged

- `pagination.tag:16-20` copies every query parameter raw into `keptQuery`, and `:25-28` build the links from it. `linkButton.tag:74` prints `href="${href}"` without escaping.
- `/listing?zz="onmouseover="alert(1)" x="` produces `href="/listing?zz="onmouseover="alert(1)" x="&page=1"`: the `"` closes the attribute and injects an event handler.
- Every paginated page is affected, including the public discovery.
- Fix: build the query with `<c:param>` inside `<c:url>` (it URL-encodes values) and escape the `href` in `linkButton.tag`.

### 1.4 `AdminServiceImpl.suspendUser`: bulk operations in loops and swallowed exceptions inside the transaction (graded, still open from the E1 feedback)

- `service/…/AdminServiceImpl.java:62-150` (`// TODO: Bro, this is just AI slop` at `:62`).
- It searches the user's active listings with `pageSize(1000)` and cancels them one by one (`:90`). It does the same for buyer offers per status group (`:103`, `:113`) and for seller offers pending payment (`:137`).
- Each iteration is wrapped in `catch (Exception e)` (`:93`, `:98`, `:116`, `:121`, `:140`, `:145`) inside a `@Transactional` method. If a cancellation marks the transaction for rollback, the catch hides it and the loop keeps running on a doomed transaction.
- `pageSize(1000)` also silently ignores anything past the first 1000 items.
- Fix: bulk `UPDATE`s (`WHERE creator_id = :id AND status = 'ACTIVE'`, and a bulk reject for offers), and no `catch (Exception)` inside the transaction.

### 1.5 Writes done one row at a time in a loop (graded)

- `OfferJpaDao.rejectPendingOffers` (`persistence/…/OfferJpaDao.java:140-155`) loads every pending offer and calls `setStatus(REJECTED)` on each one (`:151`). Hibernate issues one `UPDATE` per offer. Use one bulk `UPDATE … WHERE listing_id = :id AND status = 'PENDING'`.
- `OfferServiceImpl.autoRatePendingOffers` (`service/…/OfferServiceImpl.java:560`, loop at `:564`) runs two `UPDATE`s plus two counter increments per offer.
- `OfferServiceImpl.rejectPendingOffersForListing` (`:325`) and `NotificationDispatcherServiceImpl.notifyBrandModelRequestAccepted` / `Rejected` (`service/…/notification/NotificationDispatcherServiceImpl.java:236`, `:262`) create one notification (one `INSERT`) and send one email per recipient, inside the transaction.
- `ImageJpaDao.createAll` (`persistence/…/ImageJpaDao.java:44`) persists images with `forEach(em::persist)`. With `GenerationType.IDENTITY`, Hibernate cannot batch those inserts, so it is one `INSERT` per image.

### 1.6 Sorting done in Java (graded)

- `CatalogServiceImpl` sorts the listing summary by condition in memory (`service/…/CatalogServiceImpl.java:286` and `:302`, `.sorted(Comparator.comparingInt(…ordinal()))`). Order it in the query.

### 1.7 Business logic in the DAO (graded)

- `OfferJpaDao.populateTransientFlags` (`persistence/…/OfferJpaDao.java:248-287`) computes `hasOtherOffers` / `hasBetterOffers` (comparing amounts between offers). The code itself says `// TODO: This should not be here. Move this to Service layer`. Move the decision to the service and leave the DAO as a query.

### 1.8 `Mockito.verify` in a test (forbidden by the course rules)

- `UserServiceImplTest.java:189`: `verify(userDao).updatePreferredLanguage(USER_ID, Language.SPANISH);` (test `testUpdatePreferredLanguagePersistsChosenLanguage`). Assert the returned user instead.

### 1.9 Boxed `Boolean` for fields that are always present (graded, `Offer.isFullPrice` was flagged to us)

- `Offer.isFullPrice` (`model/…/Offer.java:39`), `Listing.acceptsTrade` (`model/…/Listing.java:60`) and `Listing.acceptsShipping` (`:63`) are `Boolean`, but their columns are `nullable = false`. Use `boolean`.

---

## 2. Security

### 2.1 Login without rotating the session id (session fixation)

- `AuthHelper.login` (`webapp/…/auth/AuthHelper.java:36-41`) sets the `Authentication` by hand and never changes the session id. It is used after email verification (`EmailVerificationController.java:56`) and after password recovery (`PasswordRecoveryController.java:84`).
- Fix: call `request.changeSessionId()` right after authenticating.

### 2.2 `OpenEntityManagerInViewFilter` runs before Spring Security

- `webapp/src/main/webapp/WEB-INF/web.xml`: the OEMIV `filter-mapping` (`:85-88`) comes before `springSecurityFilterChain` (`:96-99`). It must go after it. The encoding filter is correctly first.

### 2.3 Free-text fields without a maximum length

- `CheckoutForm.message` (`webapp/…/form/CheckoutForm.java:41`), `AdminListingFilterForm.query` and `AdminUserFilterForm.username` have no `@Size`.

### 2.4 The listing year is not validated on the server

- Only the wizard step checks the 1900–current-year range (`ChooseProductValidator.java:79`).
- `ListingDetailsForm.year` only has `@NotNull`, it travels as a hidden field, and `ListingServiceImpl.create` / `update` store it as received (`service/…/ListingServiceImpl.java:175`, `:268`). Editing the hidden field publishes a listing with year `99999` or `-5`.
- Fix: validate in the service (`BadParameterException`) sharing the range constant with the validator, and add `@Min`/`@Max` to the form.

### 2.5 `.script/clone_prod_db.py` exposes a copy of production data on the local network

- `:267` publishes the container with `-p "{port}:5432"`, which listens on every interface, with user and password `postgres`/`postgres`. Anyone on the same network can read real users' emails and password hashes. Use `127.0.0.1:{port}:5432`.
- `:182` and `:213` use `StrictHostKeyChecking=no`, and `:44` uses the unpinned `postgres:latest` image.
- **False positive:** non-issue, the script needs production credentials to clone the database, so anyone running it is trusted.

### 2.6 User input written to the logs as-is (log forging)

- A value with line breaks can fake log lines. Examples: `AdminController.java:198`, `:271` and `:274` (brand/model names), `:566`, `:594` and `:614` (the typed username or email), `BrandModelRequestController.java:62`, `PasswordRecoveryController.java:50` and `GlobalExceptionHandler.java:69` (message containing the model name).
- Log ids or results instead of free text, or strip line breaks.

---

## 3. User-visible bugs

### 3.1 Password recovery POST does not redirect (no PRG)

- `PasswordRecoveryController.passwordRecoveryRequestPOST` (`webapp/…/controller/PasswordRecoveryController.java:41`) renders the view directly after sending the email. Reloading the page resubmits the form and sends another recovery email. Redirect after the POST.

### 3.2 "Withdraw" is offered for offers that cannot be withdrawn

- `account/myOffers.jsp:46-48` shows the withdraw form for every offer in the "Pending" and "Pending payment/delivery" tabs. `OfferServiceImpl.withdraw` only accepts `PENDING` (`service/…/OfferServiceImpl.java:290`, `BadParameterException("Offer is not pending")`), so withdrawing an offer that is pending payment or delivery shows an error page.

### 3.3 Replacing or editing a model can move listings to another subcategory

- `CatalogServiceImpl.replaceModel` (`service/…/CatalogServiceImpl.java:254`) moves every listing to the target model without comparing subcategories, so a laptop listing can end up as a phone.
- `CatalogServiceImpl.updateModel` (`:225`) lets an admin change the subcategory of a model that already has listings.
- **False positive:** intended behavior.

### 3.4 Brand and model names are still compared case-sensitively when renaming a brand and creating or editing a model

- `getByName`, `getByNameAndBrandId` and `findMatching` already ignore case, which covers creating a brand and detecting duplicate requests. The three `exists…` methods still compare exactly:
  - `BrandJpaDao.existsByNameAndIdNot` (`BrandJpaDao.java:36`), used by `CatalogServiceImpl.renameBrand` (`CatalogServiceImpl.java:115`): an admin can rename a brand to «apple» while «Apple» exists.
  - `ModelJpaDao.existsByNameAndBrandId` (`ModelJpaDao.java:123`), used by `CatalogServiceImpl.createModel` (`:217`): an admin can create «macbook pro» next to «MacBook Pro» for the same brand.
  - `ModelJpaDao.existsByNameAndBrandIdAndIdNot` (`ModelJpaDao.java:133`), used by `CatalogServiceImpl.updateModel` (`:234`): same thing when editing a model.
- The database does not stop it either: `UNIQUE (brand_name)` and `UNIQUE (model_name, brand_id)` compare exactly.
- Fix: apply the same `LOWER(...) = LOWER(:name)` to the three `exists…` queries, with a test for each.

### 3.5 The price summary in the listing form includes free listings and the listing being edited

- `ModelJpaDao.getListingInfoSummaryForModel` (`persistence/…/ModelJpaDao.java:296-305`) filters by `status = ACTIVE` but not by `price > 0`, so the minimum becomes "Free" and a seller of a paid item sees "Free – 50.00" as a reference. When editing, the listing itself is also counted.
- **Intended behavior:** including free listings in the price range is intentional. Only the listing being edited must be excluded.

### 3.6 Admin catalog shows raw subcategory codes

- `AdminController.java:123-126` builds the subcategory options as `category.getName() + " / " + subcategory.getName()`. Those names are the DB codes (`computers / laptops`), not translated labels. Resolve the label in the view with `<spring:message code="category.…"/>` / `subcategory.…`, like the rest of the app.

### 3.7 Brand/model "already exists" page breaks names with commas and does not escape them — reproduced

- `jsp/error/modelAlreadyExists.jsp:25`: `<spring:message … arguments="${brandName},${modelName}"/>`. `arguments` as a string is split on commas, and `spring:message` does not escape its output (there is no `defaultHtmlEscape`).
- Reproduced: with the brand «Dolce, Gabbana» the message reads «The model Dolce  Gabbana already exists…» and the model disappears. A value with HTML is rendered raw.
- Fix: `<spring:message code="…" var="msg"><spring:argument value="${brandName}"/><spring:argument value="${modelName}"/></spring:message>` and print `<c:out value="${msg}"/>`.

### 3.8 The "max" of the price range is printed unformatted

- `jsp/listing/new/details.jsp:109`: the minimum goes through `paw:price` (currency and format), the maximum is `<c:out value="${info.maxPrice.amount}"/>`. Use `paw:price` for both.
- **False positive:** intended behavior, the maximum price is always preceded by the formatted minimum, and we don't want to show the currency twice.

### 3.9 Takedown redirects in the admin are built without URL-encoding

- `jsp/admin/listings.jsp:131` and `jsp/admin/offers.jsp:153` build the hidden `redirect` value with `<c:out>`, which escapes HTML but not URLs. A search with `&`, `#` or `%` changes or cuts the destination parameters. Build it with `<c:url>` + `<c:param>`.
- **Update:** these redirects are now built server-side as `listingsReturnUrl` / `offersReturnUrl`, because `<c:url>` also prepends the context path, which `RedirectHelper` rejects against the `/admin` path predicate (it would silently fall back to a state-less redirect in production).

### 3.10 Admin catalog actions lose page, sort and filters, and report generic errors

- **Done.** The catalog page now builds a single canonical return URL (`catalogReturnUrl`) with its current page, sort, model page/sort, request page and model filters, and passes it through a hidden `redirect` field on every catalog form and dialog. Each POST redirects via `RedirectHelper.resolvePath` + `RedirectHelper.appendQueryParam`, so page/sort/filter state survives success and error outcomes.
- Error specificity: `CatalogServiceImpl` now throws `BrandAlreadyExistsException`, `ModelAlreadyExistsException` and `CatalogEntityInUseException` (all `BadParameterException` subclasses) instead of generic `BadParameterException`s. The controller maps the brand/model name conflicts to `brandNameTaken` / `modelNameTaken`, including in the brand/model request acceptance flow, which previously reported everything as `requestInvalid`.
- Defensive catches for exceptions that cannot happen through the UI (for example a missing model on delete, or deleting a brand/model that has listings, or a self-replacement) were removed from `AdminController`; those now fall through to the global error screen. The obsolete `brandNotFound`, `brandHasListings`, `brandReplaceSame`, `modelNotFound`, `modelHasListings` and `modelReplaceSame` message keys were deleted.

### 3.11 The duplicate-request redirect empties the form

- `GlobalExceptionHandler.java:70` redirects to `/catalog-requests/new?duplicate=1`, losing the `categoryId`, `subcategoryId` and `brandId` the user came with from the listing wizard. Without `categoryId` the subcategory select is not even rendered.
- **False positive:** no longer relevant, the code has been deleted/moved.

### 3.12 Brand/model requests table: `colspan` out of date

- `jsp/admin/catalog.jsp:555` uses `colspan="7"` for the empty state, but the table has 6 columns since the ID column was removed.

### 3.13 Changing the category in the brand/model request form wipes what the user typed

- In `jsp/catalogRequest/new.jsp`, the category `change` handler reloads the page with `window.location.href = '/catalog-requests/new?categoryId=…'`. Only `categoryId` travels, so the request type, the selected brand, the new brand name and the model name the user already filled in are lost.
- Fix: keep the other fields in the reload (or submit the form to re-render it), or load the subcategories without leaving the page.

---

## 4. Architecture and layering

### 4.1 Circular dependency between services broken with `@Lazy`

- `OfferServiceImpl.java:57` injects `@Lazy ListingService`. Extract the shared logic into a third service.

### 4.2 `PermissionServiceImpl`: write transactions for read-only checks, and methods exposed for nothing

- `service/…/PermissionServiceImpl.java` has no class-level `@Transactional(readOnly = true)`, and every check is a write `@Transactional` (`:18`, `:25`, `:31`, `:37`, `:51`, `:58`, `:65`). Only `canManageNotification` is `readOnly`.
- `PermissionService.java:13-15` exposes `isListingCreator`, `isOfferSeller` and `isOfferBuyer`. `isListingCreator` is not called anywhere, and the other two are only used inside the implementation.

### 4.3 Authorization failures thrown as `BadParameterException`

- `OfferServiceImpl.java:285` (withdraw), `:344` (proof of payment), `:394` (proof of shipping) and `:448` (confirm payment) throw `BadParameterException` for "not authorized" / "only the buyer/seller can…". `accept` and `reject` already use `ForbiddenException` (`:211`, `:249`). `:391` has `// TODO remove this validation after implementing preatuhorize`.

### 4.4 Domain exception in the web module

- `webapp/…/exception/UserNotFoundException.java` is a domain exception. Domain exceptions live in `service-contract/…/exception`.

### 4.5 A public model method returns `null`

- `Offer.getUserRole` (`model/…/Offer.java:205-209`) returns `null` when the user did not take part in the offer. Return `Optional` or make it private.

### 4.6 Business rules decided in the views

- `tags/account/offerTable.tag:68` and `:73` decide whether an offer can be rated by comparing `offer.status.name() == 'ACCEPTED'`.
- `jsp/admin/offers.jsp:151` decides whether an offer can be taken down by comparing status strings.
- The model already has predicates (`canBeRatedBy`, `isPending`, …). Use them.

### 4.7 Categories queried on every request

- `CommonModelAdvice.java:20-22` runs `getAllCategories()` before every handler, including POSTs that only redirect and the image endpoint (flagged to other groups).

### 4.8 Smaller layering issues

- `EmailVerificationController.java:70` validates by hand (`// TODO: Usar Bean Validation`).
- `OfferServiceImpl.java:236`, `:272` and `:305`: `// FIXME double query`. The offer is reloaded after the write.
- `ListingServiceImpl.cancelByAdmin` (`service/…/ListingServiceImpl.java:312-317`) rejects the pending offers before checking that the listing exists. `cancel` already does it in the right order.

### 4.9 Duplicated code

- `BrandModelRequestValidator.isValid` (`webapp/…/form/validation/BrandModelRequestValidator.java:12`) and `BrandModelRequestAcceptValidator.isValid` (`BrandModelRequestAcceptValidator.java:13`) have identical bodies (a `diff` only shows the parameter type). The two forms also repeat the same fields and annotations. Use a small interface implemented by both forms and a single validator.
- `ModelJpaDao` maps rows to `ModelListingInfo` twice (`persistence/…/ModelJpaDao.java:259` and `:312`).
- `ListingController` adds `priceSummary` in three handlers (`:310`, `:366`, `:408`).
- **Done.** `ListingController` builds the details view in one helper. The `ModelJpaDao` mapping was already removed by 11.6 (constructor projections). Both request forms implement `BrandModelRequestFields`, and the brand rule both validators repeated now lives once in `BrandModelRequestBrandRule`.

### 4.10 Replacement dialogs build N² options on every request

- `CatalogServiceImpl.getBrandReplacementOptions` (`:70`) and `getModelReplacementOptions` (`:191`) build a "everyone except me" list for every brand/model on each GET of `/admin/catalog` (`AdminController.java:121`, `:131`), even though the page shows 10 rows. Pass one list and exclude the own id in the view.

---

## 5. Persistence

### 5.1 Pagination without a tiebreaker

- **Done.** Offer and rating searches now order by `createdAt DESC, id DESC`, so rows cannot repeat or disappear between pages when timestamps tie.

### 5.2 Unused average price

- **Done.** Removed `avgPrice` from `ModelListingInfo` and dropped the `AVG(l.price.amount)` aggregate from both summary queries.

### 5.3 `MIN`/`MAX` over the embeddable

- **Verified done (already applied with 11.6).** Both summary queries now aggregate `l.price.amount`. Aggregating the `Price` embeddable was ambiguous: JPQL aggregates are scalar operations, so relying on Hibernate to treat the component as its single amount column was provider-specific and could compare/select the component as a whole rather than the amount. Using `l.price.amount` makes the scalar intent explicit.

### 5.4 Mutable result types

- **Done.** `ModelListingInfo` and `ModelListingInfoSummary` are now immutable `@Value` types with builders, `listingCount` is a primitive `long`, and the summary defensively copies its list. A broader pass found the other result types already immutable (`Page`, `ListingFilter`/`OfferFilter`/`RatingFilter`, `BrandInfo`/`ModelInfo`, records); `FileDto` is mutable by design as an upload input and is not a query result.

---

## 6. Tests

### 6.1 Tests with no assertion

- **Done.** Removed the no-assertion pseudo-delegation tests from `CatalogServiceImplTest`, including `testRenameBrandChangingOnlyCaseIsAllowed`, which also had no assertion. The existing return-valued delegation tests already assert the DAO result; no void delegation tests were kept.

### 6.2 Missing coverage of the brand/model request flow

- **Done.** Added direct tests for `NotificationDispatcherServiceImpl.notifyBrandModelRequestAccepted` / `Rejected`, `SiteNotificationCreationDtoFactory.brandModelRequestAccepted` / `Rejected`, and the missing `BrandModelRequestServiceImpl.create` validations (missing type, blank model, missing/unknown subcategory, missing user, missing existing brand).

---

## 7. i18n

### 7.1 Default messages that do not exist

- **Done, without adding new keys.** `ValidBrandModelRequest.java:16` and `ValidBrandModelRequestAccept.java:16` pointed to keys that do not exist. Both defaults are unreachable in practice because the validators disable the default constraint violation before adding their own messages, so the defaults now reuse the existing `typeMismatch` key instead of introducing dead bundle entries.

### 7.2 Brand/model request validation messages

- **Done.** Conditional field validation now lives in `BrandModelRequestValidator`, which emits `error.catalogRequest.*` keys (`typeRequired`, `categoryRequired`, `subcategoryRequired`, `modelRequired`, `modelTooLong`, `newBrandRequired`, `newBrandTooLong`, `existingBrandRequired`). The form no longer carries the field constraints that used to fall back to generic Hibernate Validator messages.

### 7.3 Pluralization

- **Done.** `admin.catalog.requests.additionalOthers` now uses the choice format in both bundles (`+ {0,choice,1#+ 1 other|1<+ {0} others}` and the Spanish equivalent).

---

## 8. Mail

### 8.1 Offer-rejected email CTA

- **Done.** The "Browse listings" button in `mail/offer-rejected.html` now links to `${baseUrl + '/listing'}` instead of the home page.

### 8.2 Pure-trade pending-payment email copy

- **Done.** `email.offer.pendingPayment.body.trade` no longer asks the buyer to arrange payment or upload proof; it now asks them to arrange the exchange and delivery, which also fits a pure trade with no money involved.

---

## 9. Dead code

- **Done.** Removed the unused `tags/carousel.tag`; the landing page only uses `listingCarousel.tag`.
- Removed unused `fromString` helpers from `Condition`, `ListingSort`, `ListingStatus`, `OfferRating`, `OfferStatus`, `OfferStatusGroup`, `RatingRole` and `Role`.
- Removed unused getters: `BrandModelRequest.getBrandOptional`, `File.getExtension`, `File.getSizeKb`, `File.isImage`, `Offer.getAcceptedAt`, `User.getEmailVerifiedAt`, `User.getSuspendedAt` and `User.getPreferredLocale`. `Offer.getProofOfPaymentFilename` / `getProofOfShippingFilename` are kept because the proof upload tags render them via JSP EL.
- Removed `CatalogService.getAllModels` and its test, plus the unused `subcategories` model attribute from `ListingController.populateModel`.
- Removed 53 unused i18n keys from both bundles and the dead `admin.catalog.requests.id` lookup in `admin/catalog.jsp`.
- Removed unused `mockito-core` from `persistence`. `mockito-core` and `spring-test` stay in `webapp` because `AuthHelperTest` and `RedirectHelperTest` use Mockito and `MockHttpServletRequest`; `spring-jdbc` stays in `webapp` because `WebConfig` uses it in main code.
- False positives / kept: `BrandModelRequestController.render(form)` still reads `form`; the Offer compatibility getters are still used by services/tags; the `Listing` image mapping and its TODO are handled by the pending 11.2 branch.

---

## 10. Build and style

- **Compiler warning.** `model/…/Listing.java:72`: `@Builder will ignore the initializing expression entirely` for `images = new ArrayList<>()`. Add `@Builder.Default`. This is the only Java warning in the build.
- **Relocated dependency.** The build warns that `org.hibernate:hibernate-validator` was relocated to `org.hibernate.validator:hibernate-validator` (`pom.xml:120`, `webapp/pom.xml:66`). Update the `groupId`.
- **Wildcard imports.**
  - Every entity (`Brand`, `BrandModelRequest`, `Category`, `File`, `Image`, `Listing`, `Model`, `Offer`, `OneTimePassword`, `Price`, `Province`, `Rating`, `SiteNotification`, `Subcategory`, `User`) imports `lombok.*` and/or `javax.persistence.*`.
  - `UserController` (`ar.edu.itba.paw.model.*`), `OfferController` (`org.springframework.web.bind.annotation.*`) and `ValidProofOfShippingUploadForm` (`java.lang.annotation.*`).
  - In tests: `import static org.mockito.Mockito.*` in `ImageServiceImplTest`, `ListingServiceImplTest`, `OfferServiceImplTest`, `OneTimePasswordServiceImplTest`, `ProvinceServiceImplTest` and `UserServiceImplTest`, and `ar.edu.itba.paw.model.*` in `NotificationDispatcherServiceImplTest`, `SiteNotificationCreationDtoFactoryTest` and `SiteNotificationServiceImplTest`.
- **Tabs instead of 4 spaces.** `PermissionServiceImpl.java` (38 lines), `WebPermissionService.java` (17), `AccountController.java` (11), `persistence/src/test/…/TestConfig.java` (9), `PermissionService.java` (8), `OfferServiceImpl.java` (4) and `OfferServiceImplTest.java` (1).

---

## 11. Old TODO items still pending

These items were imported from the now-removed `doc/TODO.md` and `doc/notes/DEBT.txt`, and checked against `dev` (`a385aa63`, 2026-11-13).

### 11.1 `ImageController` access control and caching

- **Done (caching only).** Access control is intentionally unchanged: this endpoint only serves public listing images and profile pictures, while proof-of-payment/shipping files use their own authenticated endpoints.
- `getImage` now sets `Cache-Control: public, max-age=86400` and an `ETag` derived from the immutable image id. `WebRequest.checkNotModified` returns `304 Not Modified` when `If-None-Match` matches, so clients can keep the cached copy for a day and revalidate cheaply afterwards.

### 11.2 Listing/Image relation

- `Listing.java:68-75` still maps `@OneToMany @JoinTable List<Image>`; there is no `ListingImage` entity. The `coverImageId` formula (`:77`) fixes the cover N+1, but the requested intermediate entity with a lazy `Image` was never modelled.

### 11.3 JDBC dependencies in test scope

- **Done.** `spring-jdbc` is now test-scoped in `persistence`, where only the DAO tests use it.
- `webapp` keeps `spring-jdbc` as a compile dependency because `WebConfig.dataSource()` uses `SimpleDriverDataSource` in main code; it is not a test-only dependency there.

### 11.4 Form `.todto()` helpers

- No form exposes `toDto()`/`todto()`; controllers still instantiate DTOs inline (`CheckoutController` builds `OfferCreationDto`, `ListingController` builds `ListingCreationDto`/`ListingUpdateDto`, `UserController` builds `UserCreationDto`/`UserEditDto`, etc.). Add `toDto()` methods to the forms that map to a service DTO to keep that mapping in one place.

### 11.5 Prefer JPQL over `createNativeQuery`

- Audit the five native call sites and keep only the ones that cannot be expressed as typed JPQL:
  - `ModelJpaDao.java:230` (native count) and `:238` (native model-id page) in `getListingInfoSummary`. The ID query is part of the mandated 1+1 model, but the count can likely use JPQL with the same filters.
  - `ModelJpaDao.java:289` (`replaceBrand`) deletes old same-name models through a native `DELETE ... WHERE EXISTS`. This can likely be a JPQL bulk delete.
  - `ListingJpaDao.java:224` (`replaceSameNameModelsForBrands`) uses a native `UPDATE ... SET ... = (correlated subquery)`. Rewrite if JPQL supports the update, otherwise document why it must stay native.
  - `SiteNotificationJpaDao.java:137` and `BrandModelRequestJpaDao.java:74` are native ID queries for the 1+1 pagination model. These are intentional under `AGENTS.md`; do not replace them with fetch-join entity queries.

### 11.6 Avoid `Object[]` row mapping for non-entity models

- `BrandJpaDao.getWithStats` (`:69-79`) and `ModelJpaDao.getWithInfo` (`:104-119`) build `BrandInfo`/`ModelInfo` field by field, and both compute `hasListings` from `count > 0`. Store the count in the model (`listingCount`/`modelCount`-style field) and expose `hasListings()` as a derived getter, then let Hibernate/JPQL project into the model directly.
- `ModelJpaDao.getListingInfoSummary` (`:265-279`) and `getListingInfoSummaryForModel` (`:322-338`) also map `Object[]` rows into `ModelListingInfo` manually. A constructor expression/DTO projection, or small model adjustments, would remove the casting.
- `OfferJpaDao.countByStatus` (`:188`) and `populateTransientFlags` (`:285-308`) are the other `Object[]` consumers; `populateTransientFlags` is already covered by 1.7.

---

## 12. Follow-ups from code-review fixes

These follow-ups were identified when reviewing the 4.7 and 1.7 fixes against `dev` (`ea1b01a1`, 2026-11-14).

### 12.1 `OfferServiceImpl.getById` runs `getPendingCounts` on every `@PreAuthorize` check (important)

- `PermissionServiceImpl.canManageOfferAsSeller`, `canManageOfferAsBuyer` and `canAccessOffer` (`service/…/PermissionServiceImpl.java:25-43`) call `offerService.getById(offerId)` inside `@PreAuthorize` on every offer action and page (`accept`, `reject`, `withdraw`, `confirm-payment`, payment/shipping uploads, rating, `incomingOfferDetail`).
- Because `OfferServiceImpl.getById` (`service/…/OfferServiceImpl.java:70-74`) always runs `populateTransientFlags`, every authorized offer endpoint triggers an extra `offerDao.getPendingCounts` JPQL query even when the flags are unused, and `AccountController.incomingOfferDetail` (`webapp/…/controller/AccountController.java:144`) executes it twice in the same request (JPQL queries are not L1-cached).
- Fix: expose an opt-in flag or separate method (e.g. `getById(Long id, boolean populateTransientFlags)` or `getByIdWithFlags(Long id)`) and pass `false` in `PermissionServiceImpl` and non-detail callers.

### 12.2 3 controllers still query `getAllCategories()` twice per rendered request

- `ListingController.populateModel` (`webapp/…/controller/ListingController.java:252`), `AdminController.catalog` (`AdminController.java:134`) and `BrandModelRequestController.render` (`BrandModelRequestController.java:99`) call `catalogService.getAllCategories()` to build `categoryOptions` without placing `"categories"` in the `ModelAndView`, so `CategoriesInterceptor` queries `getAllCategories()` a second time on those rendered views.
- Fix: add the fetched `categories` list to the model (matching `ListingController.discovery` at `:75-79`) so `CategoriesInterceptor` skips the duplicate query.

### 12.3 `AccountController.incomingOffers` computes transient flags for non-pending tabs

- `AccountController.incomingOffers` (`webapp/…/controller/AccountController.java:98`) passes `populateTransientFlags = true` for every `statusGroup` tab, including `RESOLVED` (where `tags/account/offerTable.tag` never reads `hasOtherOffers`/`hasBetterOffers`) and `PENDING_PAYMENT`.
- Additionally, `tags/account/offerCard.tag:100` checks `${offer.hasOtherOffers && user == 'buyer'}` without checking `offer.isPending()` (unlike `jsp/account/incomingOfferDetail.jsp:77`).
- Fix: pass `true` only when `filter.statusGroup()` is `PENDING` (or `null`), and guard the banner in `offerCard.tag` with `offer.isPending()`.

### 12.4 `WebConfig` injects `@Lazy CatalogService` in its constructor

- `WebConfig` (`webapp/…/config/WebConfig.java:70-75`) injects `@Lazy CatalogService` in its constructor to break the bean-creation cycle with `@Bean`s declared on `WebConfig` (such as `entityManagerFactory()`).
- Fix: register `CategoriesInterceptor` via a `@Bean` factory method `categoriesInterceptor(CatalogService)` (similar to `localeResolver(UserService, AuthHelper)` at `:122`) so `CatalogService` is not constructor-injected with `@Lazy`.

### 12.5 Small test coverage gaps in `CategoriesInterceptorTest` and `OfferJpaDaoTest`

- `CategoriesInterceptorTest` (`webapp/…/interceptor/CategoriesInterceptorTest.java`) does not cover `modelAndView == null` (the `ResponseEntity`/`@ResponseBody`/static-resource path) or `new ModelAndView()` (`!hasView()`).
- `OfferJpaDaoTest` (`persistence/…/OfferJpaDaoTest.java:307-336`) does not cover equal-amount pending offers (`betterCount == 0` on a tie) or `null` input for `getPendingCounts`.

### 12.6 `CategoriesInterceptor.rendersView` does not skip `forward:` view names

- `CategoriesInterceptor.rendersView` (`webapp/…/interceptor/CategoriesInterceptor.java:36-44`) only excludes `"redirect:"` view names and `RedirectView`. No controller currently returns `"forward:..."`, so it is harmless today, but excluding `"forward:"` prevents querying categories twice if a handler ever forwards internally.

### 12.7 Remove `p.amount > o.amount` comparison from `OfferJpaDao.getPendingCounts` (important)

- **Important (graded 1.7 follow-up):** `OfferJpaDao.getPendingCounts` (`persistence/…/OfferJpaDao.java:95-109`) still embeds the offer-to-offer comparison `p.amount > o.amount` inside JPQL to compute `betterCount`. Even though `OfferServiceImpl` decides the final booleans, a strict reviewer may still consider "what makes one offer better than another" to be business logic inside the DAO query.
- **Equally efficient pure-service alternative:** Replace the two correlated subqueries per offer with a single `GROUP BY o.listing.id` query over `listingIds` (obtainable from `offer.getListing().getId()` on the lazy proxy without hitting `listings`):
  - DAO returns `(listingId, COUNT(o), MAX(o.amount))` for `status = PENDING` (`GROUP BY o.listing.id`), doing only pure aggregation with zero offer-to-offer comparison.
  - `OfferServiceImpl.populateTransientFlags` computes `hasOtherOffers = (offer.isPending() ? count - 1 : count) > 0` and `hasBetterOffers = maxAmount != null && maxAmount.compareTo(offer.getAmount()) > 0` (mathematically equivalent because `o.amount > o.amount` is always false, so a strictly higher pending offer exists iff `MAX(pending.amount) > o.amount`).

---

## Notes

- **`ON DELETE` review (untracked note).** PostgreSQL has no `ON INSERT`, and the repository contains no such statements. `ON DELETE` is inconsistent across migrations: some foreign keys specify an action (`V10`, `V21`, `V24`, `V25`, `V33`, `V34`), while others do not (`listings.creator_id`, `offers.listing_id`, `offers.buyer_id`, the `ratings` foreign keys, etc.). This is a potential data-integrity/cleanup issue, not an actionable item for this review.
