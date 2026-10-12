package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.model.Category;
import ar.edu.itba.paw.model.Condition;
import ar.edu.itba.paw.model.Listing;
import ar.edu.itba.paw.model.ListingFilter;
import ar.edu.itba.paw.model.ListingSort;
import ar.edu.itba.paw.model.ListingStatus;
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

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { TestConfig.class })
@Sql("classpath:sql/listing-data.sql")
@Transactional
@Rollback
public class ListingJpaDaoTest {

    // Fixture ids (see sql/listing-data.sql)
    private static final long MACBOOK_LISTING_ID = 1L;      // ACTIVE, 1500, GOOD, trade, 2 pending offers, 2 images
    private static final long GALAXY_LISTING_ID = 2L;       // ACTIVE, 800, LIKE_NEW, category 2, no offers
    private static final long OLD_MACBOOK_LISTING_ID = 3L;  // SOLD, 300, FOR_PARTS, creator 2
    private static final long GAMING_LISTING_ID = 4L;       // ACTIVE, 2000, EXCELLENT, trade, creator 2, 1 pending offer
    private static final long NON_EXISTING_ID = 9000L;

    private static final long SELLER_ID = 1L;
    private static final long BUYER_ID = 2L;
    private static final long CATEGORY_ID = 1L;
    private static final long PHONES_CATEGORY_ID = 2L;
    private static final long SUBCATEGORY_ID = 1L;
    private static final long PHONES_SUBCATEGORY_ID = 2L;
    private static final long PRODUCT_ID = 1L;
    private static final long PHONE_PRODUCT_ID = 2L;
    private static final long FRONT_IMAGE_ID = 1L;
    private static final long BACK_IMAGE_ID = 2L;
    private static final long UNASSIGNED_IMAGE_1_ID = 3L;
    private static final long UNASSIGNED_IMAGE_2_ID = 4L;

    private static final int PAGE_SIZE = 10;

    @Autowired
    private ListingDao listingDao;

    @Autowired
    private DataSource dataSource;

    private JdbcTemplate jdbcTemplate;

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

    private static Product buildFakeProduct(final long productId, final long subcategoryId, final long categoryId) {
        final Category category = Category.builder().id(categoryId).name("Category " + categoryId).build();
        final Subcategory subcategory = Subcategory.builder()
            .id(subcategoryId)
            .name("Subcategory " + subcategoryId)
            .category(category)
            .build();
        return Product.builder()
            .id(productId)
            .brand("Brand")
            .model("Model")
            .year(2023)
            .subcategory(subcategory)
            .build();
    }

    private static ListingFilter.ListingFilterBuilder baseFilter() {
        return ListingFilter.builder().page(1).pageSize(PAGE_SIZE);
    }

    private static List<Long> idsOf(final Page<Listing> page) {
        return page.getContent().stream().map(Listing::getId).collect(Collectors.toList());
    }

    /* ---------------------------------------------------------------------------------------------- */
    /* getById                                                                                         */
    /* ---------------------------------------------------------------------------------------------- */

    @Test
    public void testGetByIdExists() {
        // Act
        final Optional<Listing> maybeListing = listingDao.getById(MACBOOK_LISTING_ID);

        // Assert
        Assert.assertTrue(maybeListing.isPresent());
        final Listing listing = maybeListing.get();
        Assert.assertEquals(MACBOOK_LISTING_ID, (long) listing.getId());
        Assert.assertEquals("MacBook Pro 2023", listing.getTitle());
        Assert.assertEquals("Barely used laptop", listing.getDescription());
        Assert.assertEquals(0, new BigDecimal("1500.00").compareTo(listing.getPrice().getAmount()));
        Assert.assertEquals(ListingStatus.ACTIVE, listing.getStatus());
        Assert.assertEquals(Condition.GOOD, listing.getCondition());
        Assert.assertTrue(listing.isAcceptsTrade());
    }

