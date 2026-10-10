package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.model.Category;
import ar.edu.itba.paw.model.Language;
import ar.edu.itba.paw.model.Condition;
import ar.edu.itba.paw.model.File;
import ar.edu.itba.paw.model.Listing;
import ar.edu.itba.paw.model.ListingStatus;
import ar.edu.itba.paw.model.Offer;
import ar.edu.itba.paw.model.OfferFilter;
import ar.edu.itba.paw.model.OfferStatus;
import ar.edu.itba.paw.model.Page;
import ar.edu.itba.paw.model.Price;
import ar.edu.itba.paw.model.Product;
import ar.edu.itba.paw.model.Subcategory;
import ar.edu.itba.paw.model.User;
import ar.edu.itba.paw.model.OfferRating;
import ar.edu.itba.paw.persistence.schema.CategorySchema;
import ar.edu.itba.paw.persistence.schema.FileSchema;
import ar.edu.itba.paw.persistence.schema.ListingSchema;
import ar.edu.itba.paw.persistence.schema.OfferSchema;
import ar.edu.itba.paw.persistence.schema.ProductSchema;
import ar.edu.itba.paw.persistence.schema.SubcategorySchema;
import ar.edu.itba.paw.persistence.schema.UserSchema;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

@Repository
public class OfferJdbcDao implements OfferDao {

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert jdbcInsert;

    @Autowired
    public OfferJdbcDao(final DataSource ds) {
        jdbcTemplate = new JdbcTemplate(ds);
        jdbcInsert = new SimpleJdbcInsert(ds)
            .usingGeneratedKeyColumns(OfferSchema.ID)
            .withTableName(OfferSchema.TABLE_NAME);
    }

    @Override
    public Optional<Offer> getById(Long id) {
        return jdbcTemplate
            .query(Queries.GET_BY_ID, ROW_MAPPER, id)
            .stream()
            .findFirst();
    }

    @Override
	public Optional<Offer> getByListingAndBuyer(Listing listing, User buyer) {
        return jdbcTemplate
            .query(Queries.GET_BY_LISTING_AND_BUYER_ID, ROW_MAPPER, listing.getId(), buyer.getId())
            .stream()
            .findFirst();
    }

    @Override
	public Page<Offer> search(OfferFilter filter) {
    	final List<String> conditions = new ArrayList<>();
        final List<Object> params = new ArrayList<>();

        if (filter.getBuyerId() != null) {
            conditions.add("o." + OfferSchema.BUYER_ID + " = ?");
            params.add(filter.getBuyerId());
        }

        if (filter.getSellerId() != null) {
            conditions.add("l." + ListingSchema.CREATOR_ID + " = ?");
            params.add(filter.getSellerId());
        }

        if (filter.getStatus() != null) {
            conditions.add("o." + OfferSchema.STATUS + " IN (" + String.join(", ", filter.getStatus().stream().map((s) -> "?").toArray(String[]::new)) + ")");
            params.addAll(filter.getStatus().stream().map((s) -> s.getStatus()).toList());
        }

        final var whereClause = conditions.isEmpty() ? "" : " WHERE " + String.join(" AND ", conditions);

        final long totalCount = jdbcTemplate.queryForObject(
            "SELECT COUNT(*)" + Queries.BASE_FROM + whereClause,
            Long.class,
            params.toArray()
        );

        final int page = filter.getPage();
        final int pageSize = filter.getPageSize();
        final int offset = (page - 1) * pageSize;
        final List<Object> idParams = new ArrayList<>(params);
        idParams.add(pageSize);
        idParams.add(offset);
        final var ids = jdbcTemplate.queryForList(
            "SELECT o." + OfferSchema.ID + Queries.BASE_FROM + whereClause
                + " ORDER BY o." + OfferSchema.CREATED_AT + " DESC"
                + " LIMIT ? OFFSET ?",
            Long.class,
            idParams.toArray()
        );

        if (ids.isEmpty()) {
            return new Page<>(List.of(), page, pageSize, totalCount);
        }

        final var inPlaceholders = String.join(", ", ids.stream().map(id -> "?").toArray(String[]::new));
        final var sql = Queries.BASE_SELECT
            + " WHERE o." + OfferSchema.ID + " IN (" + inPlaceholders + ")"
            + " ORDER BY o." + OfferSchema.CREATED_AT + " DESC";

        final var content = jdbcTemplate.query(sql, ROW_MAPPER, ids.toArray());
        return new Page<>(content, page, pageSize, totalCount);
    }

