package ar.edu.itba.paw.webapp.form;

import ar.edu.itba.paw.model.notification.NotificationMode;
import ar.edu.itba.paw.model.notification.NotificationType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.EnumMap;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
public class NotificationPreferencesForm {

    private Map<NotificationType, NotificationMode> preferences = new EnumMap<>(NotificationType.class);
}
