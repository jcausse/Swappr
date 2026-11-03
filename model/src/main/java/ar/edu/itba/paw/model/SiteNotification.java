package ar.edu.itba.paw.model;

import lombok.*;

import javax.persistence.*;
import java.time.Instant;
import java.util.Optional;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "user")
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

    @Enumerated(EnumType.STRING)
    @Column(name = "notification_type", nullable = false, length = 50)
    private NotificationType type;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "body", nullable = false)
    private String body;

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

    public Optional<String> getActionUrl() {
        return Optional.ofNullable(actionUrl);
    }

    public Optional<String> getActionLabelKey() {
        return Optional.ofNullable(actionLabelKey);
    }

    public void markAsRead() {
        this.read = true;
    }
}
