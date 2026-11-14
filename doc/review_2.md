# Swappr — code review of `dev` (second pass)

Issues still open on `dev`, checked against the code after every item of the first review was merged.
Cross-referenced against `doc/review_summary_1_2026-11-14.md` (the fixes and decisions of the first
review).

- **Measured** means it was run against the dev database in Jetty. Everything else was confirmed by reading the code.
- **(graded)** marks a pattern the cátedra penalized in TP1 or answered in `doc/CONSULTAS.md`.
- Paths are shortened: `service/…` means `service/src/main/java/ar/edu/itba/paw/service/…`, and so on.

## Status tracker

| Item | Description | Status |
| --- | --- | --- |
| 1.1 | [Product summary page runs one query per model (N+1)](#11-product-summary-page-runs-one-query-per-model-n1) | 🕓 todo |
| 1.2 | [Canceling a listing does not check its state](#12-canceling-a-listing-does-not-check-its-state) | 🕓 todo |
| 1.3 | [Auto-rating job loads each listing separately (N+1)](#13-auto-rating-job-loads-each-listing-separately-n1) | 🕓 todo |
| 1.4 | [`ListingJpaDao.update` throws](#14-listingjpadaoupdate-throws) | ✅ done |
| 1.5 | [Offer takedown still catches `BadParameterException`](#15-offer-takedown-still-catches-badparameterexception) | 🕓 todo |
| 1.6 | [Catalog request form validates by hand](#16-catalog-request-form-validates-by-hand) | 🕓 todo |
| 2.1 | [Listing/offer takedowns lose the success and error banners](#21-listingoffer-takedowns-lose-the-success-and-error-banners) | 🕓 todo |
| 2.2 | [Duplicate brand/model request empties the form](#22-duplicate-brandmodel-request-empties-the-form) | 🕓 todo |
| 2.3 | [Merged brand/model requests lose the second subcategory](#23-merged-brandmodel-requests-lose-the-second-subcategory) | ❌ false positive |
| 2.4 | [Request count pluralization prints `+` twice](#24-request-count-pluralization-prints--twice) | 🕓 todo |
| 2.5 | [Admin model sort links drop the category filter](#25-admin-model-sort-links-drop-the-category-filter) | 🕓 todo |
| 2.6 | [Selecting only a category does not filter admin models](#26-selecting-only-a-category-does-not-filter-admin-models) | 🕓 todo |
| 2.7 | [Empty actions menu for free pending-payment offers](#27-empty-actions-menu-for-free-pending-payment-offers) | 🕓 todo |
| 2.8 | [Image `304` loads the image bytes and repeats `ETag`](#28-image-304-loads-the-image-bytes-and-repeats-etag) | 🕓 todo |
| 3.1 | [Listing summary count and ID query duplicate their filters](#31-listing-summary-count-and-id-query-duplicate-their-filters) | 🕓 todo |
| 3.2 | [Dead `isAutoSubmit` in the admin catalog filter forms](#32-dead-isautosubmit-in-the-admin-catalog-filter-forms) | 🕓 todo |
| 3.3 | [Unused `String` fields in five enums](#33-unused-string-fields-in-five-enums) | 🕓 todo |
| 3.4 | [Auto-rating counters can drift from the ratings written](#34-auto-rating-counters-can-drift-from-the-ratings-written) | 🕓 todo |
| 3.5 | [`rejectPendingOffers` can return stale entities](#35-rejectpendingoffers-can-return-stale-entities) | 🕓 todo |
| 3.6 | [Smaller cleanups](#36-smaller-cleanups) | 🕓 todo |
| 4.1 | [`autoRatePendingOffers` has no test](#41-autoratependingoffers-has-no-test) | 🕓 todo |
| 4.2 | [Catalog write methods have no success-path tests](#42-catalog-write-methods-have-no-success-path-tests) | ❌ false positive |
| 4.3 | [Misnamed and multi-case DAO tests](#43-misnamed-and-multi-case-dao-tests) | 🕓 todo |

No item in this review is already resolved on current `dev`. Two are false positives, two conflict
with deliberate decisions of the earlier review, and several are regressions, oversights or
follow-ups left by earlier fixes (see the notes on each item).

---

## 1. Highest priority

### 1.1 Product summary page runs one query per model (N+1)

- (graded) Measured. `ModelJpaDao.getListingInfoSummary` (`persistence/…/ModelJpaDao.java:263`) uses `SELECT new ar.edu.itba.paw.model.ModelListingInfo(b, m, …)`. With entity arguments in a constructor expression, Hibernate 5 selects only `brand_id` / `model_id` and then loads each `Model` with its own `select … from models where model_id=?`.
- The first page of `/listing/by-product?categoryId=1&subcategoryId=1` runs 16 queries; with `SELECT b, m, …` and `Object[]` it ran 6. The HTML is the same, so it only shows in the SQL log.
- **Cross-reference:** regression introduced by the earlier fix that replaced the `Object[]` row mapping with an entity constructor projection to remove the casting; that change reintroduced the N+1. Reverting to `Object[]` (or ids + `em.find`) is acceptable here; keep the immutability introduced there.
- `getListingInfoSummaryForModel` (`:311`) is not affected: its model is already loaded by `em.find`.
- Fix: select the entities as `Object[]` columns (one query, full entities) or select ids and map onto entities loaded once.
- Minor, same class: `ModelListingInfo` has two 7-argument constructors with `listingCount` and `condition` in different order (`model/…/ModelListingInfo.java:18` for the builder, `:34` for JPQL). Easy to call the wrong one. Also introduced with the earlier constructor-projection change.

### 1.2 Canceling a listing does not check its state

- (graded: state-machine guards live in the service) `ListingCancellationServiceImpl.cancel` (`service/…/ListingCancellationServiceImpl.java:24-34`) and `cancelByAdmin` (`:40-49`) reject the pending offers and call `ListingDao.cancel`, which sets `CANCELED` unconditionally.
- The UI hides the action for sold/canceled listings, but a crafted POST to `/admin/listings/takedown` or `/admin/listings/{id}/takedown` cancels a `SOLD` listing. The creator path has the same gap: `PermissionServiceImpl.canManageListing` (`:19-22`) only checks ownership and `!isCanceled()`, so the creator can cancel a `SOLD` or `PENDING_TRANSACTION` listing.
- Fix: throw a domain exception before touching the offers when `!listing.canBeCanceledByAdmin()` (admin) or `!listing.isActive()` (creator). Add service tests for both.

### 1.3 Auto-rating job loads each listing separately (N+1)

- (graded) `OfferJpaDao.getAcceptedUnratedBefore` (`persistence/…/OfferJpaDao.java:287-294`) does not fetch the listing, and `Offer.listing` is `LAZY`. `OfferServiceImpl.autoRatePendingOffers` (`service/…/OfferServiceImpl.java:593`) groups by `offer.getListing().getCreator().getId()`, which initializes each listing proxy: one `SELECT` per distinct listing. (`offer.getBuyer().getId()` is fine: reading a proxy id does not load it.)
- **Cross-reference:** follow-up of the earlier rewrite of this job into bulk updates, which left the per-listing group-by read untouched.
- Fix: `JOIN FETCH o.listing` in `getAcceptedUnratedBefore`.

### 1.4 `ListingJpaDao.update` throws

- (graded: cátedra, "never throw from the DAO") `ListingJpaDao.update` (`persistence/…/ListingJpaDao.java:309`) throws `NoSuchElementException` when the listing does not exist. It is the only `throw` in `persistence/src/main`.
- Unreachable today (`ListingServiceImpl.update` loads the listing first), but if another caller used it, a framework exception would reach the web layer as a 500.
- Fix: return `Optional<Listing>` (or `boolean`) like the other DAO writes and let the service throw.

### 1.5 Offer takedown still catches `BadParameterException`

- (graded: cátedra, catch business exceptions, not form ones) The catalog handlers now catch only specific exceptions, but both offer takedowns still catch the generic one: `AdminController.java:540` (`takeDownOfferByForm`) and `:571` (`takeDownOfferById`), for `OfferServiceImpl.cancelByAdmin`'s "Offer cannot be canceled in its current state" (`service/…/OfferServiceImpl.java:622`).
- That is a state error, not a parameter error. Fix: an `OfferNotCancelableException` (like `CatalogEntityInUseException`) and catch that.
- `AGENTS.md:107` still gives `BadParameterException` as an example of an exception a controller may catch. Change the example to a specific one so the rule matches.

### 1.6 Catalog request form validates by hand

- (graded: validation is Bean Validation, never hand-rolled ifs) `BrandModelRequestForm` has no field constraints and an `isAutoSubmit` flag (`webapp/…/form/BrandModelRequestForm.java:23`); `BrandModelRequestValidator` re-implements required and length checks (`webapp/…/form/validation/BrandModelRequestValidator.java:23-46`, e.g. `typeRequired`, `modelTooLong`) so that the category reload can skip them.
- **Cross-reference:** conflicts with the earlier deliberate decision to move validation into the class-level validator, because `newBrandName` vs `brandId` is conditional and cannot be expressed with per-field constraints (and because category reloads must bypass validation). Do not simply restore per-field constraints on the form: the conditional brand/model rules must stay in a class-level validator, per `AGENTS.md`.
- Actionable parts: replace the `isAutoSubmit` bypass with a separate category-reload handler that does not run `@Valid` (e.g. `@PostMapping(params = "categoryChanged")`), and then restore the unconditional field constraints (`@NotNull` / `@NotBlank` / `@Size` + `NotNull.requestForm.*` / `Size.requestForm.*`) that do not depend on the request type. Merge `BrandModelRequestAcceptValidator` / `@ValidBrandModelRequestAccept` into `BrandModelRequestValidator` / `@ValidBrandModelRequest` (the brand rule is already shared in `BrandModelRequestBrandRule`) only if the conditional rules stay class-level.
- `doc/review_summary_1_2026-11-14.md` describes the earlier validation decision; update it with the final shape.

---

## 2. User-visible bugs

### 2.1 Listing/offer takedowns lose the success and error banners

- The catalog handlers resolve the posted return URL and add the code with `RedirectHelper.appendQueryParam(target, key, code)` (`webapp/…/controller/AdminController.java:191-192`). The four takedown handlers do not: `resolvePath(redirectUrl, fallback, ADMIN_PATH_FILTER)` at `:460` and `:483` (listings) and `:549` and `:575` (offers), with `success=` / `error=` only in `fallback`.
- Every takedown form posts `listingsReturnUrl` / `offersReturnUrl` (`jsp/admin/listings.jsp:21`, `:131`; `jsp/admin/offers.jsp:21`, `:153`), so the fallback is never used: the page state is kept but the success banner and the "not found" / "cannot be canceled" errors never show.
- **Cross-reference:** regression from the earlier server-side redirect work, which moved these URLs into the handlers but only added the query params to the catalog handlers.
- Fix: `RedirectHelper.appendQueryParam(target, "success" | "error", code)` in the four handlers.

### 2.2 Duplicate brand/model request empties the form

- `BrandModelRequestController.submit`: on `BrandModelRequestAlreadyExistsException` it redirects to `/catalog-requests/new?duplicate=1` (`webapp/…/controller/BrandModelRequestController.java:85`). Everything the user typed is lost, and so are the `categoryId` / `subcategoryId` / `brandId` they came with from the listing wizard.
- **Cross-reference:** the earlier review marked this a false positive with the note "no longer relevant, the code has been deleted/moved". That was wrong/stale: the redirect still exists in `BrandModelRequestController.submit`, so the old false-positive note must be dropped. Future reviews should re-check closed items rather than trust the "moved" rationale.
- Fix: `errors.reject("error.catalogRequest.duplicate")` and `return render(form)`, and drop the `duplicate` request param (`:44`, `:48-49`).

### 2.3 Merged brand/model requests lose the second subcategory

- **Cross-reference: false positive.** Expected behavior, already settled in the earlier review: brand + model are unique regardless of subcategory, the first requester's subcategory wins, later users are only attached as additional requesters, and the admin does not pick a subcategory per requester. Do not add a subcategory to the acceptance DTO.
- A request for an existing brand/model from another subcategory is attached as an additional requester, but the request keeps one subcategory. `BrandModelRequestServiceImpl.accept` creates the model with `request.getSubcategory().getId()` (`service/…/BrandModelRequestServiceImpl.java:131`), and `BrandModelRequestAcceptanceDto` has no subcategory. This is by design.

### 2.4 Request count pluralization prints `+` twice

- `messages.properties:1143`: `+ {0,choice,1#+ 1 other|1<+ {0} others}`: the `+ ` is outside and inside the choice, so it renders "+ + 3 others". `messages_es.properties:1143` has the same problem ("+ + 3 más"), and both of its branches are identical.
- **Cross-reference:** regression introduced by the earlier pluralization fix, which added the choice format but left the prefix outside it.
- Fix: `{0,choice,1#+ 1 other|1<+ {0} others}` in English and `+ {0} más` in Spanish.

### 2.5 Admin model sort links drop the category filter

- `modelIdSortUrl` (`jsp/admin/catalog.jsp:365`) and `modelNameSortUrl` (`:378`) carry `filterBrandId` and `filterSubcategoryId` but not `filterCategoryId`. After sorting, the subcategory filter is still applied, but the category select resets to "All" and the subcategory select is disabled with no options. The page shows an active filter that is not visible.
- Fix: add `<c:param name="filterCategoryId">` to both links.

### 2.6 Selecting only a category does not filter admin models

- `filterCategoryId` only feeds the subcategory options (`AdminController.java:137`); `catalogService.getModels` receives `filterBrandId` and `filterSubcategoryId` (`:118-120`). Choosing a category and pressing "Apply" lists every model.
- Fix: filter by category in the DAO, or make it clear in the UI that the category select only narrows the subcategory list.

### 2.7 Empty actions menu for free pending-payment offers

- In the "pending payment" tab of `jsp/account/myOffers.jsp`, the menu (`:40-52`) shows "upload proof" only for paid listings and "withdraw" only for pending offers. For a pending-payment offer on a free listing neither applies and the `⋯` button opens an empty menu.
- **Cross-reference:** side effect of the earlier fix that correctly hid "withdraw" for non-pending offers but left the free/no-proof case without any action.
- Fix: render the menu only when at least one action applies.

### 2.8 Image `304` loads the image bytes and repeats `ETag`

- (graded: compute image ETags from metadata) Measured. `ImageController.getImage` loads the image (`webapp/…/controller/ImageController.java:33`) before `checkNotModified` (`:40`), so a `304` still runs `select … image0_.data …`. The ETag is only the id, so the check can run before the lookup.
- The `304` response carries two `ETag` headers: `checkNotModified` sets it and the response adds it again (`:42`). Return `null` (or a `304` without `.eTag`) after `checkNotModified` returns `true`.
- **Cross-reference:** oversight in the earlier image caching change, which added the id-based ETag and caching but left the lookup before the check.
- **Access control is a known false positive:** `permitAll()` and image-id enumeration are deliberate (only public listing images and profile pictures are served here; proof files use authenticated endpoints). Do not flag it again; only the caching/query issue is open.

---

## 3. Code quality

### 3.1 Listing summary count and ID query duplicate their filters

- In `ModelJpaDao.getListingInfoSummary`, the count is JPQL with its own `countWhere` (`persistence/…/ModelJpaDao.java:219-228`) and the ID query is native with the original `where` (`:195-217`, query at `:241`). The same filters are written twice in two languages; a filter added to one silently breaks the page count.
- **Cross-reference / correction:** do **not** fix this by moving the count to native SQL. We always prefer JPQL and need a *very good reason* to use a native query. The count is already JPQL (correct); the native ID query is the only mandated exception (1+1 pagination rule in `AGENTS.md`), and the earlier review notes that the only remaining native queries are these ID queries. Keep it that way.
- Fix: deduplicate the filter definition in one place (a shared predicate/parameter builder, or generate both from the same source) while keeping the count in JPQL and only the ID query native. Document why the ID query must stay native.

### 3.2 Dead `isAutoSubmit` in the admin catalog filter forms

- `AdminModelFilterForm.isAutoSubmit` (`webapp/…/form/AdminModelFilterForm.java:15`) and `AdminCreateModelFilterForm.isAutoSubmit` (`AdminCreateModelFilterForm.java:13`) are rendered as hidden fields (`jsp/admin/catalog.jsp:319`, `:488`) and set by the JS (`:837`, `:849`). The controller only resets them (`AdminController.java:115-116`) and never reads them; both are GET forms that just reload the page.
- Fix: remove the field, the hidden inputs, the JS lines and the setter calls. Without it, `AdminCreateModelFilterForm` has a single field and can become a `@RequestParam Long createCategoryId`.

### 3.3 Unused `String` fields in five enums

- `Condition`, `ListingStatus`, `OfferStatus`, `OfferRating` and `RatingRole` store a `String` equal to the constant name (`ACTIVE("ACTIVE")`) with `@Getter`. Their only reader was the removed `fromString`. No call to the getters in the compiled code (tests included), JSPs, tags, mail templates or SpEL; the database stores `name()`.
- **Cross-reference:** leftover from the earlier dead-code cleanup, which removed the only reader (`fromString`) but not the now-unused fields.
- Fix: make them plain enums, keeping names and order (`Condition` order is used by the `ORDER BY CASE`). `ListingSort.key`, `OfferStatusGroup.status` and `Role.roleName` are used and stay.

### 3.4 Auto-rating counters can drift from the ratings written

- `autoRatePendingOffers` computes the counter increments from the list loaded at the start (`service/…/OfferServiceImpl.java:586-603`) and ignores the row counts returned by `setSellerRatings` / `setBuyerRatings` (`:605-608`). If a user rates manually between the read and the bulk `UPDATE`, the `IS NULL` guard skips the offer but the counter still gets `+1`.
- **Cross-reference:** follow-up of the earlier bulk auto-rating rewrite. Low severity (scheduled job, small window).
- At least document it; or derive the increments from the rows actually updated.
- Nit in the same method: `pending` is streamed four times with each filter written twice; filter once per side.

### 3.5 `rejectPendingOffers` can return stale entities

- `OfferJpaDao.rejectPendingOffers` (`persistence/…/OfferJpaDao.java:148`) runs a bulk `UPDATE` and then re-reads the offers. Bulk updates do not refresh entities already in the persistence context, so an offer loaded earlier in the same transaction would come back `PENDING` in memory.
- **Cross-reference:** introduced by the earlier bulk-update conversion of this method.
- Not triggered by the current callers. Add a comment on the method so nobody calls it after loading those offers as entities.

### 3.6 Smaller cleanups

- `PasswordRecoveryController.passwordRecoveryRequestPOST` builds the redirect query with `URLEncoder` (`webapp/…/controller/PasswordRecoveryController.java:66`); use `RedirectAttributes.addAttribute`.
- `@ValidBrandModelRequest` and `@ValidBrandModelRequestAccept` default to `{typeMismatch}` (`webapp/…/form/validation/ValidBrandModelRequest.java:16`, `ValidBrandModelRequestAccept.java:16`). Never shown, but an unrelated key as default is misleading.
  - **Cross-reference:** this default was chosen deliberately earlier to avoid adding dead bundle keys; the constraints always disable the default. Leave it unless the validators are restructured to actually use the default.
- `AccountController.listings` passes every `ListingFilterForm` field (`webapp/…/controller/AccountController.java:71`), so crafted params like `?minPrice=` filter "my listings". No UI for them.
- `CheckoutForm.toTradeListingsFilterDto` (`webapp/…/form/CheckoutForm.java:60`) keeps the rule "tradeable listings are your own active ones" in a web form; it reads better as a service method.
- `ListingJpaDao.replaceSameNameModelsForBrands` uses a subquery in `SET`; Hibernate accepts it, standard JPQL does not. Worth a comment (it is JPQL, not native; the only native queries are the ID queries).

---

## 4. Tests

### 4.1 `autoRatePendingOffers` has no test

- `OfferServiceImplTest` has no test for `autoRatePendingOffers`, so the grouping/counting logic (`service/…/OfferServiceImpl.java:586-603`) is untested.
- **Cross-reference:** coverage gap left by the earlier rewrite of this method.
- Without `verify`: stub `getAcceptedUnratedBefore` with two unrated offers from the same seller, capture the map passed to `incrementSellerRatingCounters` with `doAnswer`, and assert `{sellerId: 2}`. Add a case where an offer is already rated and excluded.

### 4.2 Catalog write methods have no success-path tests

- **Cross-reference: false positive.** These methods are thin delegations to DAOs that are already tested at the DAO level. The earlier review deliberately removed the no-assertion delegation tests because they only restated the mock; re-adding success-path tests here would repeat that mistake. Do not implement.
- `replaceBrand`, `renameBrand`, `deleteBrand`, `updateModel`, `deleteModel` and `replaceModel` are `void` and only their error paths are tested; `updateModel` has no test at all (`service/…/CatalogServiceImplTest.java`). This is acceptable for pure delegation.

### 4.3 Misnamed and multi-case DAO tests

- `BrandModelRequestJpaDaoTest.testFindMatchingIgnoresSubcategory` (`persistence/src/test/…/BrandModelRequestJpaDaoTest.java:218`) does what `testFindMatchingExistingBrandByName` does: all fixtures share one subcategory and `findMatching` takes none. Rename or remove it; the behavior it names belongs in a service test.
- `BrandJpaDaoTest.testExistsByNameAndIdNotIsCaseInsensitiveButExcludesSameBrand` (`:112`) and `ModelJpaDaoTest.testExistsByNameAndBrandIdIsCaseInsensitive` (`:223`) / `…AndIdNotIsCaseInsensitiveButExcludesSameModel` (`:229`) check two scenarios each. One test, one thing: split them.
