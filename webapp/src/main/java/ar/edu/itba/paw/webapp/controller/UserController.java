package ar.edu.itba.paw.webapp.controller;

import ar.edu.itba.paw.model.*;
import ar.edu.itba.paw.service.EmailVerificationService;
import ar.edu.itba.paw.service.ListingService;
import ar.edu.itba.paw.service.RatingService;
import ar.edu.itba.paw.service.ProvinceService;
import ar.edu.itba.paw.service.UserService;
import ar.edu.itba.paw.service.dto.ImageData;
import ar.edu.itba.paw.service.dto.ListingFilterDto;
import ar.edu.itba.paw.service.dto.UserCreationDto;
import ar.edu.itba.paw.service.dto.UserEditDto;
import ar.edu.itba.paw.webapp.auth.AuthHelper;
import ar.edu.itba.paw.webapp.auth.CurrentUser;
import ar.edu.itba.paw.webapp.exception.UserNotFoundException;
import ar.edu.itba.paw.webapp.form.RatingFilterForm;
import ar.edu.itba.paw.webapp.form.StringSelectOption;
import ar.edu.itba.paw.webapp.form.SelectOption;
import ar.edu.itba.paw.webapp.form.UserEditForm;
import ar.edu.itba.paw.webapp.form.UserForm;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@RequiredArgsConstructor
@Controller
public class UserController {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserController.class);

    private final UserService userService;
    private final ListingService listingService;
    private final RatingService ratingService;
    private final EmailVerificationService emailVerificationService;
    private final ProvinceService provinceService;
    private final AuthHelper authHelper;
    private final MessageSource messageSource;

    private static final int PROFILE_LISTINGS_PAGE_SIZE = 5;
    private static final int PROFILE_RATINGS_PAGE_SIZE = 5;

    /* PROFILE */

    @GetMapping("/profile/{id}")
    public ModelAndView profile(@PathVariable Long id, @CurrentUser(required = false) User currentUser,
                                @ModelAttribute("ratingFilterForm") RatingFilterForm ratingFilterForm) {
        LOGGER.debug("Accessing profile for user id: {}", id);
        final User user = userService.getById(id).orElseThrow(() -> UserNotFoundException.byId(id));
        final boolean isSelfRequest = currentUser != null && Objects.equals(id, currentUser.getId());

        final var filter = ListingFilterDto.builder()
                .sort(ListingSort.RECENT.getKey())
                .creatorId(user.getId())
                .status(ListingStatus.ACTIVE.name())
                .page(1)
                .pageSize(PROFILE_LISTINGS_PAGE_SIZE)
                .build();

        final var listingPage = listingService.search(filter);
        final var listings = listingPage.getContent();

        // Default role to SELLER if not provided
        String role = ratingFilterForm.getRole();
        if (role == null || role.isBlank()) {
            role = RatingRole.SELLER.getRole();
            ratingFilterForm.setRole(role);
        }

        // Default type to null (all) if not provided
        String type = ratingFilterForm.getType();
        if (type != null && type.isBlank()) {
            type = null;
        }

        int page = ratingFilterForm.getPage() != null ? ratingFilterForm.getPage() : 1;

        var ratingFilter = ar.edu.itba.paw.model.RatingFilter.builder()
                .ratedId(user.getId())
                .roles(role != null ? java.util.List.of(RatingRole.fromString(role).orElse(RatingRole.SELLER)) : null)
                .types(type != null
                        ? java.util.List.of(OfferRating.fromString(type)
                                    .orElseThrow(() -> new IllegalArgumentException("Illegal rating filter type")))
                        : null
                )
                .page(page)
                .pageSize(PROFILE_RATINGS_PAGE_SIZE)
                .build();

        final var ratingPage = ratingService.get(ratingFilter);
        final var ratings = ratingPage.getContent();

        // Build filter options for the form
        final var locale = LocaleContextHolder.getLocale();
        final var roleOptions = Arrays.asList(
                new StringSelectOption("seller", messageSource.getMessage("profile.ratings.filter.seller", null, locale)),
                new StringSelectOption("buyer", messageSource.getMessage("profile.ratings.filter.buyer", null, locale))
        );
        final var typeOptions = Arrays.asList(
                new StringSelectOption("", messageSource.getMessage("profile.ratings.filter.all", null, locale)),
                new StringSelectOption("positive", messageSource.getMessage("profile.ratings.filter.positive", null, locale)),
                new StringSelectOption("neutral", messageSource.getMessage("profile.ratings.filter.neutral", null, locale)),
                new StringSelectOption("negative", messageSource.getMessage("profile.ratings.filter.negative", null, locale))
        );

        return new ModelAndView("profile")
                .addObject("user", user)
                .addObject("allowEdit", isSelfRequest)
                .addObject("listings", listings)
                .addObject("listingPage", listingPage)
                .addObject("ratingPage", ratingPage)
                .addObject("ratings", ratings)
                .addObject("roleOptions", roleOptions)
                .addObject("typeOptions", typeOptions);
    }

    @GetMapping("/profile")
    public ModelAndView currentUserProfile(@CurrentUser User currentUser) {
        LOGGER.debug("Accessing current user profile for user {}", currentUser.getId());
        return new ModelAndView("redirect:/profile/" + currentUser.getId());
    }

    /* PROFILE EDIT */

    @GetMapping("/profile/edit")
    public ModelAndView editProfileForm(@CurrentUser User currentUser, @ModelAttribute("userEditForm") UserEditForm form) {
        LOGGER.debug("Accessing profile edit form for user {}", currentUser.getId());
        form.setDisplayName(currentUser.getDisplayName());
        form.setProvinceId(currentUser.getProvince().map(Province::getId).orElse(null));
        form.setLocationDetail(currentUser.getLocationDetail().orElse(null));
        final Language currentLanguage = currentUser.getPreferredLanguage() != null
                ? currentUser.getPreferredLanguage()
                : Language.getDefault();
        form.setPreferredLanguage(currentLanguage.getCode());
        return profileEditView(currentUser);
    }

    private ModelAndView profileEditView(final User currentUser) {
        return new ModelAndView("profileEdit")
                .addObject("user", currentUser)
                .addObject("provinces", buildProvinceOptions())
                .addObject("languages", buildLanguageOptions());
    }

    private List<StringSelectOption> buildLanguageOptions() {
        final var locale = LocaleContextHolder.getLocale();
        return Arrays.stream(Language.values())
                .map(language -> new StringSelectOption(
                        language.getCode(),
                        messageSource.getMessage("language." + language.getCode(), null, locale)
                ))
                .toList();
    }

    private List<SelectOption> buildProvinceOptions() {
        return provinceService.getAll().stream()
                .map(province -> new SelectOption(
                        province.getId(),
                        messageSource.getMessage("province." + province.getName(), null, LocaleContextHolder.getLocale())
                ))
                .toList();
    }

    @PostMapping("/profile/edit")
    public ModelAndView editProfile(
            @CurrentUser User currentUser,
            @Valid @ModelAttribute("userEditForm") UserEditForm form,
            BindingResult errors
    ) {
        if (errors.hasErrors()) {
            LOGGER.debug("Validation failed for user {} profile edit", currentUser.getId());
            return profileEditView(currentUser);
        }

        // Extract image from form
        // TODO: Usar Bean Validation
        ImageData imageData = null;
        if (form.getProfilePicture() != null && !form.getProfilePicture().isEmpty()) {
            try {
                imageData = new ImageData(
                    form.getProfilePicture().getBytes(),
                    form.getProfilePicture().getOriginalFilename(),
                    form.getProfilePicture().getContentType()
                );
            } catch (java.io.IOException e) {
                LOGGER.error("Failed to read profile picture for user {}", currentUser.getId(), e);
                errors.rejectValue("profilePicture", "error.image.upload");
                return profileEditView(currentUser);
            }
        }

        LOGGER.info("User {} profile edit submitted", currentUser.getId());
        LOGGER.debug("User {} updating profile", currentUser.getUsername());

        // One service call does the whole profile update (display name, password, image, location,
        // language) in a single transaction. Persisting the language also updates the website locale,
        // since the LocaleResolver reads it from the refreshed authenticated principal below.
        final User updatedUser = userService.update(UserEditDto.builder()
            .user(currentUser)
            .newDisplayName(form.getDisplayName())
            .newPassword(form.getPassword())
            .newImageData(imageData)
            .updateLocationAndLanguage(true)
            .newProvinceId(form.getProvinceId())
            .newLocationDetail(form.getLocationDetail())
            .newPreferredLanguage(Language.fromCode(form.getPreferredLanguage()))
            .build());

        authHelper.update(updatedUser);

        return new ModelAndView("redirect:/profile");
    }

    /* REGISTER */

    @GetMapping("/register")
    public ModelAndView registerForm(@ModelAttribute("userForm") UserForm form) {
        LOGGER.debug("Accessing register form");
        return new ModelAndView("register");
    }

    @PostMapping("/register")
    public ModelAndView register(@Valid @ModelAttribute("userForm") UserForm form, BindingResult errors) {
        if (errors.hasErrors()) {
            LOGGER.debug("Validation failed for registration form");
            return registerForm(form);
        }

        LOGGER.info("User registration submitted: {}", form.getUsername());
        LOGGER.debug("Registering new user with username: {}", form.getUsername());

        // Extract image from form
        ImageData imageData = null;
        if (form.getProfilePicture() != null && !form.getProfilePicture().isEmpty()) {
            try {
                imageData = new ImageData(
                    form.getProfilePicture().getBytes(),
                    form.getProfilePicture().getOriginalFilename(),
                    form.getProfilePicture().getContentType()
                );
            } catch (java.io.IOException e) {
                LOGGER.error("Failed to read profile picture for registration of username {}", form.getUsername(), e);
                errors.rejectValue("profilePicture", "error.image.upload");
                return registerForm(form);
            }
        }

        User user = userService.create(new UserCreationDto(
            form.getUsername(),
            form.getDisplayName(),
            form.getEmail(),
            form.getPassword(),
            imageData
        ));

        emailVerificationService.sendVerificationEmail(user);

        return new ModelAndView("redirect:/verify?email=" + URLEncoder.encode(form.getEmail().trim().toLowerCase(), StandardCharsets.UTF_8));
    }

    /* LOGIN */

    @GetMapping("/login")
    public ModelAndView loginForm() {
        LOGGER.debug("Accessing login form");
        return new ModelAndView("login");
    }
}
