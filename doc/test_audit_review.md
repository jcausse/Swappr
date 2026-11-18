# Test Audit Review

- **Date:** 2026-11-17
- **Branch audited:** `tmp/final-checks-review-3` (local only, not pushed)
- **Remediation branches:** `chore/remove-useless-tests` (from latest `dev`, pushed) and `chore/remove-implementation-coupled-tests` (from latest `dev`, pushed).
- **Scope:** all 41 test files / 620 `@Test` methods under `model`, `persistence`, `service` and `webapp`.

Summary of findings:

| Severity | Items |
| --- | --- |
| Critical | 0 |
| High — multiple DAO/service methods per test | ✅ Resolved — dispatcher test class deleted |
| High — implementation-coupled | ✅ Resolved — 14 tests removed |
| Low — useless/redundant/misplaced | ✅ Resolved — 21 tests removed (see section 4) |
| Coverage (blatant only) | 0 (`EmailNotificationServiceImpl` untested is accepted) |

## Status tracker

| Item | Brief description | Status |
|---|---|---|
| 1.1 | [Review/rewrite or remove `NotificationDispatcherServiceImplTest` (all of §1)](#1-notificationdispatcherserviceimpltest--review-or-remove) | ✅ Resolved — file deleted |
| 2.1 | [No `Mockito.verify` anywhere](#21-no-mockitoverify-anywhere) | ✅ Resolved |
| 3.1 | [All implementation-coupled tests outside the dispatcher (all of §3)](#3-high--implementation-coupled-outside-the-dispatcher-test) | ✅ Resolved — 14 tests deleted |
| 4.1 | [All useless/duplicate/misplaced tests removed (§4)](#4-low--resolved-by-removing-the-tests) | ✅ Resolved |
| 5.1 | [`EmailNotificationServiceImpl` untested](#5-coverage) | ➖ Irrelevant |

---

## 1. `NotificationDispatcherServiceImplTest` — removed

This file was flagged for dedicated review. It contained **all** the multiple-method violations and almost all of the most implementation-coupled tests in the suite. The whole class was deleted in `chore/remove-implementation-coupled-tests`.

### 1.1 `:408` `testNotifyPurchaseConfirmedDispatchesBothWhileListingSoldIsEmailOnly`

Calls both `notifyListingSold(...)` and `notifyPurchaseConfirmed(...)` in one test.

### 1.2 `:446` `testNotifyOfferRejectedWithdrawnAndProofOfPaymentDispatchBothChannels`

Calls `notifyOfferRejected(...)`, `notifyOfferWithdrawn(...)` and `notifyProofOfPaymentUploaded(...)` in one test.

### 1.3 `:507` `testEmailOnlyNotificationTypesNeverCreateSiteNotifications`

Calls seven different dispatcher methods (`notifyWelcome`, `notifyListingPublished`, `notifyPendingTransaction`, `notifyPasswordRecovery`, `notifyEmailVerification`, `notifyAccountSuspended`, `notifyAccountUnsuspended`) in one test.

### 1.4 All 17 tests are implementation-coupled

Every test used `ArgumentCaptor` on collaborator DTOs/mail DTOs and asserted exact keys, arguments, body argument lists, action URLs and call counts (`getAllValues().size()`, `isEmpty()`). The file was maximally coupled to the internal composition of every notification and mail, so it was deleted.

---

## 2. Critical

### 2.1 No `Mockito.verify` anywhere

No occurrence of `Mockito.verify`, `.verify(`, `spy(`, `InOrder`, `verifyNoInteractions` or `verifyNoMoreInteractions` in any test. The only `.verify(` matches are `otpService.verify(...)`, which is the `OneTimePasswordService` method, not Mockito.

---

## 3. High — implementation-coupled outside the dispatcher test (all removed)

All 14 tests listed in this section were deleted in `chore/remove-implementation-coupled-tests`.

### 3.1 `ListingServiceImplTest.java:248,270,291,311` — internal `ListingFilter` captors

`testSearch_onlyFreeSearchesZeroPriceRangeIgnoringGivenRange`, `testSearch_notOnlyFreeKeepsPriceRange`, `testSearch_onlyFreeIgnoresAcceptsTrade`, `testSearch_notOnlyFreeKeepsAcceptsTrade` capture the internal filter handed to `listingDao.search` and assert its fields.

### 3.2 `ListingServiceImplTest.java:359,372,540,553` — captures `acceptsTrade` passed to DAO

`testCreate_freePriceAcceptsTrade_daoReceivesFalse`, `testCreate_paidPriceAcceptsTrade_daoReceivesTrue`, `testUpdate_freePriceAcceptsTrade_daoReceivesFalse`, `testUpdate_paidPriceAcceptsTrade_daoReceivesTrue` assert an internal DAO argument instead of an observable result.

### 3.3 `ListingServiceImplTest.java:385` — hardcoded internal alt text

`testCreateStoresOnlyNonEmptyImages` uses `argThat` to match the literal internal alt string `"Image 1 for listing: " + LISTING_TITLE`, coupling the test to the image-alt formatting implementation.

### 3.4 `ListingServiceImplTest.java:613` — `doThrow` to prove a collaborator is not called

`testUpdateIgnoresRemovedImagesFromOtherListings` installs `doThrow(...)` on `imageService.delete(eq(FOREIGN_IMAGE_ID))` so the test fails if the collaborator is invoked. This is a collaborator-call assertion, not a contract assertion.

### 3.5 `OfferServiceImplTest.java:800` — `AtomicBoolean` observes a collaborator call

`testRateByBuyerSetsSellerRatingAndIncrementsCounter` asserts via `AtomicBoolean` that `userService.incrementSellerRatingCounter(...)` was called, in addition to the observable rating result.

### 3.6 `OfferServiceImplTest.java:834,861` — captures internal aggregation maps / DAO ids

`testAutoRatePendingOffersGroupsIncrementsBySellerAndBuyer` captures the exact increment maps passed to `incrementSeller/BuyerRatingCounters`; `testAutoRatePendingOffersExcludesAlreadyRatedSeller` captures the id list passed to `setSellerRatings`. Both assert internal batching/query details.

### 3.7 `OfferServiceImplTest.java:461` — captures DAO arguments (borderline)

`testAcceptTradeRejectsOffersOnOfferedListing` observes `offerDao.rejectPendingOffers` arguments. Borderline because the rejected side effect is business behaviour, but the assertion is on a collaborator call.

### 3.8 `SiteNotificationServiceImplTest.java:172,194` — `SiteNotificationFilter` captors

`testGetRecentUnreadForUserClampsPageSizeToMax` and `...ToMin` assert the internal filter handed to the DAO. (The clamping is only observable through the DAO argument, so this is borderline.)

### 3.9 `BrandModelRequestServiceImplTest.java:297` — captures `addAdditionalRequester` args

`testCreateAttachesNewRequesterToExistingRequest` captures the request id and user id passed to the DAO instead of asserting an observable outcome.

### 3.10 `BrandModelRequestServiceImplTest.java:418,453` — captures recipient list (borderline)

`testAcceptNotifiesPrimaryAndAdditionalRequesters` and `testRejectNotifiesPrimaryAndAdditionalRequesters` capture the recipient list passed to the dispatcher. Borderline because notifications are the observable outcome, but the assertion is on the collaborator argument.

### 3.11 `CatalogServiceImplTest.java:256` — hardcoded label format

`testGetModelReplacementOptionsExcludeSelfAndLabelBrand` asserts the literal label `"Apple — MacBook Air"`, coupling to the current formatting implementation.

### 3.12 `CatalogServiceImplTest.java:469` — asserts passthrough list order (borderline)

`testGetListingInfoSummaryForModelKeepsQueryConditionOrder` only verifies that the DAO-returned order is passed through unchanged; the service adds no contract-visible behaviour.

### 3.13 `OneTimePasswordServiceImplTest.java:126` — captures encoder output (borderline)

`testCreateReturnsPlainOtpToCaller` uses an `ArgumentCaptor` on `passwordEncoder.encode(...)` to derive the plain OTP and compare it with the returned value; couples the test to the encoding call.

### 3.14 `SiteNotificationCreationDtoFactoryTest.java:120,139` — two factory methods per test

`testOfferRejectedAndWithdrawnTradeBranches` invokes `offerRejected` and `offerWithdrawn`; `testProofOfPaymentAndProofOfShippingWithAndWithoutTracking` invokes `proofOfPaymentUploaded` and `proofOfShippingUploaded`. Borderline because it is a DTO factory rather than a service interface.

---

## 4. Low — resolved by removing the tests

All useless/duplicate/misplaced tests below were removed in `chore/remove-useless-tests`. Three whole test classes were deleted, so `FileService`, `ImageService` and `ProvinceService` intentionally have no test class anymore.

- `CategoryJpaDaoTest#testGetAll` — duplicate of `testGetAllReturnsEveryCategory`.
- `ListingJpaDaoTest#testSearchReturnsOnlyCoverImage` — misnamed; asserted both image ids and never checked the cover image.
- `OfferServiceImplTest#testCanUploadDocumentsPendingPaymentFreeOfferIsFalse` and `#testCanUploadDocumentsPendingPaymentPaidOfferIsTrue` — tested `Offer` model logic in a service test.
- `OfferServiceImplTest#testRejectPendingOffersForListingReleasesOfferedListings` — delegation-only void test observing collaborators.
- `UserServiceImplTest#testUpdatePreferredLanguagePersistsChosenLanguage` and `#testUpdatePreferredLanguageReturnsUpdatedUser` — near-duplicate pass-through tests.
- `SiteNotificationJpaDaoTest#testGetMatchingEagerlyFetchesOffer` — claimed to test eager fetching but could not distinguish eager from lazy inside the transaction.
- `SiteNotificationServiceImplTest#testMarkAsReadByRecipientDelegatesToDao` and `#testDeleteByIdByRecipientDelegatesToDao` — delegation-only void tests observing DAO calls.
- `ListingCancellationServiceImplTest#testCancelByAdminPendingTransactionListingSucceeds` and `#testCancelByAdminOfferedInTradeListingSucceeds` — delegation-only void tests observing `listingDao.cancel`.
- `NotificationPreferenceServiceImplTest#testUpdatePreferencesSavesAllConfigurableTypeChannelPairsIncludingUnchecked` — captured exact `saveOrUpdate` arguments and count.
- Deleted `FileServiceImplTest` (3 tautological delegation tests).
- Deleted `ImageServiceImplTest` (3 tautological delegation tests).
- Deleted `ProvinceServiceImplTest` (2 tautological delegation tests).

---

## 5. Coverage

`EmailNotificationServiceImpl` has no test class. This is **accepted**: it sends emails through mail infrastructure that is hard to mock, and the absence of a test is not treated as a finding.

All 16 DAO implementations still have a test class. Per-method coverage gaps (e.g. `ListingDao.updateStatusBulk`, `UserNotificationPreferenceDao.getByUsersAndType`, several `AdminService`/`UserService` methods) were intentionally not counted, as requested.

---

## 6. Notes — checks that came back clean

- No assertion-less tests: every remaining `@Test` contains `assert*`, `@Test(expected = ...)` or `assertThrows`.
- DAO tests follow the one-DAO-method rule and verify writes against the database with `JdbcTestUtils`/`jdbcTemplate`.
- No test mixes two DAO classes.
- Outside `NotificationDispatcherServiceImplTest`, no test invokes more than one DAO/service method.
- Model/webapp utility tests (`SiteNotificationTest`, `UserNotificationPreferenceTest`, `RedirectHelperTest`) invoke more than one model/helper method in a test, but those are not DAO/service methods and were not counted as rule violations.
