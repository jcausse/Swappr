package ar.edu.itba.paw.model;

import lombok.*;

import javax.persistence.*;
import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"creator", "rated", "offer"})
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "ratings")
public class Rating {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "rating_id", nullable = false)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "creator_id", nullable = false)
    private User creator;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "rated_id", nullable = false)
    private User rated;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "offer_id", nullable = false)
    private Offer offer;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 10)
    private RatingRole role;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 10)
    private OfferRating type;

    @Column(name = "review_text", nullable = false)
    private String reviewText;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    // -------------------------------------------------------------------------------------------------

    public Listing getListing() {
        return offer != null ? offer.getListing() : null;
    }
}