    @Test
    public void testGetByIdLoadsCreator() {
        // Act
        final Listing listing = listingDao.getById(MACBOOK_LISTING_ID).orElseThrow();

        // Assert
        Assert.assertEquals(SELLER_ID, (long) listing.getCreator().getId());
        Assert.assertEquals("fake_user", listing.getCreator().getUsername());
    }

    @Test
    public void testGetByIdLoadsProductWithCategory() {
        // Act
        final Listing listing = listingDao.getById(MACBOOK_LISTING_ID).orElseThrow();

        // Assert
        Assert.assertEquals(PRODUCT_ID, (long) listing.getProduct().getId());
        Assert.assertEquals("Apple", listing.getProduct().getBrand());
        Assert.assertEquals(SUBCATEGORY_ID, (long) listing.getProduct().getSubcategory().getId());
        Assert.assertEquals(CATEGORY_ID, (long) listing.getProduct().getSubcategory().getCategory().getId());
        Assert.assertEquals("Electronics", listing.getProduct().getSubcategory().getCategory().getName());
    }

    @Test
    public void testGetByIdReturnsImageIdsInDisplayOrder() {
        // Act
        final Listing listing = listingDao.getById(MACBOOK_LISTING_ID).orElseThrow();

        // Assert
        Assert.assertEquals(List.of(FRONT_IMAGE_ID, BACK_IMAGE_ID), listing.getImageIds());
    }

    @Test
    public void testGetByIdWithoutImagesReturnsEmptyImageIds() {
        // Act
        final Listing listing = listingDao.getById(GALAXY_LISTING_ID).orElseThrow();

        // Assert
        Assert.assertTrue(listing.getImageIds().isEmpty());
    }

    @Test
    public void testGetByIdCountsPendingOffers() {
        // Act
        final Listing listing = listingDao.getById(MACBOOK_LISTING_ID).orElseThrow();

        // Assert
        Assert.assertEquals(2, listing.getPendingOffersCount());
    }

    @Test
    public void testGetByIdIgnoresNonPendingOffersInCount() {
        // Act
        final Listing listing = listingDao.getById(GAMING_LISTING_ID).orElseThrow();

        // Assert
        Assert.assertEquals(1, listing.getPendingOffersCount());
    }

    @Test
    public void testGetByIdDoesNotExist() {
        // Act
        final Optional<Listing> maybeListing = listingDao.getById(NON_EXISTING_ID);

        // Assert
        Assert.assertFalse(maybeListing.isPresent());
    }

    @Test
    public void testGetByIdInvalidId() {
        // Act
        final Optional<Listing> maybeListing = listingDao.getById(-1L);

        // Assert
        Assert.assertFalse(maybeListing.isPresent());
    }

    /* ---------------------------------------------------------------------------------------------- */
    /* search: filters                                                                                 */
    /* ---------------------------------------------------------------------------------------------- */

    @Test
    public void testSearchWithoutFiltersReturnsAllListings() {
        // Act
        final Page<Listing> page = listingDao.search(baseFilter().build());

        // Assert
        Assert.assertEquals(4, page.getTotalCount());
        Assert.assertEquals(
            List.of(GAMING_LISTING_ID, OLD_MACBOOK_LISTING_ID, GALAXY_LISTING_ID, MACBOOK_LISTING_ID),
            idsOf(page)
        );
    }

    @Test
    public void testSearchByStatus() {
        // Act
        final Page<Listing> page = listingDao.search(baseFilter().status(ListingStatus.SOLD).build());

        // Assert
        Assert.assertEquals(List.of(OLD_MACBOOK_LISTING_ID), idsOf(page));
    }

    @Test
    public void testSearchByCategory() {
        // Act
        final Page<Listing> page = listingDao.search(baseFilter().categoryId(PHONES_CATEGORY_ID).build());

        // Assert
        Assert.assertEquals(List.of(GALAXY_LISTING_ID), idsOf(page));
    }

    @Test
    public void testSearchBySubcategory() {
        // Act
        final Page<Listing> page = listingDao.search(baseFilter().subcategoryId(SUBCATEGORY_ID).build());

        // Assert
        Assert.assertEquals(List.of(GAMING_LISTING_ID, OLD_MACBOOK_LISTING_ID, MACBOOK_LISTING_ID), idsOf(page));
    }

