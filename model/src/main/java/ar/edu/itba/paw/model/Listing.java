package ar.edu.itba.paw.model;

import lombok.*;
import org.hibernate.annotations.Formula;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"images", "creator", "product"})
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "listings")
public class Listing {

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    @Column(name = "price", nullable = false, precision = 100, scale = 2)
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ListingStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "condition", nullable = false, length = 20)
    private Condition condition;

    @Column(name = "accepts_trade", nullable = false)
    private Boolean acceptsTrade;

    @Column(name = "accepts_shipping", nullable = false)
    private Boolean acceptsShipping;

    @OneToMany
    @JoinTable(
        name = "listing_images",
        joinColumns = @JoinColumn(name = "listing_id"),
        inverseJoinColumns = @JoinColumn(name = "image_id")
    )
    @OrderColumn(name = "display_order")
    private List<Image> images = new ArrayList<>();

    // TODO: Move the query to a DAO but leave the field
    @Formula("(SELECT COUNT(*) FROM offers o WHERE o.listing_id = listing_id AND o.status = 'PENDING')")
    private int pendingOffersCount;

    // TODO: Move the query to a DAO but leave the field
    @Formula("(SELECT MAX(o.created_at) FROM offers o WHERE o.listing_id = listing_id AND o.status = 'PENDING')")
    private Instant recentPendingOfferAt;

    // -------------------------------------------------------------------------------------------------

    public Price getPrice() {
        return price != null ? new Price(price) : null;
    }

    public void setPrice(Price price) {
        this.price = price != null ? price.getAmount() : null;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    // TODO: Remove after creating DAO
    public List<Long> getImageIds() {
        if (images == null) {
            return Collections.emptyList();
        }
        return images.stream().map(Image::getId).toList();
    }

    public boolean isAcceptsTrade() {
        return Boolean.TRUE.equals(acceptsTrade);
    }

    public boolean isAcceptsShipping() {
        return Boolean.TRUE.equals(acceptsShipping);
    }
}