    @Override
    public Offer create(Long listingId, User buyer, BigDecimal amount, Boolean isFullPrice, OfferStatus status, String message, Instant createdAt, Long offeredListingId) {
        final Map<String, Object> values = new java.util.HashMap<>();
        values.put(OfferSchema.LISTING_ID, listingId);
        values.put(OfferSchema.BUYER_ID, buyer.getId());
        values.put(OfferSchema.AMOUNT, amount);
        values.put(OfferSchema.IS_FULL_PRICE, isFullPrice);
        values.put(OfferSchema.STATUS, status.getStatus());
        values.put(OfferSchema.MESSAGE, message);
        values.put(OfferSchema.CREATED_AT, Timestamp.from(createdAt));
        values.put(OfferSchema.OFFERED_LISTING_ID, offeredListingId);

        final Long key = jdbcInsert.executeAndReturnKey(values).longValue();

        return getById(key).orElseThrow();
    }

    private static final RowMapper<Offer> ROW_MAPPER = (rs, rowNum) -> {
        User buyer = User.builder()
            .id(rs.getLong(UserSchema.ID))
            .username(rs.getString(UserSchema.USERNAME))
            .displayName(rs.getString(UserSchema.DISPLAY_NAME))
            .email(rs.getString(UserSchema.EMAIL))
            .password("<redacted>")
            .imageId(
                    Optional.ofNullable(rs.getObject(UserSchema.IMAGE_ID, Integer.class))
                            .map(Integer::longValue)
                            .orElse(null))
            .joinedAt(rs.getTimestamp(UserSchema.JOINED_AT).toInstant())
            .preferredLanguage(Language.fromCode(rs.getString("buyer_preferred_language")))
            .build();

        String imageIdsStr = rs.getString("image_ids");
        List<Long> imageIds = parseImageIds(imageIdsStr);

        Listing listing = Listing.builder()
            .id(rs.getLong(ListingSchema.ID))
            .title(rs.getString(ListingSchema.TITLE))
            .price(new Price(rs.getBigDecimal(ListingSchema.PRICE)))
            .description(rs.getString(ListingSchema.DESCRIPTION))
            .status(ListingStatus.fromString(rs.getString("listing_status")).orElse(ListingStatus.ACTIVE))
            .condition(Condition.fromString(rs.getString(ListingSchema.CONDITION)).orElse(Condition.GOOD))
            .acceptsTrade(rs.getBoolean(ListingSchema.ACCEPTS_TRADE))
            .creator(
                User.builder()
                    .id(rs.getLong("creator_id"))
                    .username(rs.getString("creator_username"))
                    .displayName(rs.getString("creator_display_name"))
                    .email(rs.getString("creator_email"))
                    .password("<redacted>")
                    .imageId(
                            Optional.ofNullable(rs.getObject("creator_image_id", Integer.class))
                                    .map(Integer::longValue)
                                    .orElse(null))
                    .joinedAt(rs.getTimestamp("creator_joined_at").toInstant())
                    .preferredLanguage(Language.fromCode(rs.getString("creator_preferred_language")))
                    .build()
            )
            .product(
                Product.builder()
                    .id(rs.getLong(ProductSchema.ID))
                    .brand(rs.getString(ProductSchema.BRAND))
                    .model(rs.getString(ProductSchema.MODEL))
                    .year(rs.getInt(ProductSchema.YEAR))
                    .subcategory(
                        Subcategory.builder()
                            .id(rs.getLong(SubcategorySchema.ID))
                            .name(rs.getString(SubcategorySchema.NAME))
                            .category(
                                Category.builder()
                                    .id(rs.getLong(CategorySchema.ID))
                                    .name(rs.getString("category_name"))
                                    .build()
                            )
                            .build()
                    )
                    .build()
            )
            .imageIds(imageIds)
            .build();

        return Offer.builder()
            .id(rs.getLong(OfferSchema.ID))
            .listing(listing)
            .buyer(buyer)
            .amount(rs.getBigDecimal(OfferSchema.AMOUNT))
            .isFullPrice(rs.getBoolean(OfferSchema.IS_FULL_PRICE))
            .status(OfferStatus.fromString(rs.getString(OfferSchema.STATUS)).orElse(OfferStatus.PENDING))
            .message(rs.getString(OfferSchema.MESSAGE))
            .hasOtherOffers(rs.getBoolean("has_other_offers"))
            .hasBetterOffers(rs.getBoolean("has_better_offers"))
            .createdAt(rs.getTimestamp(OfferSchema.CREATED_AT).toInstant())
            .proofOfPayment(buildFileSummary(rs,
                    OfferSchema.PROOF_OF_PAYMENT_ID,
                    OfferSchema.PROOF_OF_PAYMENT_FILENAME,
                    OfferSchema.PROOF_OF_PAYMENT_CONTENT_TYPE,
                    OfferSchema.PROOF_OF_PAYMENT_SIZE))
            .proofOfShipping(buildFileSummary(rs,
                    OfferSchema.PROOF_OF_SHIPPING_ID,
                    OfferSchema.PROOF_OF_SHIPPING_FILENAME,
                    OfferSchema.PROOF_OF_SHIPPING_CONTENT_TYPE,
                    OfferSchema.PROOF_OF_SHIPPING_SIZE))
            .trackingNumber(rs.getString(OfferSchema.TRACKING_NUMBER))
            .sellerRating(
                    Optional.ofNullable(rs.getString(OfferSchema.SELLER_RATING))
                            .flatMap(OfferRating::fromString)
                            .orElse(null))
            .buyerRating(
                    Optional.ofNullable(rs.getString(OfferSchema.BUYER_RATING))
                            .flatMap(OfferRating::fromString)
                            .orElse(null))
            .offeredListing(buildOfferedListing(rs))
            .build();
    };