    @Test
    public void testSearchByPriceRangeIsInclusive() {
        // Act
        final Page<Listing> page = listingDao.search(
            baseFilter().minPrice(new BigDecimal("800.00")).maxPrice(new BigDecimal("1500.00")).build()
        );

        // Assert
        Assert.assertEquals(List.of(GALAXY_LISTING_ID, MACBOOK_LISTING_ID), idsOf(page));
    }

    @Test
    public void testSearchByCondition() {
        // Act
        final Page<Listing> page = listingDao.search(baseFilter().condition(Condition.LIKE_NEW).build());

        // Assert
        Assert.assertEquals(List.of(GALAXY_LISTING_ID), idsOf(page));
    }

    @Test
    public void testSearchByAcceptsTrade() {
        // Act
        final Page<Listing> page = listingDao.search(baseFilter().acceptsTrade(true).build());

        // Assert
        Assert.assertEquals(List.of(GAMING_LISTING_ID, MACBOOK_LISTING_ID), idsOf(page));
    }

    @Test
    public void testSearchByCreator() {
        // Act
        final Page<Listing> page = listingDao.search(baseFilter().creatorId(BUYER_ID).build());

        // Assert
        Assert.assertEquals(List.of(GAMING_LISTING_ID, OLD_MACBOOK_LISTING_ID), idsOf(page));
    }

    @Test
    public void testSearchByQueryMatchesTitleIgnoringCase() {
        // Act
        final Page<Listing> page = listingDao.search(baseFilter().query("MACBOOK").build());

        // Assert
        Assert.assertEquals(List.of(OLD_MACBOOK_LISTING_ID, MACBOOK_LISTING_ID), idsOf(page));
    }

    @Test
    public void testSearchByQueryMatchesDescription() {
        // Act
        final Page<Listing> page = listingDao.search(baseFilter().query("great shape").build());

        // Assert
        Assert.assertEquals(List.of(GALAXY_LISTING_ID), idsOf(page));
    }

    @Test
    public void testSearchByQueryMatchesTitleOrDescription() {
        // Act ("laptop" is in listing 4's title and in listing 1's description)
        final Page<Listing> page = listingDao.search(baseFilter().query("laptop").build());

        // Assert
        Assert.assertEquals(List.of(GAMING_LISTING_ID, MACBOOK_LISTING_ID), idsOf(page));
    }

    @Test
    public void testSearchWithActiveOffersOnly() {
        // Act
        final Page<Listing> page = listingDao.search(baseFilter().hasActiveOffers(true).build());

        // Assert
        Assert.assertEquals(List.of(GAMING_LISTING_ID, MACBOOK_LISTING_ID), idsOf(page));
    }

    @Test
    public void testSearchCombinesFilters() {
        // Act
        final Page<Listing> page = listingDao.search(
            baseFilter()
                .status(ListingStatus.ACTIVE)
                .acceptsTrade(true)
                .minPrice(new BigDecimal("1600.00"))
                .build()
        );

        // Assert
        Assert.assertEquals(List.of(GAMING_LISTING_ID), idsOf(page));
    }

    @Test
    public void testSearchWithoutMatchesReturnsEmptyPage() {
        // Act
        final Page<Listing> page = listingDao.search(baseFilter().creatorId(NON_EXISTING_ID).build());

        // Assert
        Assert.assertTrue(page.isEmpty());
        Assert.assertEquals(0, page.getTotalCount());
    }

    @Test
    public void testSearchReturnsOnlyCoverImage() {
        // Act
        final Page<Listing> page = listingDao.search(baseFilter().query("MacBook Pro").build());

        // Assert
        Assert.assertEquals(List.of(FRONT_IMAGE_ID, BACK_IMAGE_ID), page.getContent().getFirst().getImageIds());
    }

    /* ---------------------------------------------------------------------------------------------- */
    /* search: sorting (only ACTIVE listings: 1, 2 and 4)                                              */
    /* ---------------------------------------------------------------------------------------------- */

