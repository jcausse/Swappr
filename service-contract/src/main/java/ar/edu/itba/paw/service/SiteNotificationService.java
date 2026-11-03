package ar.edu.itba.paw.service;

import ar.edu.itba.paw.model.Page;
import ar.edu.itba.paw.model.SiteNotification;
import ar.edu.itba.paw.model.SiteNotificationFilter;
import ar.edu.itba.paw.model.User;
import ar.edu.itba.paw.service.dto.SiteNotificationCreationDto;
import lombok.NonNull;

public interface SiteNotificationService {
    /**
     * Get the latest {@code n} (capped at 30) unread notifications for a {@code recipient}.
     * This is meant to help populate notification bells and other non-paged UI components.
     */
    Page<SiteNotification> getRecentUnreadForUser(@NonNull User recipient, int n);

    /** Get all notifications that match the {@code filter}. **/
    Page<SiteNotification> getMatching(@NonNull SiteNotificationFilter filter);

    /** Create a new notification **/
    SiteNotification create(@NonNull SiteNotificationCreationDto creationDto);

    /** Mark all unread notifications as read for a {@code recipient}. **/
    void markAllAsReadForUser(@NonNull User recipient);

    /** Delete a notification (note that this is a permanent operation, a physical deletion, not a logical deletion). **/
    void delete(@NonNull SiteNotification notification);

    /** Get the total unread notification count for a {@code recipient}. **/
    int countUnreadForUser(@NonNull User user);
}