    /**
     * Builds a {@link File} summary (metadata only, without the blob) from the joined file columns.
     * Returns {@code null} when the offer has no such file attached.
     */
    private static File buildFileSummary(java.sql.ResultSet rs, String idColumn, String filenameColumn,
                                         String contentTypeColumn, String sizeColumn) throws java.sql.SQLException {
        final Integer fileId = rs.getObject(idColumn, Integer.class);
        if (fileId == null) {
            return null;
        }
        return File.builder()
            .id(fileId.longValue())
            .filename(rs.getString(filenameColumn))
            .contentType(rs.getString(contentTypeColumn))
            .size(Optional.ofNullable(rs.getObject(sizeColumn, Integer.class))
                    .map(Integer::longValue)
                    .orElse(null))
            .build();
    }

    private static List<Long> parseImageIds(String imageIdsStr) {
        if (imageIdsStr == null || imageIdsStr.isEmpty()) {
            return List.of();
        }
        String[] parts = imageIdsStr.split(",");
        List<Long> ids = new ArrayList<>(parts.length);
        for (String part : parts) {
            ids.add(Long.parseLong(part.trim()));
        }
        return ids;
    }

    private static Listing buildOfferedListing(java.sql.ResultSet rs) throws java.sql.SQLException {
        Long offeredListingId = rs.getObject("offered_listing_id", Integer.class) != null
                ? rs.getLong("offered_listing_id")
                : null;
        if (offeredListingId == null) {
            return null;
        }

        Long offeredProductId = rs.getObject("offered_product_id", Integer.class) != null
                ? rs.getLong("offered_product_id")
                : null;
        Long offeredSubcategoryId = rs.getObject("offered_subcategory_id", Integer.class) != null
                ? rs.getLong("offered_subcategory_id")
                : null;
        Long offeredCategoryId = rs.getObject("offered_category_id", Integer.class) != null
                ? rs.getLong("offered_category_id")
                : null;
        Long offeredCreatorId = rs.getObject("offered_creator_id", Integer.class) != null
                ? rs.getLong("offered_creator_id")
                : null;
        Long offeredCreatorImageId = rs.getObject("offered_creator_image_id", Integer.class) != null
                ? rs.getLong("offered_creator_image_id")
                : null;
        java.time.Instant offeredCreatorJoinedAt = rs.getTimestamp("offered_creator_joined_at") != null
                ? rs.getTimestamp("offered_creator_joined_at").toInstant()
                : null;

        return Listing.builder()
                .id(offeredListingId)
                .title(rs.getString("offered_listing_title"))
                .price(new Price(rs.getBigDecimal("offered_listing_price")))
                .status(ListingStatus.fromString(rs.getString("offered_listing_status")).orElse(ListingStatus.ACTIVE))
                .condition(Condition.fromString(rs.getString("offered_listing_condition")).orElse(Condition.GOOD))
                .acceptsTrade(rs.getBoolean("offered_listing_accepts_trade"))
                .imageIds(parseImageIds(rs.getString("offered_listing_image_id")))
                .creator(
                        User.builder()
                                .id(offeredCreatorId)
                                .username(rs.getString("offered_creator_username"))
                                .displayName(rs.getString("offered_creator_display_name"))
                                .email(rs.getString("offered_creator_email"))
                                .password("<redacted>")
                                .imageId(offeredCreatorImageId)
                                .joinedAt(offeredCreatorJoinedAt)
                                .preferredLanguage(Language.fromCode(rs.getString("offered_creator_preferred_language")))
                                .build()
                )
                .product(
                        Product.builder()
                                .id(offeredProductId)
                                .brand(rs.getString("offered_product_brand"))
                                .model(rs.getString("offered_product_model"))
                                .year(rs.getObject("offered_product_year", Integer.class))
                                .subcategory(
                                        Subcategory.builder()
                                                .id(offeredSubcategoryId)
                                                .name(rs.getString("offered_subcategory_name"))
                                                .category(
                                                        Category.builder()
                                                                .id(offeredCategoryId)
                                                                .name(rs.getString("offered_category_name"))
                                                                .build()
                                                )
                                                .build()
                                )
                                .build()
                )
                .build();
    }

