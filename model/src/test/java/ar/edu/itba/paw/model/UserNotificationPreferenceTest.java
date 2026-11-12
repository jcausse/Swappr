package ar.edu.itba.paw.model;

import ar.edu.itba.paw.model.notification.NotificationMode;
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
    public void testDefaultModesMatchSupportedChannels() {
        Assert.assertEquals(NotificationMode.SITE_AND_EMAIL, NotificationType.NEW_OFFER.getDefaultMode());
        Assert.assertTrue(NotificationType.NEW_OFFER.supportsSite());
        Assert.assertTrue(NotificationType.NEW_OFFER.supportsEmail());

        Assert.assertEquals(NotificationMode.EMAIL_ONLY, NotificationType.LISTING_PUBLISHED.getDefaultMode());
        Assert.assertFalse(NotificationType.LISTING_PUBLISHED.supportsSite());
        Assert.assertTrue(NotificationType.LISTING_PUBLISHED.supportsEmail());

        Assert.assertEquals(NotificationMode.EMAIL_ONLY, NotificationType.PASSWORD_RECOVERY.getDefaultMode());
        Assert.assertFalse(NotificationType.PASSWORD_RECOVERY.supportsSite());
        Assert.assertTrue(NotificationType.PASSWORD_RECOVERY.supportsEmail());
    }

    @Test
    public void testSupportedModesForMultiChannelAndEmailOnlyTypes() {
        Assert.assertEquals(
                List.of(
                        NotificationMode.DISABLED,
                        NotificationMode.SITE_ONLY,
                        NotificationMode.EMAIL_ONLY,
                        NotificationMode.SITE_AND_EMAIL
                ),
                NotificationType.NEW_OFFER.getSupportedModes()
        );

        Assert.assertEquals(
                List.of(
                        NotificationMode.DISABLED,
                        NotificationMode.EMAIL_ONLY
                ),
                NotificationType.LISTING_PUBLISHED.getSupportedModes()
        );

        Assert.assertTrue(NotificationType.PASSWORD_RECOVERY.getSupportedModes().isEmpty());
    }
}
