package ar.edu.itba.paw.model.notification;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public final class SiteNotificationFilter {

    private final Long userId;
    private final boolean unreadOnly;

    /**
     * Set to null to get notifications regardless of their type.
     */
    private final NotificationType type;

    private final int page;
    private final int pageSize;
}
