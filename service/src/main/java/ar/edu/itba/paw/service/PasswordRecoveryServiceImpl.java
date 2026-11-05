package ar.edu.itba.paw.service;

import ar.edu.itba.paw.model.OneTimePassword;
import ar.edu.itba.paw.service.dto.UserEditDto;
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
public class PasswordRecoveryServiceImpl implements PasswordRecoveryService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PasswordRecoveryServiceImpl.class);

    private final UserService userService;
    private final OneTimePasswordService otpService;
    private final NotificationDispatcherService notificationDispatcherService;

    @Override
    @Transactional
    public Optional<OneTimePassword> startAndSendRecoveryEmail(@NonNull String usernameOrEmail) {
        LOGGER.debug("Starting password recovery for '{}'", usernameOrEmail);
        final var maybeUser = userService.getByUsernameOrEmail(usernameOrEmail);
        if (maybeUser.isEmpty()) {
            LOGGER.warn("Password recovery failed: user '{}' not found", usernameOrEmail);
            return Optional.empty();
        }

        final var user = maybeUser.get();
        final var otp = otpService.create(user);
        notificationDispatcherService.notifyPasswordRecovery(user, otp.getOtpValue());
        LOGGER.info("Password recovery notification dispatched for user id={}", user.getId());
        return Optional.of(otp);
    }

    @Override
    @Transactional
    public OneTimePasswordVerificationResult verifyAndUpdatePassword(
            @NonNull String usernameOrEmail,
            @NonNull String password,
            @NonNull String otpValue
    ) {
        LOGGER.debug("Verifying password recovery for '{}'", usernameOrEmail);
        final var maybeUser = userService.getByUsernameOrEmail(usernameOrEmail);
        if (maybeUser.isEmpty()) {
            LOGGER.warn("Password recovery verification failed: user '{}' not found", usernameOrEmail);
            return OneTimePasswordVerificationResult.REJECTED;
        }

        final var user = maybeUser.get();
        final var result = otpService.verify(user, otpValue);
        LOGGER.info("Password recovery verification result for user id={}: {}", user.getId(), result);

        if (result == OneTimePasswordVerificationResult.ACCEPTED) {
            userService.update(UserEditDto.builder()
                    .user(user)
                    .newPassword(password)
                    .build());
            LOGGER.info("Password updated successfully for user id={}", user.getId());
        }

        return result;
    }
}
