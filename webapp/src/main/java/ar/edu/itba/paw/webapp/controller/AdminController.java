package ar.edu.itba.paw.webapp.controller;

import ar.edu.itba.paw.model.Brand;
import ar.edu.itba.paw.model.BrandSort;
import ar.edu.itba.paw.model.ListingStatus;
import ar.edu.itba.paw.model.ModelSort;
import ar.edu.itba.paw.model.OfferStatusGroup;
import ar.edu.itba.paw.model.Role;
import ar.edu.itba.paw.model.User;
import ar.edu.itba.paw.service.AdminService;
import ar.edu.itba.paw.service.BrandModelRequestService;
import ar.edu.itba.paw.service.CatalogService;
import ar.edu.itba.paw.service.ListingService;
import ar.edu.itba.paw.service.OfferService;
import ar.edu.itba.paw.service.UserService;
import ar.edu.itba.paw.service.dto.BrandModelRequestAcceptanceDto;
import ar.edu.itba.paw.service.dto.ListingFilterDto;
import ar.edu.itba.paw.service.dto.OfferFilterDto;
import ar.edu.itba.paw.service.exception.BadParameterException;
import ar.edu.itba.paw.service.exception.NotFoundException;
import ar.edu.itba.paw.service.exception.SelfSuspendException;
import ar.edu.itba.paw.webapp.auth.CurrentUser;
import ar.edu.itba.paw.webapp.form.AdminBrandModelRequestAcceptForm;
import ar.edu.itba.paw.webapp.form.AdminCreateBrandForm;
import ar.edu.itba.paw.webapp.form.AdminGrantRoleForm;
import ar.edu.itba.paw.webapp.form.AdminListingFilterForm;
import ar.edu.itba.paw.webapp.form.AdminModelFilterForm;
import ar.edu.itba.paw.webapp.form.AdminModelForm;
import ar.edu.itba.paw.webapp.form.AdminOfferFilterForm;
import ar.edu.itba.paw.webapp.form.AdminRenameBrandForm;
import ar.edu.itba.paw.webapp.form.AdminReplaceBrandForm;
import ar.edu.itba.paw.webapp.form.AdminReplaceModelForm;
import ar.edu.itba.paw.webapp.form.AdminUserFilterForm;
import ar.edu.itba.paw.webapp.form.SelectOption;
import ar.edu.itba.paw.webapp.form.StringSelectOption;
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
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

@RequiredArgsConstructor
@Controller
@RequestMapping("/admin")
public class AdminController {
    private static final Logger LOGGER = LoggerFactory.getLogger(AdminController.class);
    private static final int DEFAULT_PAGE_SIZE = 10;
    private static final int BRAND_PAGE_SIZE = 10;
    private static final int MODEL_PAGE_SIZE = 10;
    private static final int REQUEST_PAGE_SIZE = 10;
    private static final Predicate<String> ADMIN_PATH_FILTER = RedirectHelper.hasPathPrefix("/admin");

    private final AdminService adminService;
    private final CatalogService catalogService;
    private final BrandModelRequestService brandModelRequestService;
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

