package ar.edu.itba.paw.webapp.controller;

import ar.edu.itba.paw.model.Role;
import ar.edu.itba.paw.model.User;
import ar.edu.itba.paw.service.AdminService;
import ar.edu.itba.paw.service.ListingService;
import ar.edu.itba.paw.service.OfferService;
import ar.edu.itba.paw.service.UserService;
import ar.edu.itba.paw.service.dto.ListingFilterDto;
import ar.edu.itba.paw.service.dto.OfferFilterDto;
import ar.edu.itba.paw.service.exception.SelfSuspendException;
import ar.edu.itba.paw.webapp.auth.CurrentUser;
import ar.edu.itba.paw.webapp.form.AdminGrantRoleForm;
import ar.edu.itba.paw.webapp.form.AdminSuspendUserForm;
import ar.edu.itba.paw.webapp.form.AdminTakeDownListingForm;
import ar.edu.itba.paw.webapp.form.AdminTakeDownOfferForm;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import javax.validation.Valid;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

@RequiredArgsConstructor
@Controller
@RequestMapping("/admin")
public class AdminController {
    private static final Logger LOGGER = LoggerFactory.getLogger(AdminController.class);
    private static final int DEFAULT_PAGE_SIZE = 10;
    private static final Predicate<String> ADMIN_PATH_FILTER = RedirectHelper.hasPathPrefix("/admin");

    private final AdminService adminService;
    private final ListingService listingService;
    private final OfferService offerService;
    private final UserService userService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ModelAndView dashboard(
            @CurrentUser User currentUser,
            @ModelAttribute("takeDownListingForm") AdminTakeDownListingForm takeDownListingForm,
            @ModelAttribute("takeDownOfferForm") AdminTakeDownOfferForm takeDownOfferForm,
            @ModelAttribute("suspendUserForm") AdminSuspendUserForm suspendUserForm,
            @ModelAttribute("grantRoleForm") AdminGrantRoleForm grantRoleForm
    ) {
        LOGGER.info("Admin {} accessed dashboard", currentUser.getUsername());
        return new ModelAndView("admin/dashboard");
    }

    @GetMapping("/listings")
    @PreAuthorize("hasRole('ADMIN')")
    public ModelAndView listings(
            @CurrentUser User currentUser,
            @RequestParam(value = "query", required = false) String query,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @ModelAttribute("takeDownListingForm") AdminTakeDownListingForm takeDownListingForm
    ) {
        LOGGER.debug("Admin {} accessed listings page: page={}, query={}, status={}", currentUser.getUsername(), page, query, status);
        ModelAndView mav = new ModelAndView("admin/listings");
        var filter = ListingFilterDto.builder()
                .query(query)
                .status(status)
                .page(page)
                .pageSize(DEFAULT_PAGE_SIZE)
                .build();
        mav.addObject("listingsPage", listingService.search(filter));
        mav.addObject("currentQuery", query);
        mav.addObject("currentStatus", status);
        return mav;
    }

    @PostMapping("/listings/takedown")
    @PreAuthorize("hasRole('ADMIN')")
    public ModelAndView takeDownListingByForm(
            @CurrentUser User currentUser,
            @Valid @ModelAttribute("takeDownListingForm") AdminTakeDownListingForm form,
            BindingResult errors,
            @RequestParam(value = "redirect", required = false) String redirectUrl
    ) {
        LOGGER.info("Admin {} requested takedown for listingId={}", currentUser.getUsername(), form.getListingId());
        if (errors.hasErrors()) {
            return new ModelAndView("admin/dashboard")
                    .addObject("takeDownOfferForm", new AdminTakeDownOfferForm())
                    .addObject("suspendUserForm", new AdminSuspendUserForm())
                    .addObject("grantRoleForm", new AdminGrantRoleForm());
        }

        boolean success = adminService.takeDownListing(form.getListingId());
        if (!success) {
            LOGGER.warn("Listing {} not found for takedown", form.getListingId());
            errors.rejectValue("listingId", "admin.error.listingNotFound");
            return new ModelAndView("admin/dashboard")
                    .addObject("takeDownOfferForm", new AdminTakeDownOfferForm())
                    .addObject("suspendUserForm", new AdminSuspendUserForm())
                    .addObject("grantRoleForm", new AdminGrantRoleForm());
        }

        final var target = RedirectHelper.resolvePath(
                redirectUrl,
                "/admin/listings?success=listing_takedown",
                ADMIN_PATH_FILTER
        );
        return new ModelAndView("redirect:" + target);
    }

