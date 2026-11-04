package ar.edu.itba.paw.service.dto;

import ar.edu.itba.paw.model.NotificationType;
import ar.edu.itba.paw.model.User;
import lombok.Builder;
import lombok.NonNull;

import java.util.List;

@Builder
public record SiteNotificationCreationDto(
    @NonNull User notificationRecipient,
    @NonNull NotificationType notificationType,
    @NonNull String titleKey,
    @NonNull String bodyKey,
    List<String> bodyArgs,
    String actionUrl,
    String actionLabelKey
) {
    public SiteNotificationCreationDto {
        bodyArgs = bodyArgs != null ? List.copyOf(bodyArgs) : List.of();
    }
}
