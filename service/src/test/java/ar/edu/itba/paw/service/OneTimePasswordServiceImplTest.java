package ar.edu.itba.paw.service;

import ar.edu.itba.paw.model.OneTimePassword;
import ar.edu.itba.paw.model.User;
import ar.edu.itba.paw.persistence.OneTimePasswordDao;
import ar.edu.itba.paw.service.enumeration.OneTimePasswordVerificationResult;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class OneTimePasswordServiceImplTest {

    private static final long USER_ID = 1L;
    private static final String PLAIN_OTP = "ABC1234567";
    private static final String ENCODED_OTP = "encoded-otp";

    @InjectMocks
    private OneTimePasswordServiceImpl otpService;

    @Mock
    private OneTimePasswordDao otpDao;
    @Mock
    private PasswordEncoder passwordEncoder;

    private User buildUser() {
        return User.builder()
            .id(USER_ID)
            .username("fake_user")
            .displayName("Fake User")
            .email("fake@example.com")
            .password("secret")
            .joinedAt(Instant.now())
            .build();
    }

    private OneTimePassword buildOtp(final Instant createdAt) {
        return OneTimePassword.builder()
            .id(USER_ID)
            .user(buildUser())
            .otpValue(ENCODED_OTP)
            .createdAt(createdAt)
            .build();
    }

    /* ---------------------------------------------------------------------------------------------- */
    /* create                                                                                          */
    /* ---------------------------------------------------------------------------------------------- */

    @Test
    public void testVerifyAcceptsMatchingUnexpiredOtp() {
        // Arrange
        final User user = buildUser();
        when(otpDao.getByUser(user)).thenReturn(Optional.of(buildOtp(Instant.now())));
        when(passwordEncoder.matches(eq(PLAIN_OTP), eq(ENCODED_OTP))).thenReturn(true);

        // Act
        final var result = otpService.verify(user, PLAIN_OTP);

        // Assert
        Assert.assertEquals(OneTimePasswordVerificationResult.ACCEPTED, result);
    }

    @Test
    public void testVerifyExpiresOldOtp() {
        // Arrange
        final User user = buildUser();
        when(otpDao.getByUser(user)).thenReturn(Optional.of(
            buildOtp(Instant.now().minus(16, ChronoUnit.MINUTES))));

        // Act
        final var result = otpService.verify(user, PLAIN_OTP);

        // Assert
        Assert.assertEquals(OneTimePasswordVerificationResult.EXPIRED, result);
    }

    @Test
    public void testVerifyRejectsWrongOtp() {
        // Arrange
        final User user = buildUser();
        when(otpDao.getByUser(user)).thenReturn(Optional.of(buildOtp(Instant.now())));
        when(passwordEncoder.matches(eq(PLAIN_OTP), eq(ENCODED_OTP))).thenReturn(false);

        // Act
        final var result = otpService.verify(user, PLAIN_OTP);

        // Assert
        Assert.assertEquals(OneTimePasswordVerificationResult.REJECTED, result);
    }

    @Test
    public void testVerifyWithoutStoredOtpIsRejected() {
        // Arrange
        final User user = buildUser();
        when(otpDao.getByUser(user)).thenReturn(Optional.empty());

        // Act
        final var result = otpService.verify(user, PLAIN_OTP);

        // Assert
        Assert.assertEquals(OneTimePasswordVerificationResult.REJECTED, result);
    }
}