    @PostMapping("/listings/{id}/takedown")
    @PreAuthorize("hasRole('ADMIN')")
    public ModelAndView takeDownListingById(
            @CurrentUser User currentUser,
            @PathVariable Long id,
            @RequestParam(value = "redirect", required = false) String redirectUrl
    ) {
        LOGGER.info("Admin {} took down listing id={}", currentUser.getUsername(), id);
        boolean success = adminService.takeDownListing(id);
        final var fallback = success
                ? "/admin/listings?success=listing_takedown"
                : "/admin/listings?error=listingNotFound";
        final var target = RedirectHelper.resolvePath(redirectUrl, fallback, ADMIN_PATH_FILTER);
        return new ModelAndView("redirect:" + target);
    }

    @GetMapping("/offers")
    @PreAuthorize("hasRole('ADMIN')")
    public ModelAndView offers(
            @CurrentUser User currentUser,
            @RequestParam(value = "statusGroup", required = false) String statusGroup,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @ModelAttribute("takeDownOfferForm") AdminTakeDownOfferForm takeDownOfferForm
    ) {
        LOGGER.debug("Admin {} accessed offers page: page={}, statusGroup={}", currentUser.getUsername(), page, statusGroup);
        ModelAndView mav = new ModelAndView("admin/offers");
        var filter = OfferFilterDto.builder()
                .statusGroup(statusGroup)
                .page(page)
                .pageSize(DEFAULT_PAGE_SIZE)
                .build();
        mav.addObject("offersPage", offerService.get(filter));
        mav.addObject("currentStatusGroup", statusGroup);
        return mav;
    }

    @PostMapping("/offers/takedown")
    @PreAuthorize("hasRole('ADMIN')")
    public ModelAndView takeDownOfferByForm(
            @CurrentUser User currentUser,
            @Valid @ModelAttribute("takeDownOfferForm") AdminTakeDownOfferForm form,
            BindingResult errors,
            @RequestParam(value = "redirect", required = false) String redirectUrl
    ) {
        LOGGER.info("Admin {} requested takedown for offerId={}", currentUser.getUsername(), form.getOfferId());
        if (errors.hasErrors()) {
            return new ModelAndView("admin/dashboard")
                    .addObject("takeDownListingForm", new AdminTakeDownListingForm())
                    .addObject("suspendUserForm", new AdminSuspendUserForm())
                    .addObject("grantRoleForm", new AdminGrantRoleForm());
        }

        boolean success = adminService.takeDownOffer(form.getOfferId());
        if (!success) {
            LOGGER.warn("Offer {} could not be taken down", form.getOfferId());
            errors.rejectValue("offerId", "admin.error.offerNotFound");
            return new ModelAndView("admin/dashboard")
                    .addObject("takeDownListingForm", new AdminTakeDownListingForm())
                    .addObject("suspendUserForm", new AdminSuspendUserForm())
                    .addObject("grantRoleForm", new AdminGrantRoleForm());
        }

        final var target = RedirectHelper.resolvePath(
                redirectUrl,
                "/admin/offers?success=offer_takedown",
                ADMIN_PATH_FILTER
        );
        return new ModelAndView("redirect:" + target);
    }

