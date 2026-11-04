package ar.edu.itba.paw.service;

import ar.edu.itba.paw.model.Page;
import ar.edu.itba.paw.model.SiteNotification;
import ar.edu.itba.paw.model.SiteNotificationFilter;
import ar.edu.itba.paw.model.User;
import ar.edu.itba.paw.persistence.SiteNotificationDao;
import ar.edu.itba.paw.service.dto.SiteNotificationCreationDto;
import ar.edu.itba.paw.service.exception.ForbiddenException;
import ar.edu.itba.paw.service.exception.NotFoundException;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SiteNotificationServiceImpl implements SiteNotificationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SiteNotificationServiceImpl.class);

    private static final int MAX_RECENT_NOTIFICATIONS = 30;

    private final SiteNotificationDao siteNotificationDao;

    @Override
    public Optional<SiteNotification> getById(long notificationId) {
        LOGGER.debug("Fetching site notification id={}", notificationId);
        return siteNotificationDao.getById(notificationId);
    }

    @Override
    public Page<SiteNotification> getRecentUnreadForUser(@NonNull User recipient, int n) {
        final int pageSize = Math.clamp(n, 1, MAX_RECENT_NOTIFICATIONS);
        LOGGER.debug("Fetching up to {} recent unread notifications for user id={}", pageSize, recipient.getId());

        return siteNotificationDao.getMatching(
                SiteNotificationFilter.builder()
                        .userId(recipient.getId())
                        .unreadOnly(true)
                        .page(1)
                        .pageSize(pageSize)
                        .build()
        );
    }

    @Override
    public Page<SiteNotification> getMatching(@NonNull SiteNotificationFilter filter) {
        LOGGER.debug("Fetching notifications matching filter: userId={}, unreadOnly={}, type={}, page={}, pageSize={}",
                filter.getUserId(), filter.isUnreadOnly(), filter.getType(), filter.getPage(), filter.getPageSize());

        return siteNotificationDao.getMatching(filter);
    }

    @Override
    @Transactional
    public SiteNotification create(@NonNull SiteNotificationCreationDto creationDto) {
        LOGGER.debug("Creating site notification of type {} for user id={}, title='{}'",
                creationDto.notificationType(), creationDto.notificationRecipient().getId(), creationDto.title());

        final SiteNotification notification = siteNotificationDao.create(
                creationDto.notificationRecipient(),
                creationDto.notificationType(),
                creationDto.title(),
                creationDto.body(),
                creationDto.actionUrl(),
                creationDto.actionLabelKey(),
                Instant.now()
        );

        LOGGER.info("Created site notification id={} of type {} for user id={}",
                notification.getId(), notification.getType(), creationDto.notificationRecipient().getId());

        return notification;
    }

    @Override
    @Transactional
    public void markAsRead(long notificationId, @NonNull User actor) {
        LOGGER.debug("Marking site notification id={} as read by user id={}", notificationId, actor.getId());
        final SiteNotification notification = getOwnedNotificationOrThrow(notificationId, actor);
        siteNotificationDao.markAsRead(notification);
        LOGGER.info("Marked site notification id={} as read by user id={}", notificationId, actor.getId());
    }

    @Override
    @Transactional
    public void markAllAsReadForUser(@NonNull User recipient) {
        LOGGER.debug("Marking all notifications as read for user id={}", recipient.getId());
        siteNotificationDao.markAllAsReadForUser(recipient);
        LOGGER.info("Marked all notifications as read for user id={}", recipient.getId());
    }

    @Override
    @Transactional
    public void delete(@NonNull SiteNotification notification) {
        LOGGER.debug("Deleting site notification id={}", notification.getId());
        siteNotificationDao.delete(notification);
        LOGGER.info("Deleted site notification id={}", notification.getId());
    }

    @Override
    @Transactional
    public void delete(long notificationId, @NonNull User actor) {
        LOGGER.debug("Deleting site notification id={} by user id={}", notificationId, actor.getId());
        final SiteNotification notification = getOwnedNotificationOrThrow(notificationId, actor);
        siteNotificationDao.delete(notification);
        LOGGER.info("Deleted site notification id={} by user id={}", notificationId, actor.getId());
    }

    @Override
    public int countUnreadForUser(@NonNull User user) {
        LOGGER.debug("Counting unread notifications for user id={}", user.getId());
        return siteNotificationDao.countUnreadForUser(user);
    }

    private SiteNotification getOwnedNotificationOrThrow(final long notificationId, final User actor) {
        final SiteNotification notification = siteNotificationDao.getById(notificationId)
                .orElseThrow(() -> NotFoundException.createFor("Notification with ID " + notificationId));
        if (!notification.isRecipient(actor)) {
            LOGGER.warn("User {} unauthorized to access notification {}", actor.getId(), notificationId);
            throw new ForbiddenException("Not authorized to manage this notification");
        }
        return notification;
    }
}
