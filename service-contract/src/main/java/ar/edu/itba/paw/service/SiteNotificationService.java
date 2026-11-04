package ar.edu.itba.paw.service;

import ar.edu.itba.paw.model.Page;
import ar.edu.itba.paw.model.SiteNotification;
import ar.edu.itba.paw.model.SiteNotificationFilter;
import ar.edu.itba.paw.model.User;
import ar.edu.itba.paw.service.dto.SiteNotificationCreationDto;
import lombok.NonNull;

import java.util.Optional;

public interface SiteNotificationService {
    /** Find a notification by its unique identifier. **/
    Optional<SiteNotification> getById(long notificationId);

    /**
     * Get the latest {@code n} (capped at 30) unread notifications for a {@code recipient}.
     * This is meant to help populate notification bells and other non-paged UI components.
     */
    Page<SiteNotification> getRecentUnreadForUser(@NonNull User recipient, int n);

    /** Get all notifications that match the {@code filter}. **/
    Page<SiteNotification> getMatching(@NonNull SiteNotificationFilter filter);

    /** Create a new notification **/
    SiteNotification create(@NonNull SiteNotificationCreationDto creationDto);

    /** Mark a single notification as read on behalf of {@code actor}. **/
    void markAsRead(long notificationId, @NonNull User actor);

    /** Mark all unread notifications as read for a {@code recipient}. **/
    void markAllAsReadForUser(@NonNull User recipient);

    /** Delete a notification by ID on behalf of {@code actor}. **/
    void delete(long notificationId, @NonNull User actor);
}
