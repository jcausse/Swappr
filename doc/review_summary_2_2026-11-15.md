# Swappr — `dev` code review, summary 2 (2026-11-15)

Condensed summary of the second review (`doc/review_2.md`, items 1–4, checked through `dee95a33`).
**Every actionable item was fixed.** The detailed per-item notes are no longer maintained; this file
keeps only what future reviews need.

## What was done

- **N+1 and queries.** The product summary no longer passes entities to a JPQL constructor
  expression: it projects `brandId` / `brandName` / `modelId` / `modelName` and groups by them, so
  one SQL statement is issued. The auto-rating query now `JOIN FETCH`es the listing. The listing
  summary builds its JPQL count and native ID filters from one shared definition (count stays JPQL,
  only the 1+1 ID query is native, with a comment to revisit).
- **State and correctness.** Listing cancel/admin-cancel now validate the state before rejecting
  offers, through `Listing` helpers and `ListingNotCancelableException`. `ListingJpaDao.update`
  returns `Optional` instead of throwing. Offer takedowns use `OfferNotCancelableException`. The
  auto-rating counter race and the stale entities returned by `rejectPendingOffers` are documented.
- **Validation.** The catalog request form restored field constraints and moved the category reload
  to a separate handler without `@Valid`; conditional brand rules stayed in the class-level
  validator.
- **User-visible bugs.** Takedown redirects keep page state and show success/error banners; a
  duplicate catalog request re-renders the form with its values; pluralization is fixed; admin model
  sort/filter preserve the category filter and category-only filtering works; the actions menu is
  only rendered when an action applies; image `304`s are answered before loading the bytes and no
  longer repeat the `ETag`.
- **Cleanup and tests.** Removed the dead `isAutoSubmit` from the admin filter forms and the unused
  enum `String` fields; applied the smaller cleanups (redirect attributes, restricted listing
  filters, trade-filter DTO moved to the service, JPQL comment); added the auto-rating job test and
  split/renamed the multi-case DAO tests.

## False positives — do not flag these again

- **Merged brand/model requests keeping one subcategory**: by design. Brand + model are unique
  regardless of subcategory, the first requester's subcategory wins, and later users are only
  attached as additional requesters.
- **Success-path tests for catalog write methods**: thin delegations to DAOs that are tested at the
  DAO level; delegation-only tests were deliberately removed earlier.
- **`ImageController` access control**: public on purpose; proof documents use authenticated
  endpoints (also listed in summary 1).
- The false positives from summary 1 still apply (clone script trust, free listings in price
  summary, unformatted max price, model move across subcategories, etc.).

## Recurring risks to re-check in future reviews

- **JPQL constructor expressions**: never pass entity instances for display DTOs; Hibernate loads
  them separately (N+1). Project scalar properties instead. Do not switch back to `Object[]`.
- **Native queries**: only the 1+1 ID queries may be native; anything else needs a very good reason.
- **N+1 / fetch joins**: don't slice a fetch-join query with `setFirstResult`/`setMaxResults`.
- **State guards**: check state before side effects (e.g. rejecting offers) and keep them in
  service/model helpers; DAOs must never throw.
- **Bulk updates**: entities loaded before a bulk update are stale in the persistence context;
  derive counters from rows actually updated or document the drift.
- **Validation**: unconditional field checks with Bean Validation, conditional rules in a
  class-level validator; category/auto-submit reloads go to a separate handler without `@Valid`.
- **Redirects**: use `RedirectHelper` and append `success`/`error` to the resolved target, even when
  a posted return URL is present.
- **i18n**: update both bundles; keep the choice-format prefix inside the branches (no doubled `+`).
- **Caching**: compute ETags from metadata and call `checkNotModified` before loading image bytes;
  don't set the same header twice.
- **Tests**: one scenario per test; no `Mockito.verify`; no assertion-less delegation tests.
- **Dead code after refactors**: remove now-unused flags, fields and getters in the same change.

## Notes for future reviews

- A previous false positive ("duplicate request redirect was deleted/moved") was stale: the code
  still existed. Re-verify old false positives against the current code instead of trusting the
  note.
- `ModelListingInfo` / `ModelListingInfoSummary` now carry flat `brandId` / `brandName` / `modelId` /
  `modelName`; do not reintroduce `Brand` / `Model` entities there.
- `ModelJpaDao`'s aggregation now groups by explicit scalar columns and is HSQLDB-compatible and
  DAO-tested, so the old functional-dependency caveat is gone.
- `AGENTS.md` now records both the no-entity-constructor rule and the scalar aggregation shape.
- The only remaining native queries are the three 1+1 ID queries; keep it that way.
