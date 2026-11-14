package ar.edu.itba.paw.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.Formula;

import javax.persistence.AttributeOverride;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.ElementCollection;
import javax.persistence.CollectionTable;
import javax.persistence.OrderColumn;
import javax.persistence.Table;
import java.time.Instant;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"imageIds", "creator", "model"})
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "listings")
public class Listing {

    public static final int MIN_YEAR = 1900;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "listing_id", nullable = false)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id")
    private User creator;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "model_id", nullable = false)
    private Model model;

    @Column(name = "year", nullable = false)
    private int year;

    @Embedded
    @AttributeOverride(name = "amount", column = @Column(name = "price", nullable = false, precision = 100, scale = 2))
    private Price price;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ListingStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "condition", nullable = false, length = 20)
    private Condition condition;

    @Column(name = "accepts_trade", nullable = false)
    private boolean acceptsTrade;

    @Column(name = "accepts_shipping", nullable = false)
    private boolean acceptsShipping;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
        name = "listing_images",
        joinColumns = @JoinColumn(name = "listing_id")
    )
    @Column(name = "image_id", nullable = false)
    @OrderColumn(name = "display_order")
    @Builder.Default
    private List<Long> imageIds = new ArrayList<>();

    @Formula("(SELECT li.image_id FROM listing_images li WHERE li.listing_id = listing_id AND li.display_order = 0)")
    private Long coverImageId;

    @Formula("(SELECT COUNT(*) FROM offers o WHERE o.listing_id = listing_id AND o.status = 'PENDING')")
    private int pendingOffersCount;

    @Formula("(SELECT MAX(o.created_at) FROM offers o WHERE o.listing_id = listing_id AND o.status = 'PENDING')")
    private Instant recentPendingOfferAt;

    // -------------------------------------------------------------------------------------------------

    public boolean isActive() {
        return status == ListingStatus.ACTIVE;
    }

    public boolean isPendingTransaction() {
        return status == ListingStatus.PENDING_TRANSACTION;
    }

    public boolean isSoldForDisplay() {
        return status == ListingStatus.SOLD
            || status == ListingStatus.PENDING_TRANSACTION
            || status == ListingStatus.OFFERED_IN_TRADE;
    }

    public boolean isCanceled() {
        return status == ListingStatus.CANCELED;
    }

    public boolean canBeCanceledByAdmin() {
        return status != ListingStatus.CANCELED && status != ListingStatus.SOLD;
    }

    public static boolean isValidYear(final int year) {
        return year >= MIN_YEAR && year <= Year.now().getValue();
    }

    public boolean isFree() {
        return price != null && price.getAmount().signum() == 0;
    }

    public boolean isCreator(Long userId) {
        return userId != null && userId.equals(creator.getId());
    }

    public boolean isCreator(User user) {
        return user != null && isCreator(user.getId());
    }

    public Brand getBrand() {
        return model != null ? model.getBrand() : null;
    }
}
