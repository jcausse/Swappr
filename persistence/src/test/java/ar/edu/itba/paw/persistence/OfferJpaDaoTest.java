package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.model.Category;
import ar.edu.itba.paw.model.Condition;
import ar.edu.itba.paw.model.File;
import ar.edu.itba.paw.model.Listing;
import ar.edu.itba.paw.model.ListingStatus;
import ar.edu.itba.paw.model.Offer;
import ar.edu.itba.paw.model.OfferFilter;
import ar.edu.itba.paw.model.OfferRating;
import ar.edu.itba.paw.model.OfferStatus;
import ar.edu.itba.paw.model.Page;
import ar.edu.itba.paw.model.Price;
import ar.edu.itba.paw.model.Product;
import ar.edu.itba.paw.model.Subcategory;
import ar.edu.itba.paw.model.User;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.jdbc.JdbcTestUtils;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.sql.DataSource;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { TestConfig.class })
@Sql({ "classpath:sql/listing-data.sql", "classpath:sql/offer-data.sql" })
@Transactional
@Rollback
public class OfferJpaDaoTest {

    // Fixture ids (see sql/listing-data.sql and sql/offer-data.sql)
    private static final long LOW_OFFER_ID = 1L;              // listing 1, buyer 2, 1400, pending
    private static final long FULL_PRICE_OFFER_ID = 2L;       // listing 1, buyer 2, 1500, full price, pending
    private static final long GAMING_OFFER_ID = 3L;           // listing 4, buyer 1, 1900, pending
    private static final long REJECTED_OFFER_ID = 4L;         // listing 4, buyer 1, 1000, rejected
    private static final long ACCEPTED_OFFER_ID = 5L;         // listing 3 (SOLD), buyer 1, both proofs, buyer rated
    private static final long TRADE_OFFER_ID = 6L;            // listing 4, buyer 1, pending_payment, offers listing 1
    private static final long RATED_OFFER_ID = 7L;            // listing 3, accepted, rated by both
    private static final long RECENT_ACCEPTED_OFFER_ID = 8L;  // listing 3, accepted on 2026-03-01, unrated
    private static final long NON_EXISTING_ID = 9000L;
    private static final int TOTAL_OFFERS = 8;

    private static final long MACBOOK_LISTING_ID = 1L;
    private static final long GALAXY_LISTING_ID = 2L;
    private static final long OLD_MACBOOK_LISTING_ID = 3L;
    private static final long GAMING_LISTING_ID = 4L;

    private static final long SELLER_ID = 1L;  // fake_user: seller of listings 1-2, buyer of offers 3-8
    private static final long BUYER_ID = 2L;   // buyer_user: buyer of offers 1-2, seller of listings 3-4
    private static final long PAYMENT_FILE_ID = 1L;
    private static final long SHIPPING_FILE_ID = 2L;
    private static final long FRONT_IMAGE_ID = 1L;
    private static final long BACK_IMAGE_ID = 2L;

    private static final Instant NEW_OFFER_CREATED_AT = Instant.parse("2026-02-01T12:00:00Z");

    private static final int PAGE_SIZE = 10;

    @Autowired
    private OfferDao offerDao;

    @Autowired
    private DataSource dataSource;

    private JdbcTemplate jdbcTemplate;

    @PersistenceContext
    private EntityManager em;