    @GetMapping("/catalog")
    @PreAuthorize("hasRole('ADMIN')")
    public ModelAndView catalog(
            @CurrentUser User currentUser,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "sort", defaultValue = "BRAND_ASC") BrandSort sort,
            @RequestParam(value = "modelPage", defaultValue = "1") int modelPage,
            @RequestParam(value = "modelSort", defaultValue = "NAME_ASC") ModelSort modelSort,
            @RequestParam(value = "requestPage", defaultValue = "1") int requestPage,
            @ModelAttribute("renameBrandForm") AdminRenameBrandForm renameBrandForm,
            @ModelAttribute("createBrandForm") AdminCreateBrandForm createBrandForm,
            @ModelAttribute("replaceBrandForm") AdminReplaceBrandForm replaceBrandForm,
            @ModelAttribute("modelFilterForm") AdminModelFilterForm modelFilterForm,
            @ModelAttribute("createModelForm") AdminModelForm createModelForm,
            @ModelAttribute("editModelForm") AdminModelForm editModelForm,
            @ModelAttribute("replaceModelForm") AdminReplaceModelForm replaceModelForm,
            @ModelAttribute("acceptRequestForm") AdminBrandModelRequestAcceptForm acceptRequestForm
    ) {
        LOGGER.debug("Admin {} accessed catalog management page: brandPage={}, modelPage={}, requestPage={}", currentUser.getUsername(), page, modelPage, requestPage);
        modelFilterForm.setIsAutoSubmit(false);
        final var brandsPage = catalogService.getBrands(sort, page, BRAND_PAGE_SIZE);
        final var modelsPage = catalogService.getModels(
                modelFilterForm.getBrandId(), modelFilterForm.getSubcategoryId(),
                modelSort, modelPage, MODEL_PAGE_SIZE);
        final var requestsPage = brandModelRequestService.getRequests(requestPage, REQUEST_PAGE_SIZE);
        final List<Brand> allBrands = catalogService.getAllBrands();
        return new ModelAndView("admin/catalog")
                .addObject("brands", brandsPage.getContent())
                .addObject("brandsPage", brandsPage)
                .addObject("currentSort", sort)
                .addObject("brandReplacementOptions", catalogService.getBrandReplacementOptions())
                .addObject("brandOptions", allBrands)
                .addObject("subcategoryOptions", catalogService.getAllSubcategories().stream()
                        .map(subcategory -> new SelectOption(
                                subcategory.getId(),
                                subcategory.getCategory().getName() + " / " + subcategory.getName()))
                        .toList())
                .addObject("categoryOptions", catalogService.getAllCategories().stream()
                        .map(category -> new SelectOption(category.getId(), "category." + category.getName()))
                        .toList())
                .addObject("modelFilterSubcategoryOptions", modelFilterForm.getCategoryId() == null
                        ? List.of()
                        : catalogService.getSubcategoriesByCategory(modelFilterForm.getCategoryId()).stream()
                                .map(subcategory -> new SelectOption(
                                        subcategory.getId(), "subcategory." + subcategory.getName()))
                                .toList())
                .addObject("models", modelsPage.getContent())
                .addObject("modelsPage", modelsPage)
                .addObject("currentModelSort", modelSort)
                .addObject("modelReplacementOptions", catalogService.getModelReplacementOptions())
                .addObject("requests", requestsPage.getContent())
                .addObject("requestsPage", requestsPage);
    }

    @PostMapping("/catalog/requests/{id}/accept")
    @PreAuthorize("hasRole('ADMIN')")
    public ModelAndView acceptBrandModelRequest(
            @CurrentUser User currentUser,
            @PathVariable Long id,
            @Valid @ModelAttribute("acceptRequestForm") AdminBrandModelRequestAcceptForm form,
            BindingResult errors
    ) {
        LOGGER.info("Admin {} requested acceptance of brand/model request {}", currentUser.getUsername(), id);
        if (errors.hasErrors()) {
            return new ModelAndView("redirect:/admin/catalog?error=requestInvalid");
        }

        try {
            brandModelRequestService.accept(id, new BrandModelRequestAcceptanceDto(
                    form.getType(),
                    form.getBrandId(),
                    form.getNewBrandName(),
                    form.getModelName()
            ));
        } catch (BadParameterException e) {
            LOGGER.warn("Brand/model request {} could not be accepted: {}", id, e.getMessage());
            return new ModelAndView("redirect:/admin/catalog?error=requestInvalid");
        } catch (NotFoundException e) {
            LOGGER.warn("Brand/model request {} not found for acceptance", id);
            return new ModelAndView("redirect:/admin/catalog?error=requestNotFound");
        }

        return new ModelAndView("redirect:/admin/catalog?success=request_accepted");
    }

    @PostMapping("/catalog/requests/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ModelAndView rejectBrandModelRequest(
            @CurrentUser User currentUser,
            @PathVariable Long id
    ) {
        LOGGER.info("Admin {} requested rejection of brand/model request {}", currentUser.getUsername(), id);
        try {
            brandModelRequestService.reject(id);
        } catch (NotFoundException e) {
            LOGGER.warn("Brand/model request {} not found for rejection", id);
            return new ModelAndView("redirect:/admin/catalog?error=requestNotFound");
        }
        return new ModelAndView("redirect:/admin/catalog?success=request_rejected");
    }

    @PostMapping("/catalog/brands")
    @PreAuthorize("hasRole('ADMIN')")
    public ModelAndView createBrand(
            @CurrentUser User currentUser,
            @Valid @ModelAttribute("createBrandForm") AdminCreateBrandForm form,
            BindingResult errors
    ) {
        LOGGER.info("Admin {} requested creation of brand '{}'", currentUser.getUsername(), form.getName());
        if (errors.hasErrors()) {
            return new ModelAndView("redirect:/admin/catalog?error=brandInvalidName");
        }

        try {
            catalogService.createBrand(form.getName());
        } catch (BadParameterException e) {
            LOGGER.warn("Brand '{}' could not be created: {}", form.getName(), e.getMessage());
            return new ModelAndView("redirect:/admin/catalog?error=brandNameTaken");
        }

        return new ModelAndView("redirect:/admin/catalog?success=brand_created");
    }

    @PostMapping("/catalog/brands/{id}/replace")
    @PreAuthorize("hasRole('ADMIN')")
    public ModelAndView replaceBrand(
            @CurrentUser User currentUser,
            @PathVariable Long id,
            @Valid @ModelAttribute("replaceBrandForm") AdminReplaceBrandForm form,
            BindingResult errors
    ) {
        LOGGER.info("Admin {} requested replacement of brand {} with {}", currentUser.getUsername(), id, form.getReplacementBrandId());
        if (errors.hasErrors()) {
            return new ModelAndView("redirect:/admin/catalog?error=brandInvalidReplacement");
        }

        try {
            catalogService.replaceBrand(id, form.getReplacementBrandId());
        } catch (NotFoundException e) {
            LOGGER.warn("Brand replacement failed for {} -> {}: {}", id, form.getReplacementBrandId(), e.getMessage());
            return new ModelAndView("redirect:/admin/catalog?error=brandNotFound");
        } catch (BadParameterException e) {
            LOGGER.warn("Brand replacement failed for {} -> {}: {}", id, form.getReplacementBrandId(), e.getMessage());
            return new ModelAndView("redirect:/admin/catalog?error=brandReplaceSame");
        }

        return new ModelAndView("redirect:/admin/catalog?success=brand_replaced");
    }

    @PostMapping("/catalog/brands/{id}/rename")
    @PreAuthorize("hasRole('ADMIN')")
    public ModelAndView renameBrand(
            @CurrentUser User currentUser,
            @PathVariable Long id,
            @Valid @ModelAttribute("renameBrandForm") AdminRenameBrandForm form,
            BindingResult errors
    ) {
        LOGGER.info("Admin {} requested rename for brand {}", currentUser.getUsername(), id);
        if (errors.hasErrors()) {
            return new ModelAndView("redirect:/admin/catalog?error=brandInvalidName");
        }

        try {
            catalogService.renameBrand(id, form.getName());
        } catch (NotFoundException e) {
            LOGGER.warn("Brand {} not found for rename", id);
            return new ModelAndView("redirect:/admin/catalog?error=brandNotFound");
        } catch (BadParameterException e) {
            LOGGER.warn("Brand {} could not be renamed: {}", id, e.getMessage());
            return new ModelAndView("redirect:/admin/catalog?error=brandNameTaken");
        }

        return new ModelAndView("redirect:/admin/catalog?success=brand_renamed");
    }

    @PostMapping("/catalog/models")
    @PreAuthorize("hasRole('ADMIN')")
    public ModelAndView createModel(
            @CurrentUser User currentUser,
            @Valid @ModelAttribute("createModelForm") AdminModelForm form,
            BindingResult errors
    ) {
        LOGGER.info("Admin {} requested creation of model '{}'", currentUser.getUsername(), form.getName());
        if (errors.hasErrors()) {
            return new ModelAndView("redirect:/admin/catalog?error=modelInvalid");
        }
        try {
            catalogService.createModel(form.getName(), form.getBrandId(), form.getSubcategoryId());
        } catch (BadParameterException e) {
            LOGGER.warn("Model '{}' could not be created: {}", form.getName(), e.getMessage());
            return new ModelAndView("redirect:/admin/catalog?error=modelNameTaken");
        } catch (NotFoundException e) {
            LOGGER.warn("Model '{}' references missing catalog data: {}", form.getName(), e.getMessage());
            return new ModelAndView("redirect:/admin/catalog?error=modelNotFound");
        }
        return new ModelAndView("redirect:/admin/catalog?success=model_created");
    }

    @PostMapping("/catalog/models/{id}/edit")
    @PreAuthorize("hasRole('ADMIN')")
    public ModelAndView editModel(
            @CurrentUser User currentUser,
            @PathVariable Long id,
            @Valid @ModelAttribute("editModelForm") AdminModelForm form,
            BindingResult errors
    ) {
        LOGGER.info("Admin {} requested edit of model {}", currentUser.getUsername(), id);
        if (errors.hasErrors()) {
            return new ModelAndView("redirect:/admin/catalog?error=modelInvalid");
        }
        try {
            catalogService.updateModel(id, form.getName(), form.getBrandId(), form.getSubcategoryId());
        } catch (BadParameterException e) {
            LOGGER.warn("Model {} could not be updated: {}", id, e.getMessage());
            return new ModelAndView("redirect:/admin/catalog?error=modelNameTaken");
        } catch (NotFoundException e) {
            LOGGER.warn("Model {} not found for update", id);
            return new ModelAndView("redirect:/admin/catalog?error=modelNotFound");
        }
        return new ModelAndView("redirect:/admin/catalog?success=model_updated");
    }

    @PostMapping("/catalog/models/{id}/delete")
    @PreAuthorize("hasRole('ADMIN')")
    public ModelAndView deleteModel(
            @CurrentUser User currentUser,
            @PathVariable Long id
    ) {
        LOGGER.info("Admin {} requested deletion of model {}", currentUser.getUsername(), id);
        try {
            catalogService.deleteModel(id);
        } catch (BadParameterException e) {
            LOGGER.warn("Model {} could not be deleted: {}", id, e.getMessage());
            return new ModelAndView("redirect:/admin/catalog?error=modelHasListings");
        } catch (NotFoundException e) {
            LOGGER.warn("Model {} not found for deletion", id);
            return new ModelAndView("redirect:/admin/catalog?error=modelNotFound");
        }
        return new ModelAndView("redirect:/admin/catalog?success=model_deleted");
    }

    @PostMapping("/catalog/models/{id}/replace")
    @PreAuthorize("hasRole('ADMIN')")
    public ModelAndView replaceModel(
            @CurrentUser User currentUser,
            @PathVariable Long id,
            @Valid @ModelAttribute("replaceModelForm") AdminReplaceModelForm form,
            BindingResult errors
    ) {
        LOGGER.info("Admin {} requested replacement of model {} with {}", currentUser.getUsername(), id, form.getReplacementModelId());
        if (errors.hasErrors()) {
            return new ModelAndView("redirect:/admin/catalog?error=modelInvalidReplacement");
        }
        try {
            catalogService.replaceModel(id, form.getReplacementModelId());
        } catch (NotFoundException e) {
            LOGGER.warn("Model replacement failed for {} -> {}: {}", id, form.getReplacementModelId(), e.getMessage());
            return new ModelAndView("redirect:/admin/catalog?error=modelNotFound");
        } catch (BadParameterException e) {
            LOGGER.warn("Model replacement failed for {} -> {}: {}", id, form.getReplacementModelId(), e.getMessage());
            return new ModelAndView("redirect:/admin/catalog?error=modelReplaceSame");
        }
        return new ModelAndView("redirect:/admin/catalog?success=model_replaced");
    }

    @PostMapping("/catalog/brands/{id}/delete")
    @PreAuthorize("hasRole('ADMIN')")
    public ModelAndView deleteBrand(
            @CurrentUser User currentUser,
            @PathVariable Long id
    ) {
        LOGGER.info("Admin {} requested deletion of brand {}", currentUser.getUsername(), id);
        try {
            catalogService.deleteBrand(id);
        } catch (NotFoundException e) {
            LOGGER.warn("Brand {} not found for deletion", id);
            return new ModelAndView("redirect:/admin/catalog?error=brandNotFound");
        } catch (BadParameterException e) {
            LOGGER.warn("Brand {} could not be deleted: {}", id, e.getMessage());
            return new ModelAndView("redirect:/admin/catalog?error=brandHasListings");
        }

        return new ModelAndView("redirect:/admin/catalog?success=brand_deleted");
    }

    @GetMapping("/listings")
    @PreAuthorize("hasRole('ADMIN')")
    public ModelAndView listings(
            @CurrentUser User currentUser,
            @Valid @ModelAttribute("filterForm") AdminListingFilterForm filterForm,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @ModelAttribute("takeDownListingForm") AdminTakeDownListingForm takeDownListingForm
    ) {
        LOGGER.debug("Admin {} accessed listings page: page={}, query={}, status={}", currentUser.getUsername(), page, filterForm.getQuery(), filterForm.getStatus());
        ModelAndView mav = new ModelAndView("admin/listings");
        var filter = ListingFilterDto.builder()
                .query(filterForm.getQuery())
                .status(filterForm.getStatus())
                .page(page)
                .pageSize(DEFAULT_PAGE_SIZE)
                .build();
        mav.addObject("listingsPage", listingService.search(filter));
        mav.addObject("statusOptions", Arrays.stream(ListingStatus.values())
                .map(status -> new StringSelectOption(status.name(), "listing.status." + status.name()))
                .toList());
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

        try {
            adminService.takeDownListing(form.getListingId());
        } catch (NotFoundException e) {
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
        String fallback = "/admin/listings?error=listingNotFound";
        try {
            adminService.takeDownListing(id);
            fallback = "/admin/listings?success=listing_takedown";
        } catch (NotFoundException e) {
            LOGGER.warn("Listing {} not found for takedown", id);
        }
        final var target = RedirectHelper.resolvePath(redirectUrl, fallback, ADMIN_PATH_FILTER);
        return new ModelAndView("redirect:" + target);
    }

    @GetMapping("/offers")
    @PreAuthorize("hasRole('ADMIN')")
    public ModelAndView offers(
            @CurrentUser User currentUser,
            @ModelAttribute("filterForm") AdminOfferFilterForm filterForm,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @ModelAttribute("takeDownOfferForm") AdminTakeDownOfferForm takeDownOfferForm
    ) {
        LOGGER.debug("Admin {} accessed offers page: page={}, statusGroup={}", currentUser.getUsername(), page, filterForm.getStatusGroup());
        ModelAndView mav = new ModelAndView("admin/offers");
        var filter = OfferFilterDto.builder()
                .statusGroup(filterForm.getStatusGroup())
                .page(page)
                .pageSize(DEFAULT_PAGE_SIZE)
                .build();
        mav.addObject("offersPage", offerService.get(filter));
        mav.addObject("statusGroupOptions", Arrays.stream(OfferStatusGroup.values())
                .map(statusGroup -> new StringSelectOption(statusGroup.name(), "offer.statusGroup." + statusGroup.getStatus()))
                .toList());
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

        try {
            adminService.takeDownOffer(form.getOfferId());
        } catch (NotFoundException e) {
            LOGGER.warn("Offer {} not found for takedown", form.getOfferId());
            errors.rejectValue("offerId", "admin.error.offerNotFound");
            return new ModelAndView("admin/dashboard")
                    .addObject("takeDownListingForm", new AdminTakeDownListingForm())
                    .addObject("suspendUserForm", new AdminSuspendUserForm())
                    .addObject("grantRoleForm", new AdminGrantRoleForm());
        } catch (BadParameterException e) {
            LOGGER.warn("Offer {} cannot be taken down: {}", form.getOfferId(), e.getMessage());
            errors.rejectValue("offerId", "admin.error.offerCannotBeCanceled");
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
        String fallback = "/admin/offers?error=offerNotFound";
        try {
            adminService.takeDownOffer(id);
            fallback = "/admin/offers?success=offer_takedown";
        } catch (NotFoundException e) {
            LOGGER.warn("Offer {} not found for takedown", id);
        } catch (BadParameterException e) {
            LOGGER.warn("Offer {} cannot be taken down: {}", id, e.getMessage());
            fallback = "/admin/offers?error=offerCannotBeCanceled";
        }
        final var target = RedirectHelper.resolvePath(redirectUrl, fallback, ADMIN_PATH_FILTER);
        return new ModelAndView("redirect:" + target);
    }

    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ModelAndView users(
            @CurrentUser User currentUser,
            @Valid @ModelAttribute("filterForm") AdminUserFilterForm filterForm,
            @ModelAttribute("suspendUserForm") AdminSuspendUserForm suspendUserForm,
            @ModelAttribute("grantRoleForm") AdminGrantRoleForm grantRoleForm
    ) {
        final var username = filterForm.getUsername();
        LOGGER.debug("Admin {} accessed users management page: username={}", currentUser.getUsername(), username);
        ModelAndView mav = new ModelAndView("admin/users");
        if (username != null && !username.isBlank()) {
            Optional<User> targetUser = userService.getByUsernameOrEmail(username);
            if (targetUser.isPresent()) {
                User user = targetUser.get();
                mav.addObject("targetUser", user);
                mav.addObject("targetUserRoles", user.getRoles());
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
