package ar.edu.itba.paw.webapp.controller;

import ar.edu.itba.paw.model.NotificationType;
import ar.edu.itba.paw.model.SiteNotificationFilter;
import ar.edu.itba.paw.model.User;
import ar.edu.itba.paw.service.SiteNotificationService;
import ar.edu.itba.paw.webapp.auth.CurrentUser;
import ar.edu.itba.paw.webapp.form.NotificationFilterForm;
import ar.edu.itba.paw.webapp.form.StringSelectOption;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.Arrays;
import java.util.List;

@RequiredArgsConstructor
@Controller
@RequestMapping("/notifications")
public class NotificationController {

    private static final Logger LOGGER = LoggerFactory.getLogger(NotificationController.class);
    private static final int DEFAULT_PAGE_SIZE = 10;

    private final SiteNotificationService siteNotificationService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ModelAndView listNotifications(
            @CurrentUser User currentUser,
            @Valid @ModelAttribute("filterForm") NotificationFilterForm filterForm
    ) {
        LOGGER.debug("User {} viewing notifications: unreadOnly={}, type={}, page={}",
                currentUser.getId(), filterForm.isUnreadOnly(), filterForm.getType(), filterForm.getPageOrDefault());

        final var filter = SiteNotificationFilter.builder()
                .userId(currentUser.getId())
                .unreadOnly(filterForm.isUnreadOnly())
                .type(filterForm.getType())
                .page(filterForm.getPageOrDefault())
                .pageSize(DEFAULT_PAGE_SIZE)
                .build();

        final var notificationPage = siteNotificationService.getMatching(filter);

        final var readStatusOptions = List.of(
                new StringSelectOption("false", "notifications.filter.status.all"),
                new StringSelectOption("true", "notifications.filter.status.unread")
        );

        final var typeOptions = Arrays.stream(NotificationType.values())
                .filter(NotificationType::supportsSite)
                .map(t -> new StringSelectOption(t.name(), "notification.type." + t.name()))
                .toList();

        return new ModelAndView("notifications/index")
                .addObject("notificationPage", notificationPage)
                .addObject("notifications", notificationPage.getContent())
                .addObject("readStatusOptions", readStatusOptions)
                .addObject("typeOptions", typeOptions);
    }

    @PostMapping("/read-all")
    @PreAuthorize("isAuthenticated()")
    public ModelAndView markAllAsRead(@CurrentUser User currentUser, HttpServletRequest request) {
        LOGGER.info("User {} marking all notifications as read", currentUser.getId());
        siteNotificationService.markAllAsReadForUser(currentUser);

        final String target = RedirectHelper.resolveReferer(request, "/notifications");
        return new ModelAndView("redirect:" + target);
    }

    @PostMapping("/{notificationId}/read")
    @PreAuthorize("@webPermissionService.canManageNotification(authentication, #notificationId)")
    public ModelAndView markAsRead(
            @PathVariable Long notificationId,
            @CurrentUser User currentUser,
            HttpServletRequest request
    ) {
        LOGGER.info("User {} marking notification {} as read", currentUser.getId(), notificationId);
        siteNotificationService.markAsRead(notificationId, currentUser);

        final String target = RedirectHelper.resolveReferer(request, "/notifications");
        return new ModelAndView("redirect:" + target);
    }

    @PostMapping("/{notificationId}/delete")
    @PreAuthorize("@webPermissionService.canManageNotification(authentication, #notificationId)")
    public ModelAndView deleteNotification(
            @PathVariable Long notificationId,
            @CurrentUser User currentUser,
            HttpServletRequest request
    ) {
        LOGGER.info("User {} deleting notification {}", currentUser.getId(), notificationId);
        siteNotificationService.delete(notificationId, currentUser);

        final String target = RedirectHelper.resolveReferer(request, "/notifications");
        return new ModelAndView("redirect:" + target);
    }
}
