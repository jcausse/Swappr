package ar.edu.itba.paw.service;

import ar.edu.itba.paw.model.OneTimePassword;
import ar.edu.itba.paw.model.User;
import ar.edu.itba.paw.service.dto.mail.UserMailDto;
import ar.edu.itba.paw.service.enumeration.OneTimePasswordVerificationResult;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class EmailVerificationServiceImpl implements EmailVerificationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(EmailVerificationServiceImpl.class);

    private final UserService userService;
    private final OneTimePasswordService otpService;
    private final MailingService mailingService;

    @Override
    @Transactional
    public OneTimePassword sendVerificationEmail(@NonNull User user) {
        LOGGER.debug("Sending verification email to user id={}", user.getId());
        final var otp = otpService.create(user);
        mailingService.sendVerificationEmail(UserMailDto.from(user), otp.getOtpValue());
        LOGGER.info("Verification email sent to user id={}", user.getId());
        return otp;
    }

    @Override
    @Transactional
    public Optional<OneTimePassword> resendVerificationEmail(@NonNull String usernameOrEmail) {
        LOGGER.debug("Resending verification email requested for '{}'", usernameOrEmail);
        final var maybeUser = userService.getByUsernameOrEmail(usernameOrEmail);
        if (maybeUser.isEmpty()) {
            LOGGER.warn("Resend verification email failed: user '{}' not found", usernameOrEmail);
            return Optional.empty();
        }

        final var user = maybeUser.get();
        if (user.isVerified()) {
            LOGGER.warn("Resend verification email aborted: user id={} is already verified", user.getId());
            return Optional.empty();
        }

        final var otp = otpService.create(user);
        mailingService.sendVerificationEmail(UserMailDto.from(user), otp.getOtpValue());
        LOGGER.info("Resent verification email to user id={}", user.getId());
        return Optional.of(otp);
    }

    @Override
    @Transactional
    public OneTimePasswordVerificationResult verifyEmail(@NonNull String usernameOrEmail, @NonNull String otpValue) {
        LOGGER.debug("Verifying email for '{}'", usernameOrEmail);
        final var maybeUser = userService.getByUsernameOrEmail(usernameOrEmail);
        if (maybeUser.isEmpty()) {
            LOGGER.warn("Email verification rejected: user '{}' not found", usernameOrEmail);
            return OneTimePasswordVerificationResult.REJECTED;
        }

        final var user = maybeUser.get();
        if (user.isVerified()) {
            LOGGER.debug("Email verification for user id={}: user already verified", user.getId());
            return OneTimePasswordVerificationResult.ACCEPTED;
        }

        final var result = otpService.verify(user, otpValue);
        LOGGER.info("Email verification result for user id={}: {}", user.getId(), result);
        if (result == OneTimePasswordVerificationResult.ACCEPTED) {
            userService.markEmailAsVerified(user);
            mailingService.sendWelcomeEmail(UserMailDto.from(user));
            LOGGER.info("User id={} email successfully verified and welcome email dispatched", user.getId());
        }

        return result;
    }
}
