package ar.edu.itba.paw.service;

import ar.edu.itba.paw.model.Image;
import ar.edu.itba.paw.model.Role;
import ar.edu.itba.paw.model.Province;
import ar.edu.itba.paw.model.User;
import ar.edu.itba.paw.model.Language;
import ar.edu.itba.paw.model.OneTimePassword;
import ar.edu.itba.paw.persistence.UserDao;
import ar.edu.itba.paw.persistence.UserRoleDao;
import ar.edu.itba.paw.service.dto.ImageData;
import ar.edu.itba.paw.service.dto.UserCreationDto;
import ar.edu.itba.paw.service.dto.UserEditDto;
import ar.edu.itba.paw.service.dto.mail.UserMailDto;
import ar.edu.itba.paw.service.enumeration.OneTimePasswordVerificationResult;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserServiceImpl.class);

    private final UserDao userDao;
    private final UserRoleDao userRoleDao;
    private final ProvinceService provinceService;
    private final ImageService imageService;
    private final PasswordEncoder passwordEncoder;
    private final MailingService mailingService;
    private final OneTimePasswordService otpService;


    @Override
    public Optional<User> getById(@NonNull Long id) {
        return userDao.getById(id);
    }

    @Override
    public Optional<User> getByUsername(@NonNull String username) {
        return userDao.getByUsername(username.trim().toLowerCase());
    }

    @Override
    public Optional<User> getByEmail(@NonNull String email) {
        return userDao.getByEmail(email.trim().toLowerCase());
    }

    @Override
    public Optional<User> getByUsernameOrEmail(@NonNull String usernameOrEmail) {
        return usernameOrEmail.contains("@")
                ? getByEmail(usernameOrEmail)
                : getByUsername(usernameOrEmail);
    }

    @Override
    @Transactional
    public User create(@NonNull UserCreationDto dto) {
        LOGGER.debug("Creating user with username '{}'", dto.username());
        if (dto.username().contains("@")) {
            LOGGER.warn("User creation rejected: username '{}' contains '@'", dto.username());
            throw new IllegalArgumentException("UserCreationDto.username cannot contain @");
        }

        var user = userDao.create(
            dto.username().trim().toLowerCase(),
            dto.displayName().trim(),
            dto.email().trim().toLowerCase(),
            passwordEncoder.encode(dto.password()),
            saveUserImage(dto.image(), dto.username()),
            Instant.now(),
            Language.fromCode(LocaleContextHolder.getLocale().getLanguage())
        );
        userRoleDao.addRole(user, Role.USER);
        LOGGER.info("User created: id={}, username='{}'", user.getId(), user.getUsername());

        sendVerificationEmail(user);

        return user;
    }

    private OneTimePassword sendVerificationEmail(@NonNull User user) {
        LOGGER.debug("Sending verification email to user id={}", user.getId());
        final var otp = otpService.create(user);
        mailingService.sendVerificationEmail(UserMailDto.from(user), otp.getOtpValue());
        LOGGER.info("Verification email sent to user id={}", user.getId());
        return otp;
    }

    @Override
    @Transactional
    public User update(@NonNull UserEditDto dto) {
        Objects.requireNonNull(dto.user(), "User cannot be null");
        final var user = dto.user();
        LOGGER.debug("Updating profile for user id={}", user.getId());

        /* Prepare User properties to be updated */
        var displayName = (dto.newDisplayName() != null && !dto.newDisplayName().isBlank())
                ? dto.newDisplayName()
                : null;
        var encodedPassword = (dto.newPassword() != null && !dto.newPassword().isBlank())
                ? passwordEncoder.encode(dto.newPassword())
                : null;
        Optional<Image> maybeNewImage = Optional.ofNullable(saveUserImage(
                dto.newImageData(),
                dto.user().getUsername()
        ));

        /* Update User */
        final var updateResult = userDao.update(
                user.getId(),
                displayName,
                null,
                encodedPassword,
                maybeNewImage.map(Image::getId).orElse(null)
        );

        /* Delete the old image to prevent orphans, if a new one was set and the user previously had one */
        if (maybeNewImage.isPresent() && user.getImageId().isPresent()) {
            LOGGER.debug("Replacing old profile image id={} for user id={}", user.getImageId().get(), user.getId());
            imageService.delete(user.getImageId().get());
        }

        var updated = updateResult
                .orElseThrow(() -> new IllegalArgumentException("Non-valid User received"));

        /*
         * Location and preferred language live on the same user, so we apply them in the same
         * transaction. Only when the caller opted in (the profile edit form); other flows such as
         * a password reset leave these untouched instead of clearing them by omission.
         */
        if (dto.updateLocationAndLanguage()) {
            updateLocation(user, dto.newProvinceId(), dto.newLocationDetail());
            updated = updatePreferredLanguage(user, dto.newPreferredLanguage());
        }

        LOGGER.info("User profile updated: id={}, displayNameChanged={}, passwordChanged={}, imageChanged={}",
                user.getId(), displayName != null, encodedPassword != null, maybeNewImage.isPresent());
        return updated;
    }

    @Override
    @Transactional
    public User updateLocation(@NonNull User user, Long provinceId, String locationDetail) {
        LOGGER.debug("Updating location for user id={}", user.getId());

        final Province province = provinceId == null
                ? null
                : provinceService.getById(provinceId).orElse(null);
        final Long resolvedProvinceId = province == null ? null : province.getId();
        final String detail = province == null ? null : normalizeDetail(locationDetail);

        return userDao.updateLocation(user.getId(), resolvedProvinceId, detail)
                .orElseThrow(() -> new IllegalArgumentException("Non-valid User received"));
    }

    @Override
    @Transactional
    public User updatePreferredLanguage(@NonNull User user, @NonNull Language preferredLanguage) {
        LOGGER.info("Updating preferred language for user id={} to '{}'", user.getId(), preferredLanguage.getCode());
        return userDao.updatePreferredLanguage(user.getId(), preferredLanguage)
                .orElseThrow(() -> new IllegalArgumentException("Non-valid User received"));
    }

    @Override
    @Transactional
    public User updateEmail(@NonNull User user, @NonNull String email) {
        LOGGER.info("Updating email for user id={}", user.getId());
        return userDao.update(user.getId(), null, email.trim().toLowerCase(), null, null)
                .orElseThrow(() -> new IllegalArgumentException("Non-valid User received"));
    }

    @Override
    @Transactional
    public Optional<OneTimePassword> resendVerificationEmail(@NonNull String usernameOrEmail) {
        LOGGER.debug("Resending verification email requested for '{}'", usernameOrEmail);
        final var maybeUser = getByUsernameOrEmail(usernameOrEmail);
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
        final var maybeUser = getByUsernameOrEmail(usernameOrEmail);
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
            markEmailAsVerified(user);
            mailingService.sendWelcomeEmail(UserMailDto.from(user));
            LOGGER.info("User id={} email successfully verified and welcome email dispatched", user.getId());
        }

        return result;
    }

    private User markEmailAsVerified(@NonNull User user) {
        LOGGER.info("Marking email as verified for user id={}", user.getId());
        return userDao.verifyEmail(user.getId(), Instant.now())
                .orElseThrow(() -> new IllegalArgumentException("Non-valid User received"));
    }

    @Override
    public boolean isUsernameTaken(@NonNull String username) {
        return userDao.isUsernameTaken(username.trim().toLowerCase());
    }

    @Override
    public boolean isEmailTaken(@NonNull String email) {
        return userDao.isEmailTaken(email.trim().toLowerCase());
    }

    @Override
    @Transactional
    public void addRole(@NonNull User user, @NonNull Role role) {
        LOGGER.info("Adding role '{}' to user id={}", role.getRoleName(), user.getId());
        userRoleDao.addRole(user, role);
    }

    @Override
    public List<Role> getRoles(@NonNull User user) {
        return userRoleDao.getRoles(user);
    }

    @Override
    @Transactional
    public User suspend(@NonNull Long userId) {
        LOGGER.info("Suspending user id={}", userId);
        final User user = userDao.suspend(userId, Instant.now())
                .orElseThrow(() -> new IllegalArgumentException("Non-valid User received"));
        mailingService.sendAccountSuspendedEmail(UserMailDto.from(user));
        return user;
    }

    @Override
    @Transactional
    public User unsuspend(@NonNull Long userId) {
        LOGGER.info("Unsuspending user id={}", userId);
        final User user = userDao.unsuspend(userId)
                .orElseThrow(() -> new IllegalArgumentException("Non-valid User received"));
        mailingService.sendAccountUnsuspendedEmail(UserMailDto.from(user));
        return user;
    }



    private Image saveUserImage(ImageData imageData, String username) {
        if (imageData != null && imageData.imageBytes() != null && imageData.imageBytes().length > 0) {
            return imageService.create(
                    imageData.imageFilename(),
                    username + "'s profile picture",
                    imageData.imageContentType(),
                    imageData.imageBytes()
            );
        }
        return null;
    }

    private static String normalizeDetail(final String detail) {
        if (detail == null) {
            return null;
        }
        final String trimmed = detail.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
