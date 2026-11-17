# DAO / Hibernate Preflight Review

- **Date:** 2026-11-17
- **Branch:** `tmp/final-checks-review-3` (local only, not pushed)
- **Scope:** all 16 JPA DAO implementations in `persistence/src/main/java/ar/edu/itba/paw/persistence/`, plus the entity mappings whose fetch behaviour the DAO queries depend on.
- **Question answered:** is there *zero* chance of N+1, lazy-init, in-memory pagination, or other unexpected Hibernate behaviour, assuming entities may change later?
- **Answer:** no. Paginated methods are structurally 1+1 today (no current `HHH000104`), but several concrete N+1s and explicit rule violations remain, and several queries are only safe under current entity mappings.

Severity groups: **1 = High**, **2 = Medium**, **3 = Low**.

## Status tracker

| Item | Brief description | Status |
|---|---|---|
| 1.1 | [SiteNotification page order restored in memory](#11-sitenotificationjpadaogetmatching-rebuilds-pagination-order-in-memory) | ✅ Done |
| 1.2 | [Eager User.roles secondary selects](#12-eager-userroles-produces-a-hidden-secondary-select-per-distinct-user) | ✅ Done |
| 1.3 | [Listing search does not fetch creator](#13-listingjpadaosearch-does-not-fetch-creator-but-admin-listings-renders-it) | ✅ Done |
| 1.4 | [rejectPendingOffers notification N+1](#14-offerjpadaorejectpendingoffers--notification-loop-is-an-n1) | ✅ Done |
| 2.1 | [hasListings JPQL pagination](#21-haslistings-paginates-a-jpql-query-rule-violation) | ✅ Done |
| 2.2 | [Unfetched associations (latent risk)](#22-returnedpaginated-entities-rely-on-callers-not-touching-unfetched-associations-latent-risk) | 🚫 Won't fix |
| 2.3 | [Bulk writes bypass persistence context](#23-bulk-jpql-writes-bypass-the-persistence-context) | 🕓 Open |
| 2.4 | [JOIN FETCH nullability / native counts](#24-inner-fetch-joins-and-non-distinct-counts-depend-on-current-nullability) | 🕓 Open |
| 3.1 | [OSIV reliance](#31-osiv-reliance-across-views-and-controllers) | 🚫 Won't fix |
| 3.2 | [File/Image blobs](#32-file--image-blobs-are-loaded-eagerly--irrelevant) | ➖ Irrelevant |
| 3.3 | [@Formula overhead](#33-formula-columns-are-evaluated-on-every-listing-load--potential-performance-issue-only) | 🚫 Won't fix |
| 3.4 | [Unbounded auto-rate list](#34-offerjpadaogetacceptedunratedbefore-returns-an-unbounded-entity-list--wont-fix) | 🚫 Won't fix |
| 3.5 | [findFirst without limit](#35-findfirst-without-a-limit-relies-on-uniqueness) | 🚫 Won't fix |
| 3.6 | [updatePreferences per-pair upsert](#36-updatepreferences-issues-one-findinsert-or-update-per-preference--wont-fix) | 🚫 Won't fix |

---

## 1. High severity

### 1.1 `SiteNotificationJpaDao.getMatching` rebuilds pagination order in memory

`SiteNotificationJpaDao.java:148-160`

The ID query paginates correctly (native, ID-only), but the population query has **no `ORDER BY`** and the result is re-ordered in Java:

```java
final Map<Long, SiteNotification> byId = fetched.stream()
        .collect(Collectors.toMap(SiteNotification::getId, Function.identity()));
final List<SiteNotification> entities = ids.stream()
        .map(byId::get)
        .filter(Objects::nonNull)
        .toList();
```

`AGENTS.md` explicitly forbids restoring pagination order in memory. The population query must re-apply the same `ORDER BY created_at DESC, notification_id DESC` as the ID query.

**Risk:** any future mapping/ordering change silently desynchronises the ID page order from the rendered order; the current code masks the missing SQL ordering instead of enforcing it.

**Status:** resolved — the population query now re-applies the same `ORDER BY n.createdAt DESC, n.id DESC` as the ID query, and the in-memory reordering was removed.

### 1.2 Eager `User.roles` produces a hidden secondary SELECT per distinct user

`User.java:108-113`, no `@BatchSize`/`default_batch_fetch_size` anywhere in the project

`roles` is an `@ElementCollection(fetch = FetchType.EAGER)`. In Hibernate 5 this is not join-fetched into the owner query; it is initialised with a secondary SELECT per distinct `User`. Every DAO query that hydrates many users therefore emits one extra SELECT per user, even when the caller never reads `roles`:

- `OfferJpaDao.search` — `o.buyer` and `l.creator` for every offer (`OfferJpaDao.java:108-123`).
- `RatingJpaDao.search` — `r.creator` for every rating (`RatingJpaDao.java:81-89`).
- `BrandModelRequestJpaDao.getMatching(int, int)` — `r.requester` and every `additionalRequesters` user (`BrandModelRequestJpaDao.java:88-99`).
- `ListingJpaDao.search` on the admin listings page — each lazily loaded `creator` adds this select as well (see 1.3).

**Risk:** an N+1 hidden behind an entity mapping; it will not show up in the DAO SQL and grows with page size.

**Status:** resolved — `User.roles` is now `FetchType.LAZY`, so only the paths that actually need roles load them (`UserRoleJpaDao#getRoles`/`#addRole`, and login via `AuthUserDetailsService`). `AdminController#users` now calls `UserService#getRoles` once for the target user and derives the admin flag from that list, instead of touching the entity collection after the service transaction ends.

### 1.3 `ListingJpaDao.search` does not fetch `creator`, but admin listings renders it

`ListingJpaDao.java:205-214`, `admin/listings.jsp:70,119`, `AdminController#listings`

The population query fetch-joins model → brand → subcategory → category, but not `creator`. `AdminController#listings` uses `ListingService#search`, and the admin JSP reads `listing.creator.id` / `listing.creator.username`. That is a lazy load (plus the eager-roles select from 1.2) per listing row.

Discovery/account/profile pages happen not to touch `creator`, so this is currently visible only on the admin page. With OSIV enabled it is an N+1; without OSIV it becomes a `LazyInitializationException`.

**Status:** resolved — `ListingDao`/`ListingService` now expose `searchForAdmin` alongside `search`, both delegating to one private implementation with a `fetchCreator` intent flag; only the admin population query adds `JOIN FETCH l.creator`, so discovery/account/profile searches keep the previous query shape. `AdminController#listings` uses `searchForAdmin`.

### 1.4 `OfferJpaDao.rejectPendingOffers` + notification loop is an N+1

`OfferJpaDao.java:210-212`, `OfferServiceImpl.java:378-394`

After the bulk reject, the populating query is:

```java
return em.createQuery("FROM Offer o WHERE o.id IN :ids", Offer.class)
        .setParameter("ids", ids)
        .getResultList();
```

It fetches no associations. `rejectPendingOffersForListing` then loops over every rejected offer and calls `notifyOfferRejected`, which reaches `OfferMailDto.from(offer)`: `buyer` (different per offer), `listing.creator`, `listing.model/brand`, `offeredListing` and `offeredListing.model/brand` are initialised lazily. Per-offer buyer and offered-listing loads make this an N+1 in the cancellation/rejection path.

**Status:** resolved — the population query now `JOIN FETCH`es `buyer`, `listing` + `creator`/`model`/`brand`, `offeredListing` + `model`/`brand`, and both listings' `imageIds` (read by `ListingMailDto`), so the notification loop no longer lazy-loads per offer.

---

## 2. Medium severity

### 2.1 `hasListings` paginates a JPQL query (rule violation)

`BrandJpaDao.java:133-140`, `ModelJpaDao.java:214-221`

Both use:

```java
em.createQuery("SELECT l.id FROM Listing l WHERE ...")
  .setMaxResults(1)
```

`AGENTS.md` requires the 1+1 model for **every** paginated query, including PK-only/projection queries: native ID query plus population query. It also states the project convention is `SELECT EXISTS(...)`. Effect is one row today, but this is exactly the construct a future mapping change can turn into in-memory pagination or an N+1.

**Status:** resolved — both `hasListings` methods now use a native `SELECT EXISTS(...)` query (see implementation below).

**Decision rationale**

- Standard JPQL has no `LIMIT`/`FETCH FIRST` in the query string, and `EXISTS` is only a `WHERE`/`HAVING` predicate, not a select expression. The only JPQL short-circuit is `setMaxResults(1)`, which is exactly the banned pagination.
- `SELECT CASE WHEN COUNT(l) > 0 THEN TRUE ELSE FALSE END` (or `SELECT COUNT(l)` + Java `> 0`) is pagination-safe, but it scans/counts every matching row. PostgreSQL does not generally rewrite `COUNT(*) > 0` into an existence check.
- Native `EXISTS` gives real first-row short-circuiting, explicit joins, no pagination of any kind, and matches the documented `SELECT EXISTS` convention.

**Implementation (done)**

- `ModelJpaDao#hasListings`: native `SELECT EXISTS (SELECT 1 FROM listings l WHERE l.model_id = :modelId)`.
- `BrandJpaDao#hasListings`: native `SELECT EXISTS (SELECT 1 FROM listings l JOIN models m ON m.model_id = l.model_id WHERE m.brand_id = :brandId)` (explicit join, no hidden implicit entity-path join).
- DAO comments explain the decision: JPQL has no select-level `EXISTS`, and this avoids reintroducing JPQL pagination and its future `HHH000104` / in-memory-pagination risk.
- `AGENTS.md` now explicitly lists native `SELECT EXISTS(...)` existence checks as one of the allowed uses of native queries (in addition to the existing `SELECT EXISTS` convention note).
- Verified the Boolean scalar mapping under both PostgreSQL and the HSQLDB test dialect.

### 2.2 Returned/paginated entities rely on callers not touching unfetched associations (latent risk)

**Status:** latent risk, not an actionable item for now. These queries only become a problem if a consumer navigates an unfetched association (N+1 under OSIV) or after OSIV is removed (`LazyInitializationException`). The OSIV-dependent routes that would be affected are already enumerated in **3.1**; fix those routes with targeted fetch joins or view DTOs when OSIV is removed. Do not “join fetch everything”: for paginated queries that is impossible for collections (`HHH000104`) and for to-one associations it just over-fetches and couples the DAO to every view. The one currently exercised case (`ListingJpaDao.search` + admin listings reading `creator`) is already tracked in 1.3.

Current consumers happen to use only the fetched fields, but any view/service change navigating these becomes an N+1 (or an OSIV-only lazy load):

- `ListingJpaDao.search` never fetches `imageIds` (lazy element collection); only `coverImageId` is used today.
- `RatingJpaDao.search` fetches `creator`, `offer`, `offer.listing`, but not `rated`.
- `SubcategoryJpaDao.getByCategoryId` returns `Subcategory` without `category`.
- `ModelJpaDao.getBySubcategoryIdAndBrandId` returns `Model` without `brand`/`subcategory`.
- `ModelJpaDao.getAllWithBrand` fetches `brand` but not `subcategory`.
- `OfferJpaDao.search` fetches `offeredListing.model/brand` but not `offeredListing.subcategory`.
- `OfferJpaDao.getById` and `getByListingAndBuyer` fetch no associations at all.
- `ListingJpaDao.getById` fetches nothing (see also 3.1).

### 2.3 Bulk JPQL writes bypass the persistence context

`ListingJpaDao` release/cancel/replace/`updateStatusBulk`; `OfferJpaDao` reject/rating bulk methods; `ModelJpaDao.replaceBrand`; `ImageJpaDao.deleteAll`; `OneTimePasswordJpaDao.deleteOlderThan`; `UserJpaDao.incrementRatingCounters`.

Managed entities loaded earlier in the same transaction are not refreshed by these updates. `OfferJpaDao.rejectPendingOffers` already documents the fragile contract (“use them only for immutable data, never read the status”). A future caller that loads one of these rows before the bulk write and reads it afterwards gets stale state.

**Status:** documented fragility, **no actual stale-data bug found** in the current call sites. Every bulk write is either terminal (nothing reads the affected entities afterwards), runs before the affected entities are loaded, or reloads them after the write. `OfferServiceImpl#autoRatePendingOffers` is the closest latent case (the loaded offers keep `null` ratings after the bulk rating updates), but nothing reads them again in that transaction.

**Action plan (documentation only, do not implement now):** document the invariant that entities affected by a bulk write must be re-fetched (or explicitly `refresh`ed/`detach`ed) before being accessed later in the same transaction. Existing bulk methods that return entities should keep/receive a comment stating that the returned instances may be stale. No blanket `em.clear()` and no behavioural change.

### 2.4 Inner fetch joins and non-`DISTINCT` counts depend on current nullability

- **Inner `JOIN FETCH` nullability — no action.** `ListingJpaDao.search` / `OfferJpaDao.search` / `RatingJpaDao.search` use inner `JOIN FETCH` for associations that are non-null today. If any becomes optional, rows silently disappear from the page even though the ID query selected them. JPQL/HQL has no way to make an inner fetch join fail on null; a null FK is simply filtered. The NOT NULL constraints plus `optional = false` are relied upon and assumed to stay.
- **Non-`DISTINCT` counts — action plan (do not implement yet).** `ListingJpaDao.search` and `OfferJpaDao.search` count with `COUNT(l)` / `COUNT(o)` while filtering through implicit joins. If any filtered path ever becomes a collection, the count will over-count. Decision: switch **all paginated fetch count queries** to native SQL for consistency, not only these two.

  Methods to convert:

  - `ListingJpaDao.search`
  - `OfferJpaDao.search`
  - `RatingJpaDao.search`
  - `SiteNotificationJpaDao.getMatching`
  - `BrandJpaDao.getWithStats`
  - `ModelJpaDao.getWithInfo`
  - `ModelJpaDao.getListingInfoSummary`
  - `BrandModelRequestJpaDao.getMatching`

  Requirements for each native count:

  - build it from the **same `WHERE`/parameter definitions** as that method's native ID query, so count and page filters cannot drift;
  - make every join explicit;
  - use `COUNT(*)` while the joins are all to-one;
  - if a collection join is ever introduced, switch to `COUNT(DISTINCT <alias>.id)` (or an `EXISTS` subquery) to avoid over-counting;
  - verify the native enums/booleans under both PostgreSQL and the HSQLDB test dialect.

---

## 3. Low severity

### 3.1 OSIV reliance across views and controllers

`web.xml:91-98` (`OpenEntityManagerInViewFilter` mapped to `/*`, marked “remove before deploy”).

Lazy associations are rendered directly from detached service results:

- `ListingController#listing` uses bare `ListingJpaDao.getById`; `listing/index.jsp` reads `listing.imageIds`, `listing.creator.province`, `listing.model`.
- `ListingController.java:329,364` call `listing.getImageIds()` outside the service transaction.
- Checkout reads `listing.imageIds` / `listing.creator`.
- Profile reads `user.province`.
- Offer detail reads `offer.listing`, `offer.buyer`, `offer.offeredListing` (metadata is prefetched in batch, but the associations are not).
- Admin pages rely on the same lazy loads (and 1.3).

Removing OSIV (the stated intent in `web.xml`) will cause `LazyInitializationException` unless these are fetched explicitly.

### 3.2 `File` / `Image` blobs are loaded eagerly — irrelevant

`FileJpaDao.getById`, `ImageJpaDao.getById`; `File.java` / `Image.java` `byte[] data` has no lazy mapping.

**Status:** irrelevant, no action. There are no production callers that need metadata only from `getById`: all of them need the bytes (`AccountController` proof downloads via `OfferServiceImpl#getProofOfPaymentFile` / `#getProofOfShippingFile`, and `ImageController#getImage`). The metadata-only path already uses the scalar `getMetadataByIds` projection. If metadata-only retrieval is ever needed, add a separate DAO method for it.

### 3.3 `@Formula` columns are evaluated on every Listing load — potential performance issue only

`Listing.java:97-104`: `coverImageId`, `pendingOffersCount`, `recentPendingOfferAt` are three correlated subqueries in every `Listing` select. They are **not** an N+1: there is still one SQL statement, with the subqueries inlined in its select list and evaluated per returned row. `coverImageId` is covered by the unique `listing_images(listing_id, display_order)` index. `pendingOffersCount` and `recentPendingOfferAt` have **no supporting index on `offers`**, so they can force an offers scan per listing row; offer searches that also fetch `offeredListing` pay this twice per row. `recentPendingOfferAt` is only needed for `RECENT_OFFERS` sorting but is computed on every load. The last two are also duplicated again in the native `ORDER BY` for `MOST_OFFERS` / `RECENT_OFFERS`.

**Status:** potential performance issue only; no correctness or N+1 problem.

**Suggested action:** add the missing index on `offers` supporting both formulas, e.g. `(listing_id, status, created_at)` (covers `COUNT(*)` with `listing_id`/`status` and `MAX(created_at)` for `RECENT_OFFERS`), via a new incremental migration plus the matching HSQLDB test migration.

### 3.4 `OfferJpaDao.getAcceptedUnratedBefore` returns an unbounded entity list — won't fix

`OfferJpaDao.java:339-346`

The scheduled auto-rating job loads every accepted, unrated offer older than the cutoff. Only `listing` is fetch-joined; buyer/creator IDs are read through proxies (safe), but the result set itself is unbounded and can grow with site volume.

**Status:** won't fix. This behaviour predates the first deadline and was not graded as an issue. The job runs hourly and, under normal operation, processes the acceptances crossing the 14-day cutoff; a material backlog requires a job failure or application downtime.

### 3.5 `findFirst` without a limit relies on uniqueness

`UserJpaDao.getByUsername`/`getByEmail`, `BrandJpaDao.getByName`, `ModelJpaDao.getByNameAndBrandId`, `BrandModelRequestJpaDao.findMatching`, `UserNotificationPreferenceJpaDao.getByUserTypeAndChannel`, `OfferJpaDao.getByListingAndBuyer` all call `getResultStream`/`getResultList().stream().findFirst()` without `setMaxResults(1)`. Safe while the uniqueness constraints hold; if duplicates ever appear, every matching row is loaded.

**Why not `getSingleResult()`:** it throws `NoResultException` for a normal “not found” (these methods return `Optional`) and `NonUniqueResultException` for queries that intentionally select the first ordered row (`findMatching`, `getByListingAndBuyer`, and the case-insensitive lookups). Using it would make absence/multiple results exception-driven control flow, not an improvement.

**Why not just add `setMaxResults(1)`:** the project rule forbids JPQL pagination even for PK-only/projection queries. Separately, if a future entity change adds a collection fetch to one of these queries, `setMaxResults(1)` would trigger HHH000104 and in-memory pagination. Implicit **to-one** joins do not cause that, but the strict rule makes this unacceptable anyway.

**Compliant options:**

- leave `findFirst()` without a limit — no pagination request, so no `HHH000104` risk (may load more than one row for the multi-match queries);
- native ID query with `FETCH FIRST 1 ROW ONLY`/`LIMIT 1` plus `em.find`/population, which is the strict 1+1 pattern;
- for `UserNotificationPreferenceDao.getByUserTypeAndChannel`, use `em.find` with the composite ID — no query and no pagination at all.

### 3.6 `updatePreferences` issues one find/insert-or-update per preference — won't fix

`NotificationPreferenceServiceImpl.updatePreferences` loops over every configurable type × supported channel and calls `UserNotificationPreferenceDao.saveOrUpdate`, which does an `em.find` plus insert/update per pair. Not an N+1 over a query result, but ~2 statements per checkbox pair on every save.

**Status:** won't fix. The loop is a fixed 21 pairs (9 two-channel types + 3 one-channel types), it runs only on an explicit user save, and it is not an N+1. The extra statements are negligible; the potential one-SELECT-plus-map cleanup is not worth the added DAO surface.

---

## What is currently clean

- Every paginated method pages a **native, ID-only** query (`Listing`, `Offer`, `Rating`, `SiteNotification`, `Brand`, `Model`, `BrandModelRequest`). No JPQL entity pagination and no current `HHH000104` / in-memory pagination in those methods.
- All list/aggregate display queries project scalar columns (DTO constructors), not entity instances: `OfferPendingCount`, `OfferStatusCount`, `ModelInfo`, `ModelListingInfo`, `BrandInfo`, `FileMetadata`.
- Offer and rating list queries fetch the associations their current views use; notification cards and brand-model request cards fetch their lazy collections.
- The 1+1 population queries that join a collection (`SiteNotification.bodyArgs`, `BrandModelRequest.additionalRequesters`) are **not** paginated, so they cannot trigger `HHH000104` as written.
