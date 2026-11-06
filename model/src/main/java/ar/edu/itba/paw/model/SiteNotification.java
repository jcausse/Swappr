package ar.edu.itba.paw.model;

import lombok.*;

import javax.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"user", "bodyArgs", "offer"})
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "site_notifications")
public class SiteNotification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notification_id", nullable = false)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "offer_id")
    private Offer offer;

    @Enumerated(EnumType.STRING)
    @Column(name = "notification_type", nullable = false, length = 50)
    private NotificationType type;

    @Column(name = "title_key", nullable = false, length = 100)
    private String titleKey;

    @Column(name = "body_key", nullable = false, length = 100)
    private String bodyKey;

    @Builder.Default
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "site_notification_args", joinColumns = @JoinColumn(name = "notification_id"))
    @OrderColumn(name = "arg_index")
    @Column(name = "arg_value", nullable = false)
    private List<String> bodyArgs = new ArrayList<>();

    @Column(name = "action_url")
    private String actionUrl;

    @Column(name = "action_label_key", length = 100)
    private String actionLabelKey;

    @Column(name = "is_read", nullable = false)
    @Builder.Default
    private boolean read = false;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    // -------------------------------------------------------------------------------------------------
    // Domain Optionals & State Mutators

    public List<String> getBodyArgs() {
        return bodyArgs != null ? Collections.unmodifiableList(bodyArgs) : Collections.emptyList();
    }

    public Optional<Offer> getOffer() {
        return Optional.ofNullable(offer);
    }

    public Optional<String> getActionUrl() {
        return Optional.ofNullable(actionUrl);
    }

    public Optional<String> getActionLabelKey() {
        return Optional.ofNullable(actionLabelKey);
    }

    public void markAsRead() {
        this.read = true;
    }

    public boolean isRecipient(User candidate) {
        // Called on the candidate so the lazy recipient proxy only exposes its ID (no extra fetch)
        return candidate != null && candidate.isSameUser(user);
    }

    public boolean showCallToAction() {
        if (type == null) {
            return true;
        }
        return type.shouldShowCallToAction(offer);
    }

    public boolean isShowCallToAction() {
        return showCallToAction();
    }
}