    @PostMapping("/offers/{id}/takedown")
    @PreAuthorize("hasRole('ADMIN')")
    public ModelAndView takeDownOfferById(
            @CurrentUser User currentUser,
            @PathVariable Long id,
            @RequestParam(value = "redirect", required = false) String redirectUrl
    ) {
        LOGGER.info("Admin {} took down offer id={}", currentUser.getUsername(), id);
        boolean success = adminService.takeDownOffer(id);
        final var fallback = success
                ? "/admin/offers?success=offer_takedown"
                : "/admin/offers?error=offerNotFound";
        final var target = RedirectHelper.resolvePath(redirectUrl, fallback, ADMIN_PATH_FILTER);
        return new ModelAndView("redirect:" + target);
    }

    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ModelAndView users(
            @CurrentUser User currentUser,
            @RequestParam(value = "username", required = false) String username,
            @ModelAttribute("suspendUserForm") AdminSuspendUserForm suspendUserForm,
            @ModelAttribute("grantRoleForm") AdminGrantRoleForm grantRoleForm
    ) {
        LOGGER.debug("Admin {} accessed users management page: username={}", currentUser.getUsername(), username);
        ModelAndView mav = new ModelAndView("admin/users");
        if (username != null && !username.isBlank()) {
            Optional<User> targetUser = userService.getByUsernameOrEmail(username);
            mav.addObject("searchedUsername", username);
            if (targetUser.isPresent()) {
                User user = targetUser.get();
                mav.addObject("targetUser", user);
                List<Role> roles = userService.getRoles(user);
                mav.addObject("targetUserRoles", roles);
                mav.addObject("isTargetUserAdmin", user.hasRole(Role.ADMIN));
                suspendUserForm.setUsername(user.getUsername());
                grantRoleForm.setUsername(user.getUsername());
            } else {
                mav.addObject("userNotFound", true);
            }
        }
        return mav;
    }

    @PostMapping("/users/suspend")
    @PreAuthorize("hasRole('ADMIN')")
    public ModelAndView suspendUser(
            @CurrentUser User currentUser,
            @Valid @ModelAttribute("suspendUserForm") AdminSuspendUserForm form,
            BindingResult errors,
            @ModelAttribute("grantRoleForm") AdminGrantRoleForm grantRoleForm
    ) {
        final var formUsernameOrEmail = form.getUsernameOrEmail();
        LOGGER.info("Admin {} requested user suspension for '{}'", currentUser.getUsername(), formUsernameOrEmail);

        if (errors.hasErrors()) {
            return new ModelAndView("admin/users");
        }

        final User user;
        try {
            user = adminService.suspendUser(currentUser, formUsernameOrEmail);
        } catch (SelfSuspendException e) {
            LOGGER.warn("Admin {} prevented from suspending theirself", currentUser.getUsername());
            errors.rejectValue("username", "admin.users.error.selfSuspend");
            return new ModelAndView("admin/users");
        }

        LOGGER.info("Admin {} logically suspended user {}", currentUser.getUsername(), user.getUsername());
        return new ModelAndView("redirect:/admin/users?success=user_suspended&username=" + URLEncoder.encode(user.getUsername(), StandardCharsets.UTF_8));
    }

    @PostMapping("/users/unsuspend")
    @PreAuthorize("hasRole('ADMIN')")
    public ModelAndView unsuspendUser(
            @CurrentUser User currentUser,
            @Valid @ModelAttribute("suspendUserForm") AdminSuspendUserForm form,
            BindingResult errors,
            @ModelAttribute("grantRoleForm") AdminGrantRoleForm grantRoleForm
    ) {
        final var formUsernameOrEmail = form.getUsernameOrEmail();
        LOGGER.info("Admin {} requested user unsuspension for '{}'", currentUser.getUsername(), formUsernameOrEmail);

        if (errors.hasErrors()) {
            return new ModelAndView("admin/users");
        }

        User user = adminService.unsuspendUser(formUsernameOrEmail);

        LOGGER.info("Admin {} unsuspended user {}", currentUser.getUsername(), user.getUsername());
        return new ModelAndView("redirect:/admin/users?success=user_unsuspended&username=" + URLEncoder.encode(user.getUsername(), StandardCharsets.UTF_8));
    }

    @PostMapping("/users/grant-admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ModelAndView grantAdminRole(
            @CurrentUser User currentUser,
            @Valid @ModelAttribute("grantRoleForm") AdminGrantRoleForm form,
            BindingResult errors,
            @ModelAttribute("suspendUserForm") AdminSuspendUserForm suspendUserForm
    ) {
        LOGGER.info("Admin {} requested granting ADMIN role for '{}'", currentUser.getUsername(), form.getUsernameOrEmail());
        if (errors.hasErrors()) {
            return new ModelAndView("admin/users");
        }

        User user = adminService.grantRole(form.getUsernameOrEmail(), Role.ADMIN);

        LOGGER.info("Admin {} granted ADMIN role to user {}", currentUser.getUsername(), user.getUsername());
        return new ModelAndView("redirect:/admin/users?success=role_granted&username=" + URLEncoder.encode(user.getUsername(), StandardCharsets.UTF_8));
    }
}
