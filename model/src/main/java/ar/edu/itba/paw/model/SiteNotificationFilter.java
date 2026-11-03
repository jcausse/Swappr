package ar.edu.itba.paw.model;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public final class SiteNotificationFilter {

    private final Long userId;
    private final boolean unreadOnly;
    private final NotificationType type;
    private final int page;
    private final int pageSize;
}
