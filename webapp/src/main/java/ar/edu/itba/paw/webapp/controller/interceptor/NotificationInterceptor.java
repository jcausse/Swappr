package ar.edu.itba.paw.webapp.controller.interceptor;

import ar.edu.itba.paw.model.Page;
import ar.edu.itba.paw.model.notification.SiteNotification;
import ar.edu.itba.paw.service.SiteNotificationService;
import ar.edu.itba.paw.webapp.auth.AuthHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

// Adds the navbar notification preview only to rendered views. A @ModelAttribute would query the
// notifications on every request, including image/file responses and redirects that never show them.
@RequiredArgsConstructor
public class NotificationInterceptor implements HandlerInterceptor {

    private static final int NAVBAR_DROPDOWN_LIMIT = 10;
    private static final String NOTIFICATIONS_ATTRIBUTE = "recentUnreadNotificationsPage";

    private final AuthHelper authHelper;
    private final SiteNotificationService siteNotificationService;

    @Override
    public void postHandle(
            @NonNull final HttpServletRequest request,
            @NonNull final HttpServletResponse response,
            @NonNull final Object handler,
            final ModelAndView modelAndView
    ) {
        if (!InterceptorUtils.rendersView(modelAndView) || modelAndView.getModel().containsKey(NOTIFICATIONS_ATTRIBUTE)) {
            return;
        }
        final Page<SiteNotification> notifications = authHelper.getCurrentUser()
                .map(user -> siteNotificationService.getRecentUnreadForUser(user, NAVBAR_DROPDOWN_LIMIT))
                .orElseGet(() -> new Page<>(List.of(), 1, NAVBAR_DROPDOWN_LIMIT, 0));
        modelAndView.addObject(NOTIFICATIONS_ATTRIBUTE, notifications);
    }
}
