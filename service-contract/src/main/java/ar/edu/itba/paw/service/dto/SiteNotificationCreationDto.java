package ar.edu.itba.paw.service.dto;

import ar.edu.itba.paw.model.NotificationType;
import ar.edu.itba.paw.model.User;
import lombok.Builder;
import lombok.NonNull;

@Builder
public record SiteNotificationCreationDto (
    @NonNull User notificationRecipient,
    @NonNull NotificationType notificationType,
    @NonNull String title,
    @NonNull String body,
    String actionUrl,
    String actionLabelKey
){}
