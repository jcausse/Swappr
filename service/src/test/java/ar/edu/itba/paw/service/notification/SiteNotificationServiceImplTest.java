package ar.edu.itba.paw.service.notification;

import ar.edu.itba.paw.model.User;
import ar.edu.itba.paw.model.notification.NotificationType;
import ar.edu.itba.paw.model.notification.SiteNotification;
import ar.edu.itba.paw.persistence.SiteNotificationDao;
import ar.edu.itba.paw.service.exception.ForbiddenException;
import ar.edu.itba.paw.service.exception.NotFoundException;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SiteNotificationServiceImplTest {

    private static final long USER_ID = 1L;

    @InjectMocks
    private SiteNotificationServiceImpl notificationService;

    @Mock
    private SiteNotificationDao siteNotificationDao;

    private User buildUser() {
        return User.builder()
                .id(USER_ID)
                .username("test_user")
                .displayName("Test User")
                .email("test@example.com")
                .password("secret")
                .joinedAt(Instant.now())
                .build();
    }

    private User buildIntruder() {
        return User.builder()
                .id(99L)
                .username("intruder")
                .displayName("Intruder")
                .email("intruder@example.com")
                .password("secret")
                .joinedAt(Instant.now())
                .build();
    }

    private SiteNotification buildNotification(final User user) {
        return SiteNotification.builder()
                .id(100L)
                .user(user)
                .type(NotificationType.NEW_OFFER)
                .titleKey("notification.newOffer.title")
                .bodyKey("notification.newOffer.body")
                .bodyArgs(List.of("Buyer Bob", "250.00", "Laptop"))
                .actionUrl("/offers/1")
                .actionLabelKey("notification.action.viewOffer")
                .read(false)
                .createdAt(Instant.now())
                .build();
    }

    @Test(expected = NotFoundException.class)
    public void testMarkAsReadNonExistentThrowsNotFound() {
        // Arrange
        when(siteNotificationDao.getById(999L)).thenReturn(Optional.empty());

        // Act
        notificationService.markAsRead(999L, buildUser());
    }

    @Test(expected = ForbiddenException.class)
    public void testMarkAsReadByOtherUserThrowsForbidden() {
        // Arrange
        final User recipient = buildUser();
        final User intruder = buildIntruder();
        when(siteNotificationDao.getById(100L)).thenReturn(Optional.of(buildNotification(recipient)));

        // Act
        notificationService.markAsRead(100L, intruder);
    }

    @Test(expected = ForbiddenException.class)
    public void testDeleteByIdByOtherUserThrowsForbidden() {
        // Arrange
        final User recipient = buildUser();
        final User intruder = buildIntruder();
        when(siteNotificationDao.getById(100L)).thenReturn(Optional.of(buildNotification(recipient)));

        // Act
        notificationService.delete(100L, intruder);
    }
}