    private static final class Queries {
private static final String BASE_FROM = " FROM " + OfferSchema.TABLE_NAME + " o" +
 		            " JOIN " + UserSchema.TABLE_NAME + " u ON u." + UserSchema.ID + " = o." + OfferSchema.BUYER_ID +
 		            " JOIN " + ListingSchema.TABLE_NAME + " l ON l." + ListingSchema.ID + " = o." + OfferSchema.LISTING_ID +
 		            " JOIN " + UserSchema.TABLE_NAME + " c ON c." + UserSchema.ID + " = l." + ListingSchema.CREATOR_ID +
 		            " JOIN " + ProductSchema.TABLE_NAME + " p ON p." + ProductSchema.ID + " = l." + ListingSchema.PRODUCT_ID +
 		            " LEFT JOIN " + SubcategorySchema.TABLE_NAME + " s ON s." + SubcategorySchema.ID + " = p." + ProductSchema.SUBCATEGORY_ID +
 		            " LEFT JOIN " + CategorySchema.TABLE_NAME + " cat ON cat." + CategorySchema.ID + " = s." + SubcategorySchema.CATEGORY_ID +
 		            " LEFT JOIN " + ListingSchema.TABLE_NAME + " ol ON ol." + ListingSchema.ID + " = o." + OfferSchema.OFFERED_LISTING_ID +
 		            " LEFT JOIN " + ProductSchema.TABLE_NAME + " op ON op." + ProductSchema.ID + " = ol." + ListingSchema.PRODUCT_ID +
 		            " LEFT JOIN " + SubcategorySchema.TABLE_NAME + " os ON os." + SubcategorySchema.ID + " = op." + ProductSchema.SUBCATEGORY_ID +
 		            " LEFT JOIN " + CategorySchema.TABLE_NAME + " ocat ON ocat." + CategorySchema.ID + " = os." + SubcategorySchema.CATEGORY_ID +
 		            " LEFT JOIN " + UserSchema.TABLE_NAME + " ocl ON ocl." + UserSchema.ID + " = ol." + ListingSchema.CREATOR_ID;

