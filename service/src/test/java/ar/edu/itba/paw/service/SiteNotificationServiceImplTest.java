package ar.edu.itba.paw.service;

import ar.edu.itba.paw.model.*;
import ar.edu.itba.paw.persistence.SiteNotificationDao;
import ar.edu.itba.paw.service.dto.SiteNotificationCreationDto;
import ar.edu.itba.paw.service.exception.ForbiddenException;
import ar.edu.itba.paw.service.exception.NotFoundException;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
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
                .title("New Offer")
                .body("You have a new offer")
                .actionUrl("/offers/1")
                .actionLabelKey("notification.action.viewOffer")
                .read(false)
                .createdAt(Instant.now())
                .build();
    }

    @Test
    public void testCreateDelegatesToDaoAndReturnsNotification() {
        // Arrange
        final User user = buildUser();
        final SiteNotification expected = buildNotification(user);
        final SiteNotificationCreationDto dto = SiteNotificationCreationDto.builder()
                .notificationRecipient(user)
                .notificationType(NotificationType.NEW_OFFER)
                .title("New Offer")
                .body("You have a new offer")
                .actionUrl("/offers/1")
                .actionLabelKey("notification.action.viewOffer")
                .build();

        when(siteNotificationDao.create(
                eq(user),
                eq(NotificationType.NEW_OFFER),
                eq("New Offer"),
                eq("You have a new offer"),
                eq("/offers/1"),
                eq("notification.action.viewOffer"),
                any(Instant.class)
        )).thenReturn(expected);

        // Act
        final SiteNotification result = notificationService.create(dto);

        // Assert
        Assert.assertNotNull(result);
        Assert.assertEquals(expected.getId(), result.getId());
        Assert.assertEquals(expected.getTitle(), result.getTitle());
    }

    @Test
    public void testGetRecentUnreadForUserClampsPageSizeToMax() {
        // Arrange
        final User user = buildUser();
        final ArgumentCaptor<SiteNotificationFilter> filterCaptor =
                ArgumentCaptor.forClass(SiteNotificationFilter.class);
        final Page<SiteNotification> mockPage = new Page<>(List.of(), 1, 30, 0);

        when(siteNotificationDao.getMatching(filterCaptor.capture())).thenReturn(mockPage);

        // Act - request 50 (exceeding MAX 30)
        final Page<SiteNotification> result = notificationService.getRecentUnreadForUser(user, 50);

        // Assert
        Assert.assertNotNull(result);
        final SiteNotificationFilter capturedFilter = filterCaptor.getValue();
        Assert.assertEquals(USER_ID, (long) capturedFilter.getUserId());
        Assert.assertTrue(capturedFilter.isUnreadOnly());
        Assert.assertEquals(30, capturedFilter.getPageSize());
        Assert.assertEquals(1, capturedFilter.getPage());
    }

    @Test
    public void testGetRecentUnreadForUserClampsPageSizeToMin() {
        // Arrange
        final User user = buildUser();
        final ArgumentCaptor<SiteNotificationFilter> filterCaptor =
                ArgumentCaptor.forClass(SiteNotificationFilter.class);
        final Page<SiteNotification> mockPage = new Page<>(List.of(), 1, 1, 0);

        when(siteNotificationDao.getMatching(filterCaptor.capture())).thenReturn(mockPage);

        // Act - request 0 (below min 1)
        final Page<SiteNotification> result = notificationService.getRecentUnreadForUser(user, 0);

        // Assert
        Assert.assertNotNull(result);
        final SiteNotificationFilter capturedFilter = filterCaptor.getValue();
        Assert.assertEquals(1, capturedFilter.getPageSize());
    }

    @Test
    public void testGetMatchingDelegatesToDao() {
        // Arrange
        final SiteNotificationFilter filter = SiteNotificationFilter.builder()
                .userId(USER_ID)
                .unreadOnly(false)
                .page(1)
                .pageSize(10)
                .build();
        final Page<SiteNotification> mockPage = new Page<>(List.of(), 1, 10, 0);

        when(siteNotificationDao.getMatching(filter)).thenReturn(mockPage);

        // Act
        final Page<SiteNotification> result = notificationService.getMatching(filter);

        // Assert
        Assert.assertSame(mockPage, result);
    }

    @Test
    public void testMarkAsReadByRecipientDelegatesToDao() {
        // Arrange
        final User recipient = buildUser();
        final SiteNotification notification = buildNotification(recipient);
        final ArgumentCaptor<SiteNotification> captor = ArgumentCaptor.forClass(SiteNotification.class);
        when(siteNotificationDao.getById(100L)).thenReturn(Optional.of(notification));
        doNothing().when(siteNotificationDao).markAsRead(captor.capture());

        // Act
        notificationService.markAsRead(100L, recipient);

        // Assert
        Assert.assertSame(notification, captor.getValue());
    }

    @Test
    public void testDeleteByIdByRecipientDelegatesToDao() {
        // Arrange
        final User recipient = buildUser();
        final SiteNotification notification = buildNotification(recipient);
        final ArgumentCaptor<SiteNotification> captor = ArgumentCaptor.forClass(SiteNotification.class);
        when(siteNotificationDao.getById(100L)).thenReturn(Optional.of(notification));
        doNothing().when(siteNotificationDao).delete(captor.capture());

        // Act
        notificationService.delete(100L, recipient);

        // Assert
        Assert.assertSame(notification, captor.getValue());
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
