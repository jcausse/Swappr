package ar.edu.itba.paw.model;

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
}