    @Test
    public void testSearchDefaultSortIsMostRecentFirst() {
        // Act
        final Page<Listing> page = listingDao.search(baseFilter().status(ListingStatus.ACTIVE).build());

        // Assert
        Assert.assertEquals(List.of(GAMING_LISTING_ID, GALAXY_LISTING_ID, MACBOOK_LISTING_ID), idsOf(page));
    }

    @Test
    public void testSearchSortByPriceAscending() {
        // Act
        final Page<Listing> page = listingDao.search(
            baseFilter().status(ListingStatus.ACTIVE).sort(ListingSort.PRICE_ASC).build()
        );

        // Assert
        Assert.assertEquals(List.of(GALAXY_LISTING_ID, MACBOOK_LISTING_ID, GAMING_LISTING_ID), idsOf(page));
    }

    @Test
    public void testSearchSortByPriceDescending() {
        // Act
        final Page<Listing> page = listingDao.search(
            baseFilter().status(ListingStatus.ACTIVE).sort(ListingSort.PRICE_DESC).build()
        );

        // Assert
        Assert.assertEquals(List.of(GAMING_LISTING_ID, MACBOOK_LISTING_ID, GALAXY_LISTING_ID), idsOf(page));
    }

    @Test
    public void testSearchSortByNameAscending() {
        // Act
        final Page<Listing> page = listingDao.search(
            baseFilter().status(ListingStatus.ACTIVE).sort(ListingSort.NAME_ASC).build()
        );

        // Assert ("Galaxy S23" < "Gaming laptop" < "MacBook Pro 2023")
        Assert.assertEquals(List.of(GALAXY_LISTING_ID, GAMING_LISTING_ID, MACBOOK_LISTING_ID), idsOf(page));
    }

    @Test
    public void testSearchSortByNameDescending() {
        // Act
        final Page<Listing> page = listingDao.search(
            baseFilter().status(ListingStatus.ACTIVE).sort(ListingSort.NAME_DESC).build()
        );

        // Assert
        Assert.assertEquals(List.of(MACBOOK_LISTING_ID, GAMING_LISTING_ID, GALAXY_LISTING_ID), idsOf(page));
    }

    @Test
    public void testSearchSortByMostPendingOffers() {
        // Act
        final Page<Listing> page = listingDao.search(
            baseFilter().status(ListingStatus.ACTIVE).sort(ListingSort.MOST_OFFERS).build()
        );

        // Assert (2 pending, 1 pending, 0 pending)
        Assert.assertEquals(List.of(MACBOOK_LISTING_ID, GAMING_LISTING_ID, GALAXY_LISTING_ID), idsOf(page));
    }

    @Test
    public void testSearchSortByMostRecentPendingOffer() {
        // Act
        final Page<Listing> page = listingDao.search(
            baseFilter().status(ListingStatus.ACTIVE).sort(ListingSort.RECENT_OFFERS).build()
        );

        // Assert (listing 4's rejected offer is newer but must be ignored; listing 2 has no offers -> last)
        Assert.assertEquals(List.of(GAMING_LISTING_ID, MACBOOK_LISTING_ID, GALAXY_LISTING_ID), idsOf(page));
    }

    /* ---------------------------------------------------------------------------------------------- */
    /* search: pagination (only ACTIVE listings: 4, 2, 1 in default order)                             */
    /* ---------------------------------------------------------------------------------------------- */

    @Test
    public void testSearchFirstPage() {
        // Act
        final Page<Listing> page = listingDao.search(
            ListingFilter.builder().status(ListingStatus.ACTIVE).page(1).pageSize(2).build()
        );

        // Assert
        Assert.assertEquals(List.of(GAMING_LISTING_ID, GALAXY_LISTING_ID), idsOf(page));
        Assert.assertEquals(3, page.getTotalCount());
        Assert.assertEquals(2, page.getTotalPages());
        Assert.assertTrue(page.isHasNext());
    }