        private static String coverImageIdSubquery(final String listingAlias) {
            return "COALESCE((SELECT CAST(li.image_id AS VARCHAR(20)) FROM listing_images li" +
                " WHERE li.listing_id = " + listingAlias + "." + ListingSchema.ID +
                " AND li.display_order = (SELECT MIN(li2.display_order) FROM listing_images li2" +
                " WHERE li2.listing_id = " + listingAlias + "." + ListingSchema.ID + ")), '')";
        }

		private static final String BASE_SELECT =
            "SELECT o." + OfferSchema.ID + ", o." + OfferSchema.LISTING_ID + ", o." + OfferSchema.BUYER_ID +
            ", o." + OfferSchema.AMOUNT + ", o." + OfferSchema.IS_FULL_PRICE + ", o." + OfferSchema.STATUS +
            ", o." + OfferSchema.MESSAGE + ", o." + OfferSchema.CREATED_AT +
            ", o." + OfferSchema.PROOF_OF_PAYMENT_ID +
            ", f." + FileSchema.FILENAME + " as " + OfferSchema.PROOF_OF_PAYMENT_FILENAME +
            ", f." + FileSchema.CONTENT_TYPE + " as " + OfferSchema.PROOF_OF_PAYMENT_CONTENT_TYPE +
            ", OCTET_LENGTH(f." + FileSchema.DATA + ") as " + OfferSchema.PROOF_OF_PAYMENT_SIZE +
            ", o." + OfferSchema.PROOF_OF_SHIPPING_ID +
            ", fs." + FileSchema.FILENAME + " as " + OfferSchema.PROOF_OF_SHIPPING_FILENAME +
            ", fs." + FileSchema.CONTENT_TYPE + " as " + OfferSchema.PROOF_OF_SHIPPING_CONTENT_TYPE +
            ", OCTET_LENGTH(fs." + FileSchema.DATA + ") as " + OfferSchema.PROOF_OF_SHIPPING_SIZE +
            ", o." + OfferSchema.TRACKING_NUMBER +
            ", o." + OfferSchema.SELLER_RATING +
            ", o." + OfferSchema.BUYER_RATING +
            ", o." + OfferSchema.OFFERED_LISTING_ID +
            ", u." + UserSchema.ID + ", u." + UserSchema.USERNAME + ", u." + UserSchema.DISPLAY_NAME +
            ", u." + UserSchema.EMAIL + ", u." + UserSchema.IMAGE_ID + ", u." + UserSchema.JOINED_AT +
            ", u." + UserSchema.PREFERRED_LANGUAGE + " as buyer_preferred_language" +
            ", l." + ListingSchema.ID + ", l." + ListingSchema.TITLE + ", l." + ListingSchema.DESCRIPTION +
            ", l." + ListingSchema.PRICE + ", l." + ListingSchema.STATUS + " as listing_status, l." + ListingSchema.CONDITION +
            ", l." + ListingSchema.ACCEPTS_TRADE + ", l." + ListingSchema.CREATOR_ID + ", l." + ListingSchema.PRODUCT_ID +
            ", c." + UserSchema.ID + " as creator_id, c." + UserSchema.USERNAME + " as creator_username" +
            ", c." + UserSchema.DISPLAY_NAME + " as creator_display_name, c." + UserSchema.EMAIL + " as creator_email" +
            ", c." + UserSchema.IMAGE_ID + " as creator_image_id" + ", c." + UserSchema.JOINED_AT + " as creator_joined_at" +
            ", c." + UserSchema.PREFERRED_LANGUAGE + " as creator_preferred_language" +
            ", p." + ProductSchema.ID + ", p." + ProductSchema.BRAND + ", p." + ProductSchema.MODEL +
            ", p." + ProductSchema.YEAR + ", p." + ProductSchema.SUBCATEGORY_ID +
            ", s." + SubcategorySchema.ID + ", s." + SubcategorySchema.NAME +
            ", cat." + CategorySchema.ID + ", cat." + CategorySchema.NAME + " as category_name" +
            ", ol." + ListingSchema.ID + " as offered_listing_id" +
            ", ol." + ListingSchema.TITLE + " as offered_listing_title" +
            ", ol." + ListingSchema.PRICE + " as offered_listing_price" +
            ", ol." + ListingSchema.STATUS + " as offered_listing_status" +
            ", ol." + ListingSchema.CONDITION + " as offered_listing_condition" +
            ", ol." + ListingSchema.ACCEPTS_TRADE + " as offered_listing_accepts_trade" +
            ", ol." + ListingSchema.CREATOR_ID + " as offered_listing_creator_id" +
            ", ol." + ListingSchema.PRODUCT_ID + " as offered_listing_product_id" +
            ", op." + ProductSchema.ID + " as offered_product_id" +
            ", op." + ProductSchema.BRAND + " as offered_product_brand" +
            ", op." + ProductSchema.MODEL + " as offered_product_model" +
            ", op." + ProductSchema.YEAR + " as offered_product_year" +
            ", os." + SubcategorySchema.ID + " as offered_subcategory_id" +
            ", os." + SubcategorySchema.NAME + " as offered_subcategory_name" +
            ", ocat." + CategorySchema.ID + " as offered_category_id" +
            ", ocat." + CategorySchema.NAME + " as offered_category_name" +
            ", ocl." + UserSchema.ID + " as offered_creator_id" +
            ", ocl." + UserSchema.USERNAME + " as offered_creator_username" +
            ", ocl." + UserSchema.DISPLAY_NAME + " as offered_creator_display_name" +
            ", ocl." + UserSchema.EMAIL + " as offered_creator_email" +
            ", ocl." + UserSchema.PREFERRED_LANGUAGE + " as offered_creator_preferred_language" +
            ", ocl." + UserSchema.IMAGE_ID + " as offered_creator_image_id" +
            ", ocl." + UserSchema.JOINED_AT + " as offered_creator_joined_at" +
            ", " + coverImageIdSubquery("l") + " as image_ids" +
            ", " + coverImageIdSubquery("ol") + " as offered_listing_image_id" +
            ", EXISTS(SELECT 1 FROM " + OfferSchema.TABLE_NAME + " o2 " +
            " WHERE o2." + OfferSchema.LISTING_ID + " = o." + OfferSchema.LISTING_ID +
            " AND o2." + OfferSchema.ID + " != o." + OfferSchema.ID +
            " AND o2." + OfferSchema.STATUS + " = '" + OfferStatus.PENDING.getStatus() + "') as has_other_offers" +
            ", EXISTS(SELECT 1 FROM " + OfferSchema.TABLE_NAME + " o3 " +
            " WHERE o3." + OfferSchema.LISTING_ID + " = o." + OfferSchema.LISTING_ID +
            " AND o3." + OfferSchema.ID + " != o." + OfferSchema.ID +
            " AND o3." + OfferSchema.AMOUNT + " > o." + OfferSchema.AMOUNT +
            " AND o3." + OfferSchema.STATUS + " = '" + OfferStatus.PENDING.getStatus() + "') as has_better_offers" +
            BASE_FROM
            + " LEFT JOIN " + FileSchema.TABLE_NAME + " f ON f." + FileSchema.ID + " = o." + OfferSchema.PROOF_OF_PAYMENT_ID
            + " LEFT JOIN " + FileSchema.TABLE_NAME + " fs ON fs." + FileSchema.ID + " = o." + OfferSchema.PROOF_OF_SHIPPING_ID;

