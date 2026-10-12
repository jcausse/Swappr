package ar.edu.itba.paw.model;

import lombok.*;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"listing", "buyer", "proofOfPayment", "proofOfShipping", "offeredListing"})
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "offers")
public class Offer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "offer_id", nullable = false)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "listing_id", nullable = false)
    private Listing listing;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "buyer_id", nullable = false)
    private User buyer;

    @Column(name = "amount", nullable = false, precision = 100, scale = 2)
    private BigDecimal amount;

    @Column(name = "is_full_price", nullable = false)
    private Boolean isFullPrice;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private OfferStatus status;

    @Column(name = "message")
    private String message;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proof_of_payment_id")
    private File proofOfPayment;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proof_of_shipping_id")
    private File proofOfShipping;

    @Column(name = "tracking_number")
    private String trackingNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "offered_listing_id")
    private Listing offeredListing;

    @Column(name = "accepted_at")
    private Instant acceptedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "buyer_rating", length = 10)
    private OfferRating buyerRating;

    @Enumerated(EnumType.STRING)
    @Column(name = "seller_rating", length = 10)
    private OfferRating sellerRating;

    @Transient
    private boolean hasOtherOffers;

    @Transient
    private boolean hasBetterOffers;

    // -------------------------------------------------------------------------------------------------

    public Optional<File> getProofOfPayment() {
        return Optional.ofNullable(proofOfPayment);
    }

    public Optional<File> getProofOfShipping() {
        return Optional.ofNullable(proofOfShipping);
    }

    public Optional<Listing> getOfferedListing() {
        return Optional.ofNullable(offeredListing);
    }

    public Optional<Instant> getAcceptedAt() {
        return Optional.ofNullable(acceptedAt);
    }

    public Optional<OfferRating> getBuyerRating() {
        return Optional.ofNullable(buyerRating);
    }

    public Optional<OfferRating> getSellerRating() {
        return Optional.ofNullable(sellerRating);
    }

    // -- Transitional getters for Service / Webapp compatibility --
    // TODO: [JPA Migration Cleanup] Transitional getters for compatibility.
    public Long getOfferedListingId() {
        return offeredListing != null ? offeredListing.getId() : null;
    }

    public Long getProofOfPaymentId() {
        return proofOfPayment != null ? proofOfPayment.getId() : null;
    }

    public String getProofOfPaymentFilename() {
        return proofOfPayment != null ? proofOfPayment.getFilename() : null;
    }

    public Long getProofOfPaymentSizeKb() {
        if (proofOfPayment == null || proofOfPayment.getSize() <= 0) return 0L;
        return proofOfPayment.getSize() / 1024;
    }

    public boolean proofOfPaymentIsImage() {
        return proofOfPayment != null && proofOfPayment.getContentType().map(ct -> ct.startsWith("image/")).orElse(false);
    }

    public String getProofOfPaymentExtension() {
        if (proofOfPayment == null || proofOfPayment.getFilename() == null || proofOfPayment.getFilename().isBlank()) return "";
        String filename = proofOfPayment.getFilename();
        int lastDot = filename.lastIndexOf('.');
        if (lastDot == -1 || lastDot == filename.length() - 1) return "";
        return filename.substring(lastDot + 1).toLowerCase();
    }

    public Long getProofOfShippingId() {
        return proofOfShipping != null ? proofOfShipping.getId() : null;
    }

    public String getProofOfShippingFilename() {
        return proofOfShipping != null ? proofOfShipping.getFilename() : null;
    }

    public Long getProofOfShippingSizeKb() {
        if (proofOfShipping == null || proofOfShipping.getSize() <= 0) return 0L;
        return proofOfShipping.getSize() / 1024;
    }

    public boolean proofOfShippingIsImage() {
        return proofOfShipping != null && proofOfShipping.getContentType().map(ct -> ct.startsWith("image/")).orElse(false);
    }

    public String getProofOfShippingExtension() {
        if (proofOfShipping == null || proofOfShipping.getFilename() == null || proofOfShipping.getFilename().isBlank()) return "";
        String filename = proofOfShipping.getFilename();
        int lastDot = filename.lastIndexOf('.');
        if (lastDot == -1 || lastDot == filename.length() - 1) return "";
        return filename.substring(lastDot + 1).toLowerCase();
    }
}