    @Test
    public void testSearchLastPage() {
        // Act
        final Page<Listing> page = listingDao.search(
            ListingFilter.builder().status(ListingStatus.ACTIVE).page(2).pageSize(2).build()
        );

        // Assert
        Assert.assertEquals(List.of(MACBOOK_LISTING_ID), idsOf(page));
        Assert.assertEquals(3, page.getTotalCount());
        Assert.assertFalse(page.isHasNext());
    }

    /* ---------------------------------------------------------------------------------------------- */
    /* create                                                                                          */
    /* ---------------------------------------------------------------------------------------------- */

    @Test
    public void testCreateReturnsActiveListing() {
        // Act
        final Listing created = listingDao.create(
            "Brand new listing", new Price(new BigDecimal("999.99")), buildFakeUser(SELLER_ID),
            buildFakeProduct(PRODUCT_ID, SUBCATEGORY_ID, CATEGORY_ID), Condition.UNUSED, true, false, "Still sealed", List.of()
        );

        // Assert
        Assert.assertNotNull(created.getId());
        Assert.assertEquals("Brand new listing", created.getTitle());
        Assert.assertEquals(ListingStatus.ACTIVE, created.getStatus());
        Assert.assertEquals(Condition.UNUSED, created.getCondition());
        Assert.assertEquals(0, created.getPendingOffersCount());
    }

    @Test
    public void testCreatePersistsListing() {
        // Act
        listingDao.create(
            "Brand new listing", new Price(new BigDecimal("999.99")), buildFakeUser(SELLER_ID),
            buildFakeProduct(PRODUCT_ID, SUBCATEGORY_ID, CATEGORY_ID), Condition.UNUSED, true, false, "Still sealed", List.of()
        );

        // Assert
        final int count = JdbcTestUtils.countRowsInTableWhere(
            jdbcTemplate,
            "listings",
            "title = 'Brand new listing' AND status = 'ACTIVE' AND creator_id = " + SELLER_ID
        );
        Assert.assertEquals(1, count);
    }

    @Test
    public void testCreatePersistsImagesInGivenOrder() {
        // Act
        final Listing created = listingDao.create(
            "Brand new listing", new Price(new BigDecimal("999.99")), buildFakeUser(SELLER_ID),
            buildFakeProduct(PRODUCT_ID, SUBCATEGORY_ID, CATEGORY_ID), Condition.UNUSED, true, false, "Still sealed",
            List.of(UNASSIGNED_IMAGE_2_ID, UNASSIGNED_IMAGE_1_ID)
        );

        // Assert
        final Listing reloaded = listingDao.getById(created.getId()).orElseThrow();
        Assert.assertEquals(List.of(UNASSIGNED_IMAGE_2_ID, UNASSIGNED_IMAGE_1_ID), reloaded.getImageIds());
    }

    @Test
    public void testCreateWithoutImagesStoresNoImageRows() {
        // Act
        final Listing created = listingDao.create(
            "Brand new listing", new Price(new BigDecimal("999.99")), buildFakeUser(SELLER_ID),
            buildFakeProduct(PRODUCT_ID, SUBCATEGORY_ID, CATEGORY_ID), Condition.UNUSED, true, false, "Still sealed", null
        );

        // Assert
        final int count = JdbcTestUtils.countRowsInTableWhere(
            jdbcTemplate, "listing_images", "listing_id = " + created.getId()
        );
        Assert.assertEquals(0, count);
        Assert.assertTrue(created.getImageIds().isEmpty());
    }

    /* ---------------------------------------------------------------------------------------------- */
    /* update                                                                                          */
    /* ---------------------------------------------------------------------------------------------- */

    @Test
    public void testUpdateReturnsUpdatedListing() {
        // Act
        final Listing updated = listingDao.update(
            MACBOOK_LISTING_ID, "MacBook Pro M3", new Price(new BigDecimal("1400.00")),
            buildFakeProduct(PHONE_PRODUCT_ID, PHONES_SUBCATEGORY_ID, PHONES_CATEGORY_ID),
            Condition.FAIR, false, false, "Some scratches"
        );

        // Assert
        Assert.assertEquals("MacBook Pro M3", updated.getTitle());
        Assert.assertEquals("Some scratches", updated.getDescription());
        Assert.assertEquals(0, new BigDecimal("1400.00").compareTo(updated.getPrice().getAmount()));
        Assert.assertEquals(PHONE_PRODUCT_ID, (long) updated.getProduct().getId());
        Assert.assertEquals(Condition.FAIR, updated.getCondition());
        Assert.assertFalse(updated.isAcceptsTrade());
    }