        private static final String GET_BY_ID =
            BASE_SELECT +
            " WHERE o." + OfferSchema.ID + " = ?";

        private static final String GET_BY_LISTING_AND_BUYER_ID =
            BASE_SELECT +
            " WHERE o." + OfferSchema.LISTING_ID + " = ?" +
            " AND o." + OfferSchema.BUYER_ID + " = ?" +
            " AND o." + OfferSchema.STATUS + " IN ('" + OfferStatus.PENDING.getStatus() + "', '" + OfferStatus.PENDING_PAYMENT.getStatus() + "')" +
            " ORDER BY o." + OfferSchema.ID + " DESC";

        private static final String UPDATE_STATUS =
            "UPDATE " + OfferSchema.TABLE_NAME +
            " SET " + OfferSchema.STATUS + " = ?" +
            " WHERE " + OfferSchema.ID + " = ?";

        private static final String UPDATE_PROOF_OF_PAYMENT_ID =
            "UPDATE " + OfferSchema.TABLE_NAME +
            " SET " + OfferSchema.PROOF_OF_PAYMENT_ID + " = ?" +
            " WHERE " + OfferSchema.ID + " = ?";

        private static final String UPDATE_PROOF_OF_SHIPPING =
            "UPDATE " + OfferSchema.TABLE_NAME +
            " SET " + OfferSchema.PROOF_OF_SHIPPING_ID + " = ?" +
            ", " + OfferSchema.TRACKING_NUMBER + " = ?" +
            " WHERE " + OfferSchema.ID + " = ?";

