package ar.edu.itba.paw.webapp.controller;

import ar.edu.itba.paw.model.*;
import ar.edu.itba.paw.service.ListingService;
import ar.edu.itba.paw.service.RatingService;
import ar.edu.itba.paw.service.ProvinceService;
import ar.edu.itba.paw.service.UserService;
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
import org.springframework.security.access.prepost.PreAuthorize;
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

@RequiredArgsConstructor
@Controller
public class UserController {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserController.class);

    private final UserService userService;
    private final ListingService listingService;
    private final RatingService ratingService;
    private final ProvinceService provinceService;
    private final AuthHelper authHelper;

    private static final int PROFILE_LISTINGS_PAGE_SIZE = 5;
    private static final int PROFILE_RATINGS_PAGE_SIZE = 5;

    /* PROFILE */

    @GetMapping("/profile/{id}")
    @PreAuthorize("permitAll()")
    public ModelAndView profile(@PathVariable Long id, @CurrentUser(required = false) User currentUser,
                                @ModelAttribute("ratingFilterForm") RatingFilterForm ratingFilterForm) {
        LOGGER.debug("Accessing profile for user id: {}", id);
        final User user = userService.getById(id).orElseThrow(() -> UserNotFoundException.byId(id));
        final boolean isSelfRequest = user.isSameUser(currentUser);

        final var filter = ListingFilterDto.builder()
                .sort(ListingSort.RECENT)
                .creatorId(user.getId())
                .status(ListingStatus.ACTIVE)
                .page(1)
                .pageSize(PROFILE_LISTINGS_PAGE_SIZE)
                .build();

        final var listingPage = listingService.search(filter);
        final var listings = listingPage.getContent();

        RatingRole role = ratingFilterForm.getRole();
        if (role == null) {
            role = RatingRole.SELLER;
            ratingFilterForm.setRole(role);
        }

        OfferRating type = ratingFilterForm.getType();

        int page = ratingFilterForm.getPage() != null ? ratingFilterForm.getPage() : 1;

        var ratingFilter = RatingFilter.builder()
                .ratedId(user.getId())
                .roles(List.of(role))
                .types(type != null ? List.of(type) : null)
                .page(page)
                .pageSize(PROFILE_RATINGS_PAGE_SIZE)
                .build();

        final var ratingPage = ratingService.get(ratingFilter);
        final var ratings = ratingPage.getContent();

        final var roleOptions = Arrays.asList(
                new StringSelectOption(RatingRole.SELLER.name(), "profile.ratings.filter.seller"),
                new StringSelectOption(RatingRole.BUYER.name(), "profile.ratings.filter.buyer")
        );
        final var typeOptions = Arrays.asList(
                new StringSelectOption("", "profile.ratings.filter.all"),
                new StringSelectOption(OfferRating.POSITIVE.name(), "profile.ratings.filter.positive"),
                new StringSelectOption(OfferRating.NEUTRAL.name(), "profile.ratings.filter.neutral"),
                new StringSelectOption(OfferRating.NEGATIVE.name(), "profile.ratings.filter.negative")
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
    @PreAuthorize("isAuthenticated()")
    public ModelAndView currentUserProfile(@CurrentUser User currentUser) {
        LOGGER.debug("Accessing current user profile for user {}", currentUser.getId());
        return new ModelAndView("redirect:/profile/" + currentUser.getId());
    }

    /* PROFILE EDIT */

    @GetMapping("/profile/edit")
    @PreAuthorize("isAuthenticated()")
    public ModelAndView editProfileForm(@CurrentUser User currentUser, @ModelAttribute("userEditForm") UserEditForm form) {
        LOGGER.debug("Accessing profile edit form for user {}", currentUser.getId());
        form.setDisplayName(currentUser.getDisplayName());
        form.setProvinceId(currentUser.getProvince().map(Province::getId).orElse(null));
        form.setLocationDetail(currentUser.getLocationDetail().orElse(null));
        form.setPreferredLanguage(currentUser.getPreferredLanguageOrDefault());
        return profileEditView(currentUser);
    }

    private ModelAndView profileEditView(final User currentUser) {
        return new ModelAndView("profileEdit")
                .addObject("user", currentUser)
                .addObject("provinces", buildProvinceOptions())
                .addObject("languages", buildLanguageOptions());
    }

    private List<StringSelectOption> buildLanguageOptions() {
        return Arrays.stream(Language.values())
                .map(language -> new StringSelectOption(
                        language.name(),
                        "language." + language.getCode()
                ))
                .toList();
    }

    private List<SelectOption> buildProvinceOptions() {
        return provinceService.getAll().stream()
                .map(province -> new SelectOption(
                        province.getId(),
                        "province." + province.getName()
                ))
                .toList();
    }

    @PostMapping("/profile/edit")
    @PreAuthorize("isAuthenticated()")
    public ModelAndView editProfile(
            @CurrentUser User currentUser,
            @Valid @ModelAttribute("userEditForm") UserEditForm form,
            BindingResult errors
    ) {
        if (errors.hasErrors()) {
            LOGGER.debug("Validation failed for user {} profile edit", currentUser.getId());
            return profileEditView(currentUser);
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
            .newImageData(form.getProfilePicture().orElse(null))
            .updateLocationAndLanguage(true)
            .newProvinceId(form.getProvinceId())
            .newLocationDetail(form.getLocationDetail())
            .newPreferredLanguage(form.getPreferredLanguage())
            .build());

        authHelper.update(updatedUser);

        return new ModelAndView("redirect:/profile");
    }

    /* REGISTER */

    @GetMapping("/register")
    @PreAuthorize("not isAuthenticated()")
    public ModelAndView registerForm(@ModelAttribute("userForm") UserForm form) {
        LOGGER.debug("Accessing register form");
        return new ModelAndView("register");
    }

    @PostMapping("/register")
    @PreAuthorize("not isAuthenticated()")
    public ModelAndView register(@Valid @ModelAttribute("userForm") UserForm form, BindingResult errors) {
        if(errors.hasErrors()) {
            LOGGER.debug("Validation failed for registration form");
            return registerForm(form);
        }

        LOGGER.info("User registration submitted: {}", form.getUsername());
        LOGGER.debug("Registering new user with username: {}", form.getUsername());

        final User created = userService.create(new UserCreationDto(
            form.getUsername(),
            form.getDisplayName(),
            form.getEmail(),
            form.getPassword(),
            form.getProfilePicture().orElse(null)
        ));

        return new ModelAndView("redirect:/verify?email=" + URLEncoder.encode(created.getEmail(), StandardCharsets.UTF_8));
    }

    /* LOGIN */

    @GetMapping("/login")
    @PreAuthorize("not isAuthenticated()")
    public ModelAndView loginForm() {
        LOGGER.debug("Accessing login form");
        return new ModelAndView("login");
    }
}
