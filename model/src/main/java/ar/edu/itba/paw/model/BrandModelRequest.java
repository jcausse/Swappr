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
@ToString(exclude = {"brand", "subcategory", "requester"})
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "brand_model_requests")
public class BrandModelRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "request_id", nullable = false)
    @EqualsAndHashCode.Include
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "request_type", nullable = false, length = 20)
    private BrandModelRequestType type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "brand_id")
    private Brand brand;

    @Column(name = "brand_name", length = 100)
    private String brandName;

    @Column(name = "model_name", nullable = false, length = 100)
    private String modelName;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "subcategory_id", nullable = false)
    private Subcategory subcategory;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "requester_id", nullable = false)
    private User requester;

    @Builder.Default
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "brand_model_request_additional_users",
        joinColumns = @JoinColumn(name = "request_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> additionalRequesters = new HashSet<>();

    @Builder.Default
    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    public Optional<Brand> getBrandOptional() {
        return Optional.ofNullable(brand);
    }

    public Long getBrandId() {
        return brand != null ? brand.getId() : null;
    }

    public boolean isNewBrand() {
        return type == BrandModelRequestType.NEW_BRAND;
    }

    public String getRequestedBrandName() {
        return brand != null ? brand.getName() : brandName;
    }

    public int getAdditionalRequesterCount() {
        return additionalRequesters == null ? 0 : additionalRequesters.size();
    }
}
