package ar.edu.itba.paw.model;

import ar.edu.itba.paw.model.notification.NotificationChannel;
import ar.edu.itba.paw.model.notification.NotificationType;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class UserNotificationPreferenceTest {

    @Test
    public void testConfigurableTypesExcludesSecurityAndOnboardingTypes() {
        final List<NotificationType> configurable = NotificationType.getConfigurableTypes();

        Assert.assertFalse(configurable.contains(NotificationType.WELCOME));
        Assert.assertFalse(configurable.contains(NotificationType.PASSWORD_RECOVERY));
        Assert.assertFalse(configurable.contains(NotificationType.EMAIL_VERIFICATION));
        Assert.assertFalse(configurable.contains(NotificationType.ACCOUNT_SUSPENDED));
        Assert.assertFalse(configurable.contains(NotificationType.ACCOUNT_UNSUSPENDED));

        Assert.assertTrue(configurable.contains(NotificationType.NEW_OFFER));
        Assert.assertTrue(configurable.contains(NotificationType.LISTING_PUBLISHED));
        Assert.assertTrue(configurable.contains(NotificationType.PENDING_TRANSACTION));
        Assert.assertTrue(configurable.contains(NotificationType.LISTING_SOLD));
    }

    @Test
    public void testSupportedChannelsMatchSiteAndEmailHelpers() {
        Assert.assertTrue(NotificationType.NEW_OFFER.supportsChannel(NotificationChannel.SITE));
        Assert.assertTrue(NotificationType.NEW_OFFER.supportsChannel(NotificationChannel.EMAIL));
        Assert.assertTrue(NotificationType.NEW_OFFER.supportsSite());
        Assert.assertTrue(NotificationType.NEW_OFFER.supportsEmail());

        Assert.assertFalse(NotificationType.LISTING_PUBLISHED.supportsChannel(NotificationChannel.SITE));
        Assert.assertTrue(NotificationType.LISTING_PUBLISHED.supportsChannel(NotificationChannel.EMAIL));
        Assert.assertFalse(NotificationType.LISTING_PUBLISHED.supportsSite());
        Assert.assertTrue(NotificationType.LISTING_PUBLISHED.supportsEmail());

        Assert.assertFalse(NotificationType.PASSWORD_RECOVERY.supportsChannel(NotificationChannel.SITE));
        Assert.assertTrue(NotificationType.PASSWORD_RECOVERY.supportsChannel(NotificationChannel.EMAIL));
        Assert.assertFalse(NotificationType.PASSWORD_RECOVERY.supportsChannel(null));
    }
}
