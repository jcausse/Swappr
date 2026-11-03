# Test Coverage Report - 2026-11-03

Counts are per test class; "tested" = invoked at least once (indirect execution counted). "Insufficient" = only error/empty/no-op tests, no success assertion.

## DAOs

| DAO | Tests | Tested | Untested | Insufficient |
|---|---:|---:|---|---|
| CategoryDao | 2 | 1/1 | — | — |
| FileDao | 8 | 2/2 | — | — |
| ImageDao | 11 | 3/3 | — | — |
| ListingDao | 45 | 7/8 | `updateStatusBulk` | — |
| OfferDao | 59 | 14/15 | `countByStatus` | — |
| OneTimePasswordDao | 10 | 4/4 | — | — |
| ProductDao | 12 | 5/5 | — | — |
| ProvinceDao | 0 | 0/2 | `getById`, `getAll` | — |
| RatingDao | 0 | 0/2 | `search`, `create` | — |
| SubcategoryDao | 8 | 2/2 | — | — |
| UserDao | 17 | 10/14 | `getByUsername`, `getByEmail`, `suspend`, `unsuspend` | `update`, `verifyEmail`, `updateLocation`, `updatePreferredLanguage` (only non-existing/no-op) |
| UserRoleDao | 0 | 0/2 | `addRole`, `getRoles` | — |

## Services

| Service | Tests | Tested | Untested | Insufficient |
|---|---:|---:|---|---|
| AdminService | 0 | 0/5 | `takeDownListing`, `takeDownOffer`, `grantRole`, `suspendUser`, `unsuspendUser` | — |
| ImageService | 3 | 2/3 | `delete` | — |
| ListingService | 19 | 7/10 | `updateStatus`, `updateStatusBulk`, `cancelByAdmin` | `pendingTransaction`, `cancel` (only NotFound) |
| MailingService | 0 | 0/15 | all 15 `send*` methods | — |
| OfferService | 39 | 14/19 | `getById`, `getOfferCountByStatus`, `rate(...,reviewText)`, `autoRatePendingOffers`, `cancelByAdmin` | `get`, `reject`, `withdraw`, `uploadProofOfShipping`, `confirmPayment`, `rate(...,rating)` (only throws) |
| OneTimePasswordService | 1 | 1/3 | `verify`, `clearUnused` | — |
| PasswordRecoveryService | 0 | 0/2 | `startAndSendRecoveryEmail`, `verifyAndUpdatePassword` | — |
| PermissionService | 0 | 0/7 | all 7 methods | — |
| ProductService | 7 | 6/6 | — | `findOrCreateByBrandModelYear` (only unknown-subcategory throw) |
| ProvinceService | 2 | 1/2 | `getAll` | — |
| RatingService | 0 | 0/2 | `get`, `create` | — |
| UserService | 21 | 10/16 | `update`, `updateLocation`, `addRole`, `getRoles`, `suspend`, `unsuspend` | `create` (only invalid-username throw) |
