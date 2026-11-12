package ar.edu.itba.paw.model.notification;

import ar.edu.itba.paw.model.User;
import lombok.*;

import javax.persistence.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "user")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "user_notification_preferences")
public class UserNotificationPreference {

    @EmbeddedId
    @EqualsAndHashCode.Include
    private UserNotificationPreferenceId id;

    @MapsId("userId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "is_enabled", nullable = false)
    private boolean enabled;
}
