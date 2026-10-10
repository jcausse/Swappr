package ar.edu.itba.paw.model;

import lombok.*;

import javax.persistence.*;
import java.time.Instant;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"password", "emailVerifiedAt"})
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id", nullable = false)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "username", nullable = false, length = 100)
    private String username;

    @Column(name = "display_name", nullable = false, length = 100)
    private String displayName;

    @Column(name = "email", nullable = false, length = 254)
    private String email;

    @Column(name = "password", nullable = false)
    private String password;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "image_id")
    private Image image;

    @Column(name = "joined_at", nullable = false)
    private Instant joinedAt;

    @Column(name = "email_verified_at")
    private Instant emailVerifiedAt;

    @Builder.Default
    @Column(name = "seller_positive_ratings", nullable = false)
    private Integer sellerPositiveRatings = 0;

    @Builder.Default
    @Column(name = "seller_neutral_ratings", nullable = false)
    private Integer sellerNeutralRatings = 0;

    @Builder.Default
    @Column(name = "seller_negative_ratings", nullable = false)
    private Integer sellerNegativeRatings = 0;

    @Builder.Default
    @Column(name = "buyer_positive_ratings", nullable = false)
    private Integer buyerPositiveRatings = 0;

    @Builder.Default
    @Column(name = "buyer_neutral_ratings", nullable = false)
    private Integer buyerNeutralRatings = 0;

    @Builder.Default
    @Column(name = "buyer_negative_ratings", nullable = false)
    private Integer buyerNegativeRatings = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "province_id")
    private Province province;

    @Column(name = "location_detail", length = 100)
    private String locationDetail;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "preferred_language", nullable = false, length = 8)
    private Language preferredLanguage = Language.ENGLISH;

    @Column(name = "suspended_at")
    private Instant suspendedAt;

    @Builder.Default
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "users_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "role_name")
    @Enumerated(EnumType.STRING)
    private Set<Role> roles = new HashSet<>();

    // -------------------------------------------------------------------------------------------------

    public Optional<Image> getImage() {
        return Optional.ofNullable(image);
    }

    public Optional<Long> getImageId() {
        return Optional.ofNullable(image != null ? image.getId() : null);
    }

    public Optional<Instant> getEmailVerifiedAt() {
        return Optional.ofNullable(emailVerifiedAt);
    }

    public boolean isVerified() {
        return emailVerifiedAt != null;
    }

    public Optional<Instant> getSuspendedAt() {
        return Optional.ofNullable(suspendedAt);
    }

    public boolean isSuspended() {
        return suspendedAt != null;
    }

    public Optional<Province> getProvince() {
        return Optional.ofNullable(province);
    }

    public Optional<String> getLocationDetail() {
        return Optional.ofNullable(locationDetail);
    }

    public int getSellerTotalRatings() {
        int pos = sellerPositiveRatings != null ? sellerPositiveRatings : 0;
        int neu = sellerNeutralRatings != null ? sellerNeutralRatings : 0;
        int neg = sellerNegativeRatings != null ? sellerNegativeRatings : 0;
        return pos + neu + neg;
    }

    public int getBuyerTotalRatings() {
        int pos = buyerPositiveRatings != null ? buyerPositiveRatings : 0;
        int neu = buyerNeutralRatings != null ? buyerNeutralRatings : 0;
        int neg = buyerNegativeRatings != null ? buyerNegativeRatings : 0;
        return pos + neu + neg;
    }

    public int getSellerRatingBalance() {
        int pos = sellerPositiveRatings != null ? sellerPositiveRatings : 0;
        int neg = sellerNegativeRatings != null ? sellerNegativeRatings : 0;
        return pos - neg;
    }

    public int getBuyerRatingBalance() {
        int pos = buyerPositiveRatings != null ? buyerPositiveRatings : 0;
        int neg = buyerNegativeRatings != null ? buyerNegativeRatings : 0;
        return pos - neg;
    }

    // TODO: [JPA Migration Cleanup] Transitional builder method for JDBC backward compatibility.
    // Remove imageId adapter once UserDao is migrated to JPA.
    public static class UserBuilder {
        public UserBuilder imageId(Long imageId) {
            if (imageId != null) {
                this.image = Image.builder().id(imageId).build();
            }
            return this;
        }
    }
}