        private static final String WITHDRAW =
            "UPDATE " + OfferSchema.TABLE_NAME +
            " SET " + OfferSchema.STATUS + " = ?" +
            " WHERE " + OfferSchema.ID + " = ?" +
            " AND " + OfferSchema.BUYER_ID + " = ?" +
            " AND " + OfferSchema.STATUS + " = ?";

        private static final String GET_PENDING_TO_REJECT =
            BASE_SELECT +
            " WHERE o." + OfferSchema.LISTING_ID + " = ?" +
            " AND o." + OfferSchema.STATUS + " = ?" +
            " AND (CAST(? AS BIGINT) IS NULL OR o." + OfferSchema.ID + " != ?)";

        private static final String REJECT_PENDING =
            "UPDATE " + OfferSchema.TABLE_NAME +
            " SET " + OfferSchema.STATUS + " = ?" +
            " WHERE " + OfferSchema.LISTING_ID + " = ?" +
            " AND " + OfferSchema.STATUS + " = ?" +
            " AND (CAST(? AS BIGINT) IS NULL OR " + OfferSchema.ID + " != ?)";

        private static final String MARK_ACCEPTED =
            "UPDATE " + OfferSchema.TABLE_NAME +
            " SET " + OfferSchema.STATUS + " = ?" +
            ", " + OfferSchema.ACCEPTED_AT + " = ?" +
            " WHERE " + OfferSchema.ID + " = ?";

        private static final String SET_SELLER_RATING =
            "UPDATE " + OfferSchema.TABLE_NAME +
            " SET " + OfferSchema.SELLER_RATING + " = ?" +
            " WHERE " + OfferSchema.ID + " = ?" +
            " AND " + OfferSchema.SELLER_RATING + " IS NULL";

        private static final String SET_BUYER_RATING =
            "UPDATE " + OfferSchema.TABLE_NAME +
            " SET " + OfferSchema.BUYER_RATING + " = ?" +
            " WHERE " + OfferSchema.ID + " = ?" +
            " AND " + OfferSchema.BUYER_RATING + " IS NULL";
        