    @Before
    public void setUp() {
        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    /* ---------------------------------------------------------------------------------------------- */
    /* Fixtures & helpers                                                                              */
    /* ---------------------------------------------------------------------------------------------- */

    private static User buildFakeUser(final long id) {
        return User.builder()
            .id(id)
            .username("user_" + id)
            .displayName("User " + id)
            .email("user_" + id + "@example.com")
            .password("fake_password")
            .joinedAt(Instant.now())
            .build();
    }

    private static Listing buildFakeListing(final long id) {
        final Category category = Category.builder().id(1L).name("Category").build();
        final Subcategory subcategory = Subcategory.builder().id(1L).name("Subcategory").category(category).build();
        final Product product = Product.builder()
            .id(1L)
            .brand("Brand")
            .model("Model")
            .year(2023)
            .subcategory(subcategory)
            .build();
        return Listing.builder()
            .id(id)
            .title("Listing " + id)
            .creator(buildFakeUser(SELLER_ID))
            .price(new Price(new BigDecimal("100.00")))
            .product(product)
            .status(ListingStatus.ACTIVE)
            .condition(Condition.GOOD)
            .build();
    }

    private static OfferFilter.OfferFilterBuilder baseFilter() {
        return OfferFilter.builder().page(1).pageSize(PAGE_SIZE);
    }

    private static List<Long> idsOf(final List<Offer> offers) {
        return offers.stream().map(Offer::getId).collect(Collectors.toList());
    }

    private static List<Long> sortedIdsOf(final List<Offer> offers) {
        return offers.stream().map(Offer::getId).sorted().collect(Collectors.toList());
    }

    private int countOffersWhere(final String whereClause) {
        em.flush();
        return JdbcTestUtils.countRowsInTableWhere(jdbcTemplate, "offers", whereClause);
    }

    /* ---------------------------------------------------------------------------------------------- */
    /* getById                                                                                         */
    /* ---------------------------------------------------------------------------------------------- */

    @Test
    public void testGetByIdExists() {
        // Act
        final Optional<Offer> maybeOffer = offerDao.getById(LOW_OFFER_ID);

        // Assert
        Assert.assertTrue(maybeOffer.isPresent());
        final Offer offer = maybeOffer.get();
        Assert.assertEquals(LOW_OFFER_ID, (long) offer.getId());
        Assert.assertEquals(0, new BigDecimal("1400.00").compareTo(offer.getAmount()));
        Assert.assertFalse(offer.getIsFullPrice());
        Assert.assertEquals(OfferStatus.PENDING, offer.getStatus());
        Assert.assertNull(offer.getMessage());
        final Instant storedCreatedAt = jdbcTemplate.queryForObject(
            "SELECT created_at FROM offers WHERE offer_id = ?", java.sql.Timestamp.class, LOW_OFFER_ID).toInstant();
        Assert.assertEquals(storedCreatedAt, offer.getCreatedAt());
    }

    @Test
    public void testGetByIdDoesNotExist() {
        // Act
        final Optional<Offer> maybeOffer = offerDao.getById(NON_EXISTING_ID);

        // Assert
        Assert.assertFalse(maybeOffer.isPresent());
    }

    @Test
    public void testGetByIdLoadsBuyer() {
        // Act
        final Offer offer = offerDao.getById(LOW_OFFER_ID).orElseThrow();

        // Assert
        Assert.assertEquals(BUYER_ID, (long) offer.getBuyer().getId());
        Assert.assertEquals("buyer_user", offer.getBuyer().getUsername());
    }

    @Test
    public void testGetByIdLoadsListingWithCreatorAndProduct() {
        // Act
        final Listing listing = offerDao.getById(LOW_OFFER_ID).orElseThrow().getListing();

        // Assert
        Assert.assertEquals(MACBOOK_LISTING_ID, (long) listing.getId());
        Assert.assertEquals("MacBook Pro 2023", listing.getTitle());
        Assert.assertEquals(SELLER_ID, (long) listing.getCreator().getId());
        Assert.assertEquals("fake_user", listing.getCreator().getUsername());
        Assert.assertEquals("Apple", listing.getProduct().getBrand());
        Assert.assertEquals("Laptops", listing.getProduct().getSubcategory().getName());
        Assert.assertEquals("Electronics", listing.getProduct().getSubcategory().getCategory().getName());
    }

    @Test
    public void testGetByIdLoadsListingCoverImage() {
        // Act
        final Listing listing = offerDao.getById(LOW_OFFER_ID).orElseThrow().getListing();

        // Assert
        Assert.assertEquals(List.of(FRONT_IMAGE_ID, BACK_IMAGE_ID), listing.getImageIds());
    }

    @Test
    public void testGetByIdLoadsListingStatusInsteadOfOfferStatus() {
        // Act
        final Offer offer = offerDao.getById(ACCEPTED_OFFER_ID).orElseThrow();

        // Assert
        Assert.assertEquals(OLD_MACBOOK_LISTING_ID, (long) offer.getListing().getId());
        Assert.assertEquals(OfferStatus.ACCEPTED, offer.getStatus());
        Assert.assertEquals(ListingStatus.SOLD, offer.getListing().getStatus());
    }

    @Test
    public void testGetByIdLoadsProofOfPayment() {
        // Act
        final Offer offer = offerDao.getById(ACCEPTED_OFFER_ID).orElseThrow();

        // Assert
        final File proofOfPayment = offer.getProofOfPayment().orElseThrow();
        Assert.assertEquals(PAYMENT_FILE_ID, (long) proofOfPayment.getId());
        Assert.assertEquals("payment.pdf", proofOfPayment.getFilename());
        Assert.assertEquals(Optional.of("application/pdf"), proofOfPayment.getContentType());
        Assert.assertEquals(3L, proofOfPayment.getSize());
    }

    @Test
    public void testGetByIdLoadsProofOfShipping() {
        // Act
        final Offer offer = offerDao.getById(ACCEPTED_OFFER_ID).orElseThrow();

        // Assert
        final File proofOfShipping = offer.getProofOfShipping().orElseThrow();
        Assert.assertEquals(SHIPPING_FILE_ID, (long) proofOfShipping.getId());
        Assert.assertEquals("shipping.jpg", proofOfShipping.getFilename());
        Assert.assertEquals(Optional.of("image/jpeg"), proofOfShipping.getContentType());
        Assert.assertEquals(2L, proofOfShipping.getSize());
        Assert.assertEquals("TRACK-123", offer.getTrackingNumber());
    }

    @Test
    public void testGetByIdLoadsRatings() {
        // Act
        final Offer offer = offerDao.getById(ACCEPTED_OFFER_ID).orElseThrow();

        // Assert
        Assert.assertEquals(Optional.of(OfferRating.POSITIVE), offer.getBuyerRating());
        Assert.assertEquals(Optional.empty(), offer.getSellerRating());
    }

    @Test
    public void testGetByIdWithoutProofsRatingsOrTrade() {
        // Act
        final Offer offer = offerDao.getById(LOW_OFFER_ID).orElseThrow();

        // Assert
        Assert.assertEquals(Optional.empty(), offer.getProofOfPayment());
        Assert.assertEquals(Optional.empty(), offer.getProofOfShipping());
        Assert.assertNull(offer.getTrackingNumber());
        Assert.assertEquals(Optional.empty(), offer.getBuyerRating());
        Assert.assertEquals(Optional.empty(), offer.getSellerRating());
        Assert.assertEquals(Optional.empty(), offer.getOfferedListing());
    }

    @Test
    public void testGetByIdLoadsOfferedListingOfTrade() {
        // Act
        final Offer offer = offerDao.getById(TRADE_OFFER_ID).orElseThrow();

        // Assert
        Assert.assertEquals(OfferStatus.PENDING_PAYMENT, offer.getStatus());
        final Listing offered = offer.getOfferedListing().orElseThrow();
        Assert.assertEquals(MACBOOK_LISTING_ID, (long) offered.getId());
        Assert.assertEquals("MacBook Pro 2023", offered.getTitle());
        Assert.assertEquals(SELLER_ID, (long) offered.getCreator().getId());
        Assert.assertEquals("Apple", offered.getProduct().getBrand());
        Assert.assertEquals("Electronics", offered.getProduct().getSubcategory().getCategory().getName());
        Assert.assertEquals(List.of(FRONT_IMAGE_ID, BACK_IMAGE_ID), offered.getImageIds());
    }

    @Test
    public void testGetByIdListingWithoutImagesHasEmptyImageIds() {
        // Act
        final Offer offer = offerDao.getById(TRADE_OFFER_ID).orElseThrow();

        // Assert
        Assert.assertEquals(GAMING_LISTING_ID, (long) offer.getListing().getId());
        Assert.assertTrue(offer.getListing().getImageIds().isEmpty());
    }

    @Test
    public void testGetByIdLowerOfferHasOtherAndBetterOffers() {
        // Act
        final Offer offer = offerDao.getById(LOW_OFFER_ID).orElseThrow();

        // Assert
        Assert.assertTrue(offer.isHasOtherOffers());
        Assert.assertTrue(offer.isHasBetterOffers());
    }

    @Test
    public void testGetByIdHighestOfferHasOtherButNoBetterOffers() {
        // Act
        final Offer offer = offerDao.getById(FULL_PRICE_OFFER_ID).orElseThrow();

        // Assert
        Assert.assertTrue(offer.isHasOtherOffers());
        Assert.assertFalse(offer.isHasBetterOffers());
    }

    @Test
    public void testGetByIdOnlyCountsPendingOffersAsOtherOffers() {
        // Act (listing 4 also has a rejected and a pending_payment offer, which must be ignored)
        final Offer offer = offerDao.getById(GAMING_OFFER_ID).orElseThrow();

        // Assert
        Assert.assertFalse(offer.isHasOtherOffers());
        Assert.assertFalse(offer.isHasBetterOffers());
    }

    /* ---------------------------------------------------------------------------------------------- */
    /* getByListingAndBuyer                                                                            */
    /* ---------------------------------------------------------------------------------------------- */

    @Test
    public void testGetByListingAndBuyerReturnsMostRecentActiveOffer() {
        // Act
        final Optional<Offer> maybeOffer = offerDao.getByListingAndBuyer(
            buildFakeListing(MACBOOK_LISTING_ID), buildFakeUser(BUYER_ID));

        // Assert
        Assert.assertTrue(maybeOffer.isPresent());
        Assert.assertEquals(FULL_PRICE_OFFER_ID, (long) maybeOffer.get().getId());
    }

    @Test
    public void testGetByListingAndBuyerIncludesPendingPayment() {
        // Act
        final Optional<Offer> maybeOffer = offerDao.getByListingAndBuyer(
            buildFakeListing(GAMING_LISTING_ID), buildFakeUser(SELLER_ID));

        // Assert
        Assert.assertTrue(maybeOffer.isPresent());
        Assert.assertEquals(TRADE_OFFER_ID, (long) maybeOffer.get().getId());
    }

    @Test
    public void testGetByListingAndBuyerIgnoresClosedOffers() {
        // Act (offers 5, 7 and 8 on listing 3 are all accepted)
        final Optional<Offer> maybeOffer = offerDao.getByListingAndBuyer(
            buildFakeListing(OLD_MACBOOK_LISTING_ID), buildFakeUser(SELLER_ID));

        // Assert
        Assert.assertFalse(maybeOffer.isPresent());
    }

    @Test
    public void testGetByListingAndBuyerWithoutOffers() {
        // Act
        final Optional<Offer> maybeOffer = offerDao.getByListingAndBuyer(
            buildFakeListing(GALAXY_LISTING_ID), buildFakeUser(BUYER_ID));

        // Assert
        Assert.assertFalse(maybeOffer.isPresent());
    }

    /* ---------------------------------------------------------------------------------------------- */
    /* search                                                                                          */
    /* ---------------------------------------------------------------------------------------------- */

    @Test
    public void testSearchWithoutFiltersReturnsAllOrderedByCreatedAtDesc() {
        // Act
        final Page<Offer> page = offerDao.search(baseFilter().build());

        // Assert
        Assert.assertEquals(TOTAL_OFFERS, page.getTotalCount());
        Assert.assertEquals(List.of(8L, 7L, 6L, 4L, 3L, 5L, 2L, 1L), idsOf(page.getContent()));
    }

    @Test
    public void testSearchBySeller() {
        // Act
        final Page<Offer> page = offerDao.search(baseFilter().sellerId(BUYER_ID).build());

        // Assert
        Assert.assertEquals(6, page.getTotalCount());
        Assert.assertEquals(List.of(8L, 7L, 6L, 4L, 3L, 5L), idsOf(page.getContent()));
    }

    @Test
    public void testSearchByBuyer() {
        // Act
        final Page<Offer> page = offerDao.search(baseFilter().buyerId(BUYER_ID).build());

        // Assert
        Assert.assertEquals(2, page.getTotalCount());
        Assert.assertEquals(List.of(FULL_PRICE_OFFER_ID, LOW_OFFER_ID), idsOf(page.getContent()));
    }

    @Test
    public void testSearchBySingleStatus() {
        // Act
        final Page<Offer> page = offerDao.search(
            baseFilter().sellerId(BUYER_ID).status(List.of(OfferStatus.ACCEPTED)).build());

        // Assert
        Assert.assertEquals(3, page.getTotalCount());
        Assert.assertEquals(
            List.of(RECENT_ACCEPTED_OFFER_ID, RATED_OFFER_ID, ACCEPTED_OFFER_ID), idsOf(page.getContent()));
    }

    @Test
    public void testSearchBySeveralStatuses() {
        // Act
        final Page<Offer> page = offerDao.search(baseFilter()
            .sellerId(BUYER_ID)
            .status(List.of(OfferStatus.PENDING, OfferStatus.PENDING_PAYMENT))
            .build());

        // Assert
        Assert.assertEquals(2, page.getTotalCount());
        Assert.assertEquals(List.of(TRADE_OFFER_ID, GAMING_OFFER_ID), idsOf(page.getContent()));
    }

    @Test
    public void testSearchCombinesSellerAndBuyer() {
        // Act (user 1 never bought from themselves)
        final Page<Offer> page = offerDao.search(baseFilter().sellerId(SELLER_ID).buyerId(SELLER_ID).build());

        // Assert
        Assert.assertEquals(0, page.getTotalCount());
        Assert.assertTrue(page.isEmpty());
    }

    @Test
    public void testSearchPaginates() {
        // Act
        final Page<Offer> page = offerDao.search(OfferFilter.builder()
            .sellerId(BUYER_ID)
            .page(2)
            .pageSize(2)
            .build());

        // Assert
        Assert.assertEquals(6, page.getTotalCount());
        Assert.assertEquals(2, page.getPage());
        Assert.assertEquals(2, page.getPageSize());
        Assert.assertEquals(List.of(TRADE_OFFER_ID, REJECTED_OFFER_ID), idsOf(page.getContent()));
    }

    @Test
    public void testSearchPageOutOfRangeIsEmptyButKeepsTotal() {
        // Act
        final Page<Offer> page = offerDao.search(OfferFilter.builder()
            .sellerId(BUYER_ID)
            .page(10)
            .pageSize(2)
            .build());

        // Assert
        Assert.assertTrue(page.isEmpty());
        Assert.assertEquals(6, page.getTotalCount());
    }

    /* ---------------------------------------------------------------------------------------------- */
    /* create                                                                                          */
    /* ---------------------------------------------------------------------------------------------- */

    @Test
    public void testCreateReturnsCreatedOffer() {
        // Act
        final Offer offer = offerDao.create(GALAXY_LISTING_ID, buildFakeUser(BUYER_ID), new BigDecimal("750.00"),
            false, OfferStatus.PENDING, "Is it unlocked?", NEW_OFFER_CREATED_AT, null);

        // Assert
        Assert.assertNotNull(offer.getId());
        Assert.assertEquals(GALAXY_LISTING_ID, (long) offer.getListing().getId());
        Assert.assertEquals(BUYER_ID, (long) offer.getBuyer().getId());
        Assert.assertEquals(0, new BigDecimal("750.00").compareTo(offer.getAmount()));
        Assert.assertFalse(offer.getIsFullPrice());
        Assert.assertEquals(OfferStatus.PENDING, offer.getStatus());
        Assert.assertEquals("Is it unlocked?", offer.getMessage());
        Assert.assertEquals(NEW_OFFER_CREATED_AT, offer.getCreatedAt());
        Assert.assertEquals(Optional.empty(), offer.getOfferedListing());
        Assert.assertFalse(offer.isHasOtherOffers());
    }

    @Test
    public void testCreatePersistsOffer() {
        // Act
        final Offer offer = offerDao.create(GALAXY_LISTING_ID, buildFakeUser(BUYER_ID), new BigDecimal("750.00"),
            false, OfferStatus.PENDING, null, NEW_OFFER_CREATED_AT, null);

        // Assert
        em.flush();
        Assert.assertEquals(TOTAL_OFFERS + 1, JdbcTestUtils.countRowsInTable(jdbcTemplate, "offers"));
        Assert.assertEquals(1, countOffersWhere("offer_id = " + offer.getId()
            + " AND listing_id = " + GALAXY_LISTING_ID
            + " AND buyer_id = " + BUYER_ID
            + " AND status = 'PENDING'"));
    }

    @Test
    public void testCreateTradeOfferLinksOfferedListing() {
        // Act (user 1 offers their Galaxy for user 2's gaming laptop)
        final Offer offer = offerDao.create(GAMING_LISTING_ID, buildFakeUser(SELLER_ID), new BigDecimal("500.00"),
            false, OfferStatus.PENDING, null, NEW_OFFER_CREATED_AT, GALAXY_LISTING_ID);

        // Assert
        final Listing offered = offer.getOfferedListing().orElseThrow();
        Assert.assertEquals(GALAXY_LISTING_ID, (long) offered.getId());
        Assert.assertEquals("Galaxy S23", offered.getTitle());
        Assert.assertTrue(offered.getImageIds().isEmpty());
    }

    /* ---------------------------------------------------------------------------------------------- */
    /* updateStatus                                                                                    */
    /* ---------------------------------------------------------------------------------------------- */

    @Test
    public void testUpdateStatusExisting() {
        // Act
        final boolean updated = offerDao.updateStatus(LOW_OFFER_ID, OfferStatus.REJECTED);

        // Assert
        Assert.assertTrue(updated);
        Assert.assertEquals(1, countOffersWhere("offer_id = " + LOW_OFFER_ID + " AND status = 'REJECTED'"));
    }

    @Test
    public void testUpdateStatusNonExisting() {
        // Act
        final boolean updated = offerDao.updateStatus(NON_EXISTING_ID, OfferStatus.REJECTED);

        // Assert
        Assert.assertFalse(updated);
    }

    /* ---------------------------------------------------------------------------------------------- */
    /* withdraw                                                                                        */
    /* ---------------------------------------------------------------------------------------------- */

    @Test
    public void testWithdrawPendingOfferByItsBuyer() {
        // Act
        final boolean withdrawn = offerDao.withdraw(LOW_OFFER_ID, BUYER_ID);

        // Assert
        Assert.assertTrue(withdrawn);
        Assert.assertEquals(1, countOffersWhere("offer_id = " + LOW_OFFER_ID + " AND status = 'WITHDRAWN'"));
    }

    @Test
    public void testWithdrawByAnotherUserDoesNothing() {
        // Act
        final boolean withdrawn = offerDao.withdraw(LOW_OFFER_ID, SELLER_ID);

        // Assert
        Assert.assertFalse(withdrawn);
        Assert.assertEquals(1, countOffersWhere("offer_id = " + LOW_OFFER_ID + " AND status = 'PENDING'"));
    }

    @Test
    public void testWithdrawRejectedOfferDoesNothing() {
        // Act
        final boolean withdrawn = offerDao.withdraw(REJECTED_OFFER_ID, SELLER_ID);

        // Assert
        Assert.assertFalse(withdrawn);
        Assert.assertEquals(1, countOffersWhere("offer_id = " + REJECTED_OFFER_ID + " AND status = 'REJECTED'"));
    }

    @Test
    public void testWithdrawPendingPaymentOfferDoesNothing() {
        // Act
        final boolean withdrawn = offerDao.withdraw(TRADE_OFFER_ID, SELLER_ID);

        // Assert
        Assert.assertFalse(withdrawn);
        Assert.assertEquals(1, countOffersWhere("offer_id = " + TRADE_OFFER_ID + " AND status = 'PENDING_PAYMENT'"));
    }

    /* ---------------------------------------------------------------------------------------------- */
    /* rejectPendingOffers                                                                             */
    /* ---------------------------------------------------------------------------------------------- */

    @Test
    public void testRejectPendingOffersReturnsAllButTheExcludedOne() {
        // Act
        final List<Offer> rejected = offerDao.rejectPendingOffers(MACBOOK_LISTING_ID, FULL_PRICE_OFFER_ID);

        // Assert
        Assert.assertEquals(List.of(LOW_OFFER_ID), idsOf(rejected));
    }

    @Test
    public void testRejectPendingOffersKeepsTheExcludedOnePending() {
        // Act
        offerDao.rejectPendingOffers(MACBOOK_LISTING_ID, FULL_PRICE_OFFER_ID);

        // Assert
        Assert.assertEquals(1, countOffersWhere("offer_id = " + LOW_OFFER_ID + " AND status = 'REJECTED'"));
        Assert.assertEquals(1, countOffersWhere("offer_id = " + FULL_PRICE_OFFER_ID + " AND status = 'PENDING'"));
    }

    @Test
    public void testRejectPendingOffersWithoutExceptionRejectsAll() {
        // Act
        final List<Offer> rejected = offerDao.rejectPendingOffers(MACBOOK_LISTING_ID, null);

        // Assert
        Assert.assertEquals(List.of(LOW_OFFER_ID, FULL_PRICE_OFFER_ID), sortedIdsOf(rejected));
        Assert.assertEquals(0, countOffersWhere("listing_id = " + MACBOOK_LISTING_ID + " AND status = 'PENDING'"));
    }

    @Test
    public void testRejectPendingOffersIgnoresOtherStatuses() {
        // Act (listing 4 also has a rejected and a pending_payment offer)
        final List<Offer> rejected = offerDao.rejectPendingOffers(GAMING_LISTING_ID, null);

        // Assert
        Assert.assertEquals(List.of(GAMING_OFFER_ID), idsOf(rejected));
        Assert.assertEquals(1, countOffersWhere("offer_id = " + TRADE_OFFER_ID + " AND status = 'PENDING_PAYMENT'"));
    }

    @Test
    public void testRejectPendingOffersDoesNotTouchOtherListings() {
        // Act
        offerDao.rejectPendingOffers(MACBOOK_LISTING_ID, null);

        // Assert
        Assert.assertEquals(1, countOffersWhere("offer_id = " + GAMING_OFFER_ID + " AND status = 'PENDING'"));
    }

    /* ---------------------------------------------------------------------------------------------- */
    /* countPendingBySeller                                                                            */
    /* ---------------------------------------------------------------------------------------------- */

    @Test
    public void testCountPendingBySeller() {
        // Act
        final int count = offerDao.countPendingBySeller(buildFakeUser(SELLER_ID));

        // Assert
        Assert.assertEquals(2, count);
    }

    @Test
    public void testCountPendingBySellerIgnoresOtherStatuses() {
        // Act (user 2 sells listings 3 and 4: only offer 3 is pending)
        final int count = offerDao.countPendingBySeller(buildFakeUser(BUYER_ID));

        // Assert
        Assert.assertEquals(1, count);
    }

    @Test
    public void testCountPendingBySellerWithoutListings() {
        // Act
        final int count = offerDao.countPendingBySeller(buildFakeUser(NON_EXISTING_ID));

        // Assert
        Assert.assertEquals(0, count);
    }

    /* ---------------------------------------------------------------------------------------------- */
    /* updateProofOfPaymentId / updateProofOfShipping                                                  */
    /* ---------------------------------------------------------------------------------------------- */

    @Test
    public void testUpdateProofOfPaymentId() {
        // Act
        final boolean updated = offerDao.updateProofOfPaymentId(LOW_OFFER_ID, PAYMENT_FILE_ID);

        // Assert
        Assert.assertTrue(updated);
        final Offer offer = offerDao.getById(LOW_OFFER_ID).orElseThrow();
        final File proofOfPayment = offer.getProofOfPayment().orElseThrow();
        Assert.assertEquals(PAYMENT_FILE_ID, (long) proofOfPayment.getId());
        Assert.assertEquals("payment.pdf", proofOfPayment.getFilename());
    }

    @Test
    public void testUpdateProofOfPaymentIdNonExisting() {
        // Act
        final boolean updated = offerDao.updateProofOfPaymentId(NON_EXISTING_ID, PAYMENT_FILE_ID);

        // Assert
        Assert.assertFalse(updated);
    }

    @Test
    public void testUpdateProofOfShipping() {
        // Act
        final boolean updated = offerDao.updateProofOfShipping(LOW_OFFER_ID, SHIPPING_FILE_ID, "TRACK-999");

        // Assert
        Assert.assertTrue(updated);
        final Offer offer = offerDao.getById(LOW_OFFER_ID).orElseThrow();
        final File proofOfShipping = offer.getProofOfShipping().orElseThrow();
        Assert.assertEquals(SHIPPING_FILE_ID, (long) proofOfShipping.getId());
        Assert.assertEquals("shipping.jpg", proofOfShipping.getFilename());
        Assert.assertEquals("TRACK-999", offer.getTrackingNumber());
    }

    @Test
    public void testUpdateProofOfShippingNonExisting() {
        // Act
        final boolean updated = offerDao.updateProofOfShipping(NON_EXISTING_ID, SHIPPING_FILE_ID, "TRACK-999");

        // Assert
        Assert.assertFalse(updated);
    }

    /* ---------------------------------------------------------------------------------------------- */
    /* markAccepted                                                                                    */
    /* ---------------------------------------------------------------------------------------------- */

    @Test
    public void testMarkAcceptedSetsStatusAndDate() {
        // Act
        final boolean updated = offerDao.markAccepted(TRADE_OFFER_ID, NEW_OFFER_CREATED_AT);

        // Assert
        // accepted_at is not mapped into Offer, so it is checked directly on the table
        Assert.assertTrue(updated);
        Assert.assertEquals(1, countOffersWhere("offer_id = " + TRADE_OFFER_ID
            + " AND status = 'ACCEPTED' AND accepted_at IS NOT NULL"));
    }

    @Test
    public void testMarkAcceptedNonExisting() {
        // Act
        final boolean updated = offerDao.markAccepted(NON_EXISTING_ID, NEW_OFFER_CREATED_AT);

        // Assert
        Assert.assertFalse(updated);
    }

    /* ---------------------------------------------------------------------------------------------- */
    /* setSellerRating / setBuyerRating                                                                */
    /* ---------------------------------------------------------------------------------------------- */

    @Test
    public void testSetSellerRatingWhenNotRatedYet() {
        // Act
        final boolean updated = offerDao.setSellerRating(ACCEPTED_OFFER_ID, OfferRating.NEGATIVE);

        // Assert
        Assert.assertTrue(updated);
        Assert.assertEquals(Optional.of(OfferRating.NEGATIVE),
            offerDao.getById(ACCEPTED_OFFER_ID).orElseThrow().getSellerRating());
    }

    @Test
    public void testSetSellerRatingWhenAlreadyRatedDoesNothing() {
        // Act
        final boolean updated = offerDao.setSellerRating(RATED_OFFER_ID, OfferRating.NEGATIVE);

        // Assert
        Assert.assertFalse(updated);
        Assert.assertEquals(1, countOffersWhere("offer_id = " + RATED_OFFER_ID + " AND seller_rating = 'NEUTRAL'"));
    }

    @Test
    public void testSetBuyerRatingWhenNotRatedYet() {
        // Act
        final boolean updated = offerDao.setBuyerRating(RECENT_ACCEPTED_OFFER_ID, OfferRating.NEUTRAL);

        // Assert
        Assert.assertTrue(updated);
        Assert.assertEquals(Optional.of(OfferRating.NEUTRAL),
            offerDao.getById(RECENT_ACCEPTED_OFFER_ID).orElseThrow().getBuyerRating());
    }

    @Test
    public void testSetBuyerRatingWhenAlreadyRatedDoesNothing() {
        // Act
        final boolean updated = offerDao.setBuyerRating(ACCEPTED_OFFER_ID, OfferRating.NEGATIVE);

        // Assert
        Assert.assertFalse(updated);
        Assert.assertEquals(1, countOffersWhere("offer_id = " + ACCEPTED_OFFER_ID + " AND buyer_rating = 'POSITIVE'"));
    }

    @Test
    public void testSetRatingNonExisting() {
        // Act
        final boolean updated = offerDao.setSellerRating(NON_EXISTING_ID, OfferRating.POSITIVE);

        // Assert
        Assert.assertFalse(updated);
    }

    /* ---------------------------------------------------------------------------------------------- */
    /* getAcceptedUnratedBefore                                                                        */
    /* ---------------------------------------------------------------------------------------------- */

    @Test
    public void testGetAcceptedUnratedBeforeOnlyReturnsOffersAcceptedBeforeCutoff() {
        // Act (offer 8 was accepted after the cutoff, offer 7 is fully rated)
        final List<Offer> offers = offerDao.getAcceptedUnratedBefore(Instant.parse("2026-02-01T00:00:00Z"));

        // Assert
        Assert.assertEquals(List.of(ACCEPTED_OFFER_ID), idsOf(offers));
    }

    @Test
    public void testGetAcceptedUnratedBeforeWithLaterCutoff() {
        // Act
        final List<Offer> offers = offerDao.getAcceptedUnratedBefore(Instant.parse("2026-04-01T00:00:00Z"));

        // Assert
        Assert.assertEquals(List.of(ACCEPTED_OFFER_ID, RECENT_ACCEPTED_OFFER_ID), sortedIdsOf(offers));
    }

    @Test
    public void testGetAcceptedUnratedBeforeEarliestCutoffReturnsNothing() {
        // Act
        final List<Offer> offers = offerDao.getAcceptedUnratedBefore(Instant.parse("2026-01-01T00:00:00Z"));

        // Assert
        Assert.assertTrue(offers.isEmpty());
    }

    @Test
    public void testGetAcceptedUnratedBeforeIgnoresNonAcceptedOffers() {
        // Arrange (pending offer 1 gets an accepted_at but keeps its status)
        jdbcTemplate.update("UPDATE offers SET accepted_at = ? WHERE offer_id = ?",
            java.sql.Timestamp.from(Instant.parse("2026-01-02T00:00:00Z")), LOW_OFFER_ID);

        // Act
        final List<Offer> offers = offerDao.getAcceptedUnratedBefore(Instant.parse("2026-02-01T00:00:00Z"));

        // Assert
        Assert.assertEquals(List.of(ACCEPTED_OFFER_ID), idsOf(offers));
    }
}