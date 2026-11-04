package ar.edu.itba.paw.webapp.form;

import ar.edu.itba.paw.model.NotificationType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.Min;

@NoArgsConstructor
@Getter
@Setter
public class NotificationFilterForm {

    private boolean unreadOnly = false;
    private NotificationType type;
    @Min(1)
    private Integer page = 1;

    public int getPageOrDefault() {
        return page != null && page >= 1 ? page : 1;
    }
}