    @Test
    public void testUpdateKeepsStatus() {
        // Act
        final Listing updated = listingDao.update(
            MACBOOK_LISTING_ID, "MacBook Pro M3", new Price(new BigDecimal("1400.00")),
            buildFakeProduct(PRODUCT_ID, SUBCATEGORY_ID, CATEGORY_ID), Condition.FAIR, false, false, "Some scratches"
        );

        // Assert
        Assert.assertEquals(ListingStatus.ACTIVE, updated.getStatus());
    }

    @Test
    public void testUpdatePersistsChanges() {
        // Act
        listingDao.update(
            MACBOOK_LISTING_ID, "MacBook Pro M3", new Price(new BigDecimal("1400.00")),
            buildFakeProduct(PRODUCT_ID, SUBCATEGORY_ID, CATEGORY_ID), Condition.FAIR, false, false, "Some scratches"
        );

        // Assert
        final int count = JdbcTestUtils.countRowsInTableWhere(
            jdbcTemplate,
            "listings",
            "listing_id = " + MACBOOK_LISTING_ID + " AND title = 'MacBook Pro M3' AND accepts_trade = FALSE"
        );
        Assert.assertEquals(1, count);
    }

    /* ---------------------------------------------------------------------------------------------- */
    /* status changes                                                                                  */
    /* ---------------------------------------------------------------------------------------------- */

    @Test
    public void testPurchaseMarksListingAsSold() {
        // Act
        final ListingStatus status = listingDao.purchase(MACBOOK_LISTING_ID, BUYER_ID);

        // Assert
        Assert.assertEquals(ListingStatus.SOLD, status);
        Assert.assertEquals(1, JdbcTestUtils.countRowsInTableWhere(
            jdbcTemplate, "listings", "listing_id = " + MACBOOK_LISTING_ID + " AND status = 'SOLD'"
        ));
    }

    @Test
    public void testPendingTransactionMarksListingAsPending() {
        // Act
        final ListingStatus status = listingDao.pendingTransaction(MACBOOK_LISTING_ID, BUYER_ID);

        // Assert
        Assert.assertEquals(ListingStatus.PENDING_TRANSACTION, status);
        Assert.assertEquals(1, JdbcTestUtils.countRowsInTableWhere(
            jdbcTemplate, "listings", "listing_id = " + MACBOOK_LISTING_ID + " AND status = 'PENDING_TRANSACTION'"
        ));
    }

    @Test
    public void testCancelMarksListingAsCanceled() {
        // Act
        listingDao.cancel(MACBOOK_LISTING_ID);

        // Assert
        Assert.assertEquals(1, JdbcTestUtils.countRowsInTableWhere(
            jdbcTemplate, "listings", "listing_id = " + MACBOOK_LISTING_ID + " AND status = 'CANCELED'"
        ));
    }

    @Test
    public void testCancelDoesNotAffectOtherListings() {
        // Act
        listingDao.cancel(MACBOOK_LISTING_ID);

        // Assert
        Assert.assertEquals(1, JdbcTestUtils.countRowsInTableWhere(
            jdbcTemplate, "listings", "status = 'CANCELED'"
        ));
    }

    @Test
    public void testUpdateStatus() {
        // Act
        listingDao.updateStatus(GALAXY_LISTING_ID, ListingStatus.OFFERED_IN_TRADE);

        // Assert
        Assert.assertEquals(1, JdbcTestUtils.countRowsInTableWhere(
            jdbcTemplate, "listings", "listing_id = " + GALAXY_LISTING_ID + " AND status = 'OFFERED_IN_TRADE'"
        ));
    }
}