        private static final String GET_ACCEPTED_UNRATED_BEFORE =
            BASE_SELECT +
            " WHERE o." + OfferSchema.STATUS + " = ?" +
            " AND o." + OfferSchema.ACCEPTED_AT + " IS NOT NULL" +
            " AND o." + OfferSchema.ACCEPTED_AT + " < ?" +
            " AND (o." + OfferSchema.SELLER_RATING + " IS NULL OR o." + OfferSchema.BUYER_RATING + " IS NULL)";
    }

    @Override
    public boolean updateStatus(Long offerId, OfferStatus status) {
        return jdbcTemplate.update(Queries.UPDATE_STATUS, status.getStatus(), offerId) > 0;
    }

    @Override
    public boolean withdraw(Long offerId, Long buyerId) {
        return jdbcTemplate.update(
            Queries.WITHDRAW,
            OfferStatus.WITHDRAWN.getStatus(),
            offerId,
            buyerId,
            OfferStatus.PENDING.getStatus()
        ) > 0;
    }

    @Override
    public List<Offer> rejectPendingOffers(Long listingId, Long exceptOfferId) {
        final List<Offer> toReject = jdbcTemplate.query(
            Queries.GET_PENDING_TO_REJECT,
            ROW_MAPPER,
            listingId, OfferStatus.PENDING.getStatus(), exceptOfferId, exceptOfferId
        );

        jdbcTemplate.update(
            Queries.REJECT_PENDING,
            OfferStatus.REJECTED.getStatus(), listingId, OfferStatus.PENDING.getStatus(), exceptOfferId, exceptOfferId
        );

        return toReject;
    }

    @Override
    public int countPendingBySeller(User seller) {
        final String sql = "SELECT COUNT(*) FROM " + OfferSchema.TABLE_NAME + " o" +
                " JOIN " + ListingSchema.TABLE_NAME + " l ON l." + ListingSchema.ID + " = o." + OfferSchema.LISTING_ID +
                " WHERE l." + ListingSchema.CREATOR_ID + " = ?" +
                " AND o." + OfferSchema.STATUS + " = ?";
        return jdbcTemplate.queryForObject(sql, Integer.class, seller.getId(), OfferStatus.PENDING.getStatus());
    }

    @Override
    public boolean updateProofOfPaymentId(Long offerId, Long proofOfPaymentId) {
        return jdbcTemplate.update(Queries.UPDATE_PROOF_OF_PAYMENT_ID, proofOfPaymentId, offerId) > 0;
    }

    @Override
    public boolean updateProofOfShipping(Long offerId, Long proofOfShippingId, String trackingNumber) {
        return jdbcTemplate.update(Queries.UPDATE_PROOF_OF_SHIPPING, proofOfShippingId, trackingNumber, offerId) > 0;
    }

    @Override
    public boolean markAccepted(Long offerId, Instant acceptedAt) {
        return jdbcTemplate.update(
            Queries.MARK_ACCEPTED,
            OfferStatus.ACCEPTED.getStatus(),
            Timestamp.from(acceptedAt),
            offerId
        ) > 0;
    }

    @Override
    public boolean setSellerRating(Long offerId, OfferRating rating) {
        return jdbcTemplate.update(Queries.SET_SELLER_RATING, rating.getRating(), offerId) > 0;
    }

    @Override
    public boolean setBuyerRating(Long offerId, OfferRating rating) {
        return jdbcTemplate.update(Queries.SET_BUYER_RATING, rating.getRating(), offerId) > 0;
    }

    @Override
    public List<Offer> getAcceptedUnratedBefore(Instant cutoff) {
        return jdbcTemplate.query(
            Queries.GET_ACCEPTED_UNRATED_BEFORE,
            ROW_MAPPER,
            OfferStatus.ACCEPTED.getStatus(),
            Timestamp.from(cutoff)
        );
    }
}
