package ar.edu.itba.paw.webapp.controller.advice;

import ar.edu.itba.paw.model.Page;
import ar.edu.itba.paw.model.notification.SiteNotification;
import ar.edu.itba.paw.service.SiteNotificationService;
import ar.edu.itba.paw.webapp.auth.AuthHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.List;

@ControllerAdvice
@RequiredArgsConstructor
public class NotificationControllerAdvice {

    private static final int NAVBAR_DROPDOWN_LIMIT = 10;

    private final AuthHelper authHelper;
    private final SiteNotificationService siteNotificationService;

    @ModelAttribute(value = "recentUnreadNotificationsPage", binding = false)
    public Page<SiteNotification> recentUnreadNotificationsPage() {
        return authHelper.getCurrentUser()
                .map(user -> siteNotificationService.getRecentUnreadForUser(user, NAVBAR_DROPDOWN_LIMIT))
                .orElseGet(() -> new Page<>(List.of(), 1, NAVBAR_DROPDOWN_LIMIT, 0));
    }
}
