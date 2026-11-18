package ar.edu.itba.paw.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.time.Instant;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"password", "emailVerifiedAt", "image", "province", "roles"})
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
    private int sellerPositiveRatings = 0;

    @Builder.Default
    @Column(name = "seller_neutral_ratings", nullable = false)
    private int sellerNeutralRatings = 0;

    @Builder.Default
    @Column(name = "seller_negative_ratings", nullable = false)
    private int sellerNegativeRatings = 0;

    @Builder.Default
    @Column(name = "buyer_positive_ratings", nullable = false)
    private int buyerPositiveRatings = 0;

    @Builder.Default
    @Column(name = "buyer_neutral_ratings", nullable = false)
    private int buyerNeutralRatings = 0;

    @Builder.Default
    @Column(name = "buyer_negative_ratings", nullable = false)
    private int buyerNegativeRatings = 0;

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
    @ElementCollection(fetch = FetchType.LAZY)
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

    public boolean isVerified() {
        return emailVerifiedAt != null;
    }

    public boolean isSuspended() {
        return suspendedAt != null;
    }

    public boolean isSameUser(User other) {
        return other != null && id != null && id.equals(other.getId());
    }

    public boolean matchesUsernameOrEmail(String usernameOrEmail) {
        if (usernameOrEmail == null) {
            return false;
        }
        final String normalized = usernameOrEmail.trim();
        return username.equalsIgnoreCase(normalized) || email.equalsIgnoreCase(normalized);
    }

    public boolean hasRole(Role role) {
        return role != null && roles.contains(role);
    }

    public Language getPreferredLanguageOrDefault() {
        return preferredLanguage != null ? preferredLanguage : Language.getDefault();
    }

    public Optional<Province> getProvince() {
        return Optional.ofNullable(province);
    }

    public Optional<String> getLocationDetail() {
        return Optional.ofNullable(locationDetail);
    }

    public int getSellerTotalRatings() {
        return sellerPositiveRatings + sellerNeutralRatings + sellerNegativeRatings;
    }

    public int getBuyerTotalRatings() {
        return buyerPositiveRatings + buyerNeutralRatings + buyerNegativeRatings;
    }

    public int getSellerRatingBalance() {
        return sellerPositiveRatings - sellerNegativeRatings;
    }

    public int getBuyerRatingBalance() {
        return buyerPositiveRatings - buyerNegativeRatings;
    }
}
