package ar.edu.itba.paw.webapp.form;

import ar.edu.itba.paw.model.notification.NotificationType;
import ar.edu.itba.paw.model.notification.SiteNotificationFilter;
import ar.edu.itba.paw.model.User;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;

import javax.validation.constraints.Min;

@NoArgsConstructor
@Getter
@Setter
public class NotificationFilterForm {

    private static final int DEFAULT_PAGE_SIZE = 10;

    private boolean unreadOnly = false;
    private NotificationType type;
    @Min(1)
    private Integer page = 1;

    public int getPageOrDefault() {
        return page != null && page >= 1 ? page : 1;
    }

    public SiteNotificationFilter toFilter(@NonNull final User recipient) {
        return SiteNotificationFilter.builder()
                .userId(recipient.getId())
                .unreadOnly(unreadOnly)
                .type(type)
                .page(getPageOrDefault())
                .pageSize(DEFAULT_PAGE_SIZE)
                .build();
    }
}
