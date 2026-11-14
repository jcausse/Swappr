# Swappr — `dev` code review, summary 1 (2026-11-14)

Condensed summary of the first full review of `dev` (items 1–12 of the old tracker, ~70 findings,
checked up to `41d08076`). **Every actionable item was fixed.** The detailed per-item descriptions
are no longer maintained; this file keeps only what future reviews need.

## What was done

- **Performance / persistence.** Removed the cover-image N+1 (cover id now comes from a formula and
  `images.data` is only read by `/image/{id}`); enforced the 1+1 pagination model with deterministic
  tie-breakers; replaced per-row write loops (`suspendUser`, offer rejection, notifications) with bulk
  updates; removed in-memory sorting and business logic from DAOs; made query result types immutable;
  dropped the unused average price; made offer transient flags opt-in.
- **Security.** Session id rotation after login, correct OEMIV/Spring Security filter order, length
  limits on free-text fields, server-side listing-year validation, log-forging sanitization, encoded
  redirect targets, and XSS-safe pagination links.
- **User-visible bugs.** PRG for password recovery, withdraw only for withdrawable offers,
  case-insensitive catalog uniqueness, admin catalog/listings/offers actions preserving page, sort and
  filters with specific errors, catalog request form state kept across category changes, escaped
  brand/model names, i18n'd subcategory labels and table columns.
- **Architecture / layering.** Broke the `@Lazy` service cycle, read-only transactions for permission
  checks, `ForbiddenException` for authorization, domain exceptions moved out of `webapp`, no public
  model method returning `null`, business rules moved from JSPs into model predicates, and categories
  fetched once per request through the interceptor.
- **Tests, i18n, mail, cleanup.** Assertion-less and forbidden (`Mockito.verify`) tests removed,
  brand/model request coverage added, missing bundle keys and pluralization fixed, email CTAs and copy
  corrected, 53 unused keys and dead code removed, style/build warnings cleaned up.

## False positives — do not flag these again

- **Clone script network exposure**: `clone_prod_db.py` needs production credentials, so whoever runs
  it is already trusted.
- **Model replace/edit moving listings across subcategories**: intended admin behavior.
- **Unformatted maximum price**: intentional; the minimum is already formatted and the currency must
  not be shown twice.
- **Duplicate-request redirect losing form fields**: the code path no longer exists.
- **Price summary including free listings**: intentional; only the listing being edited must be
  excluded.
- **`ImageController` access control**: intentionally public; proof documents use separate
  authenticated endpoints.
- **Offer proof filename getters**: kept on purpose, rendered by JSP EL.

## Recurring risks to re-check in future reviews

- **N+1 and lazy access**: views must not traverse lazy collections, and paginated fetch joins must
  never be sliced with `setFirstResult`/`setMaxResults`.
- **Pagination**: native ID query + population query + `COUNT`, always with a unique tie-breaker.
- **Layering**: query logic belongs in DAOs, decisions in services/model predicates; controllers and
  JSPs must not re-implement status or ownership rules.
- **Duplicate queries per request**: `getAllCategories()` must be read once (put `categories` in the
  model) and `getById` transient flags must stay opt-in.
- **Writes in loops inside transactions**, especially with swallowed exceptions.
- **i18n**: update both bundles, follow the apostrophe rule (double `'` only with `{n}` placeholders),
  escape `spring:message` arguments with `<spring:argument>` + `<c:out>`, never hardcode UI text.
- **URLs**: `<c:url>` + `<c:param>` everywhere, `RedirectHelper` for redirect targets, `<c:out>` for
  HTML.
- **Entity types**: primitives for `NOT NULL` columns, no boxed `Boolean`; always `@Builder.Default`
  for initialized `@Builder` fields.
- **Validation**: every free-text field needs `@Size`; hidden fields (e.g. listing year) need
  server-side validation.
- **Tests**: no `Mockito.verify`, one DAO method per test, seed fixtures with SQL/JDBC.
- **Style**: no wildcard imports, 4 spaces (no tabs).

## Notes for future reviews

- `ON DELETE` behavior is inconsistent across migrations (some FKs have an action, many do not). This
  is a data-integrity/cleanup concern, not an actionable review item yet.
- `Listing` maps images as an `@ElementCollection List<Long> imageIds` over `listing_images` (no
  `Image` association or `ListingImage` entity); covers come from the `coverImageId` formula.
- The only remaining native queries are the ID queries required by the 1+1 pagination rule
  (`SiteNotificationJpaDao`, `BrandModelRequestJpaDao`, `ModelJpaDao`); all other queries are typed
  JPQL. Keep it that way in future changes.
- Review scope was first the `dev` revision `a80c04fd`, then `a385aa63`, then `ea1b01a1`; the fixes
  landed through `41d08076`.
