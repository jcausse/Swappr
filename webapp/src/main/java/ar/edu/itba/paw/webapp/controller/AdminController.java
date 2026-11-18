package ar.edu.itba.paw.webapp.controller;

import ar.edu.itba.paw.model.Brand;
import ar.edu.itba.paw.model.BrandInfo;
import ar.edu.itba.paw.model.ModelInfo;
import ar.edu.itba.paw.model.BrandSort;
import ar.edu.itba.paw.model.ListingStatus;
import ar.edu.itba.paw.model.ModelSort;
import ar.edu.itba.paw.model.OfferStatusGroup;
import ar.edu.itba.paw.model.Role;
import ar.edu.itba.paw.model.Subcategory;
import ar.edu.itba.paw.model.User;
import ar.edu.itba.paw.service.AdminService;
import ar.edu.itba.paw.service.BrandModelRequestService;
import ar.edu.itba.paw.service.CatalogService;
import ar.edu.itba.paw.service.ListingService;
import ar.edu.itba.paw.service.OfferService;
import ar.edu.itba.paw.service.UserService;
import ar.edu.itba.paw.service.exception.BrandAlreadyExistsException;
import ar.edu.itba.paw.service.exception.ListingNotCancelableException;
import ar.edu.itba.paw.service.exception.ModelAlreadyExistsException;
import ar.edu.itba.paw.service.exception.NotFoundException;
import ar.edu.itba.paw.service.exception.OfferNotCancelableException;
import ar.edu.itba.paw.service.exception.SelfSuspendException;
import ar.edu.itba.paw.webapp.auth.CurrentUser;
import ar.edu.itba.paw.webapp.form.AdminBrandModelRequestAcceptForm;
import ar.edu.itba.paw.webapp.form.AdminCreateBrandForm;
import ar.edu.itba.paw.webapp.form.AdminCreateModelFilterForm;
import ar.edu.itba.paw.webapp.form.AdminGrantRoleForm;
import ar.edu.itba.paw.webapp.form.AdminListingFilterForm;
import ar.edu.itba.paw.webapp.form.AdminModelFilterForm;
import ar.edu.itba.paw.webapp.form.AdminModelForm;
import ar.edu.itba.paw.webapp.form.AdminOfferFilterForm;
import ar.edu.itba.paw.webapp.form.AdminRenameBrandForm;
import ar.edu.itba.paw.webapp.form.AdminReplaceBrandForm;
import ar.edu.itba.paw.webapp.form.AdminReplaceModelForm;
import ar.edu.itba.paw.webapp.form.AdminUserFilterForm;
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
import org.springframework.web.util.UriComponentsBuilder;

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
        LOGGER.info("Admin {} accessed dashboard", currentUser.getId());
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
            @ModelAttribute("createModelFilterForm") AdminCreateModelFilterForm createModelFilterForm,
            @ModelAttribute("createModelForm") AdminModelForm createModelForm,
            @ModelAttribute("editModelForm") AdminModelForm editModelForm,
            @ModelAttribute("replaceModelForm") AdminReplaceModelForm replaceModelForm,
            @ModelAttribute("acceptRequestForm") AdminBrandModelRequestAcceptForm acceptRequestForm
    ) {
        LOGGER.debug("Admin {} accessed catalog management page: brandPage={}, modelPage={}, requestPage={}", currentUser.getId(), page, modelPage, requestPage);
        final var brandsPage = catalogService.getBrands(sort, page, BRAND_PAGE_SIZE);
        final var modelsPage = catalogService.getModels(
                modelFilterForm.getFilterBrandId(), modelFilterForm.getFilterCategoryId(),
                modelFilterForm.getFilterSubcategoryId(),
                modelSort, modelPage, MODEL_PAGE_SIZE);
        final var requestsPage = brandModelRequestService.getRequests(requestPage, REQUEST_PAGE_SIZE);
        final List<Brand> allBrands = catalogService.getAllBrands();
        final var categories = catalogService.getAllCategories();
        return new ModelAndView("admin/catalog")
                .addObject("brands", brandsPage.getContent())
                .addObject("brandsPage", brandsPage)
                .addObject("currentSort", sort)
                .addObject("brandReplacementOptions", catalogService.getBrandReplacementOptions(
                        brandsPage.getContent().stream().map(BrandInfo::getId).toList()))
                .addObject("brandOptions", allBrands)
                .addObject("editModelSubcategories", catalogService.getAllSubcategories())
                .addObject("categories", categories)
                .addObject("modelFilterSubcategories", subcategoryOptions(modelFilterForm.getFilterCategoryId()))
                .addObject("createModelSubcategories", subcategoryOptions(createModelFilterForm.getCategoryId()))
                .addObject("models", modelsPage.getContent())
                .addObject("modelsPage", modelsPage)
                .addObject("currentModelSort", modelSort)
                .addObject("modelReplacementOptions", catalogService.getModelReplacementOptions(
                        modelsPage.getContent().stream().map(ModelInfo::getId).toList()))
                .addObject("requests", requestsPage.getContent())
                .addObject("requestsPage", requestsPage)
                .addObject("catalogReturnUrl", buildCatalogReturnUrl(
                        page, sort, modelPage, modelSort, requestPage, modelFilterForm, createModelFilterForm));
    }

    private String buildCatalogReturnUrl(
            final int page,
            final BrandSort sort,
            final int modelPage,
            final ModelSort modelSort,
            final int requestPage,
            final AdminModelFilterForm modelFilterForm,
            final AdminCreateModelFilterForm createModelFilterForm
    ) {
        final var builder = UriComponentsBuilder.fromPath("/admin/catalog")
                .queryParam("page", page)
                .queryParam("sort", sort.name())
                .queryParam("modelPage", modelPage)
                .queryParam("modelSort", modelSort.name())
                .queryParam("requestPage", requestPage);
        addQueryParamIfPresent(builder, "filterBrandId", modelFilterForm.getFilterBrandId());
        addQueryParamIfPresent(builder, "filterCategoryId", modelFilterForm.getFilterCategoryId());
        addQueryParamIfPresent(builder, "filterSubcategoryId", modelFilterForm.getFilterSubcategoryId());
        addQueryParamIfPresent(builder, "categoryId", createModelFilterForm.getCategoryId());
        return builder.build().encode().toUriString();
    }

    private void addQueryParamIfPresent(
            final UriComponentsBuilder builder,
            final String name,
            final Object value
    ) {
        if (value != null) {
            builder.queryParam(name, value);
        }
    }

    private ModelAndView catalogSuccess(final String redirectUrl, final String code) {
        return catalogOutcome(redirectUrl, "success", code);
    }

    private ModelAndView catalogError(final String redirectUrl, final String code) {
        return catalogOutcome(redirectUrl, "error", code);
    }

    private ModelAndView catalogOutcome(final String redirectUrl, final String key, final String code) {
        final var target = RedirectHelper.resolvePath(redirectUrl, "/admin/catalog", ADMIN_PATH_FILTER);
        return new ModelAndView("redirect:" + RedirectHelper.appendQueryParam(target, key, code));
    }

    private List<Subcategory> subcategoryOptions(Long categoryId) {
        if (categoryId == null) {
            return List.of();
        }
        return catalogService.getSubcategoriesByCategory(categoryId);
    }

    @PostMapping("/catalog/requests/{id}/accept")
    @PreAuthorize("hasRole('ADMIN')")
    public ModelAndView acceptBrandModelRequest(
            @CurrentUser User currentUser,
            @PathVariable Long id,
            @Valid @ModelAttribute("acceptRequestForm") AdminBrandModelRequestAcceptForm form,
            BindingResult errors,
            @RequestParam(value = "redirect", required = false) String redirectUrl
    ) {
        LOGGER.info("Admin {} requested acceptance of brand/model request {}", currentUser.getId(), id);
        if (errors.hasErrors()) {
            return catalogError(redirectUrl, "requestInvalid");
        }

        try {
            brandModelRequestService.accept(id, form.toDto());
        } catch (BrandAlreadyExistsException e) {
            LOGGER.warn("Brand/model request {} could not be accepted: {}", id, e.getMessage());
            return catalogError(redirectUrl, "brandNameTaken");
        } catch (ModelAlreadyExistsException e) {
            LOGGER.warn("Brand/model request {} could not be accepted: {}", id, e.getMessage());
            return catalogError(redirectUrl, "modelNameTaken");
        } catch (NotFoundException e) {
            LOGGER.warn("Brand/model request {} not found for acceptance", id);
            return catalogError(redirectUrl, "requestNotFound");
        }

        return catalogSuccess(redirectUrl, "request_accepted");
    }

    @PostMapping("/catalog/requests/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ModelAndView rejectBrandModelRequest(
            @CurrentUser User currentUser,
            @PathVariable Long id,
            @RequestParam(value = "redirect", required = false) String redirectUrl
    ) {
        LOGGER.info("Admin {} requested rejection of brand/model request {}", currentUser.getId(), id);
        try {
            brandModelRequestService.reject(id);
        } catch (NotFoundException e) {
            LOGGER.warn("Brand/model request {} not found for rejection", id);
            return catalogError(redirectUrl, "requestNotFound");
        }
        return catalogSuccess(redirectUrl, "request_rejected");
    }

    @PostMapping("/catalog/brands")
    @PreAuthorize("hasRole('ADMIN')")
    public ModelAndView createBrand(
            @CurrentUser User currentUser,
            @Valid @ModelAttribute("createBrandForm") AdminCreateBrandForm form,
            BindingResult errors,
            @RequestParam(value = "redirect", required = false) String redirectUrl
    ) {
        LOGGER.info("Admin {} requested creation of brand '{}'", currentUser.getId(), form.getName());
        if (errors.hasErrors()) {
            return catalogError(redirectUrl, "brandInvalidName");
        }

        try {
            catalogService.createBrand(form.getName());
        } catch (BrandAlreadyExistsException e) {
            LOGGER.warn("Brand '{}' could not be created: {}", form.getName(), e.getMessage());
            return catalogError(redirectUrl, "brandNameTaken");
        }

        return catalogSuccess(redirectUrl, "brand_created");
    }

    @PostMapping("/catalog/brands/{id}/replace")
    @PreAuthorize("hasRole('ADMIN')")
    public ModelAndView replaceBrand(
            @CurrentUser User currentUser,
            @PathVariable Long id,
            @Valid @ModelAttribute("replaceBrandForm") AdminReplaceBrandForm form,
            BindingResult errors,
            @RequestParam(value = "redirect", required = false) String redirectUrl
    ) {
        LOGGER.info("Admin {} requested replacement of brand {} with {}", currentUser.getId(), id, form.getReplacementBrandId());
        if (errors.hasErrors()) {
            return catalogError(redirectUrl, "brandInvalidReplacement");
        }

        catalogService.replaceBrand(id, form.getReplacementBrandId());

        return catalogSuccess(redirectUrl, "brand_replaced");
    }

    @PostMapping("/catalog/brands/{id}/rename")
    @PreAuthorize("hasRole('ADMIN')")
    public ModelAndView renameBrand(
            @CurrentUser User currentUser,
            @PathVariable Long id,
            @Valid @ModelAttribute("renameBrandForm") AdminRenameBrandForm form,
            BindingResult errors,
            @RequestParam(value = "redirect", required = false) String redirectUrl
    ) {
        LOGGER.info("Admin {} requested rename for brand {}", currentUser.getId(), id);
        if (errors.hasErrors()) {
            return catalogError(redirectUrl, "brandInvalidName");
        }

        try {
            catalogService.renameBrand(id, form.getName());
        } catch (BrandAlreadyExistsException e) {
            LOGGER.warn("Brand {} could not be renamed: {}", id, e.getMessage());
            return catalogError(redirectUrl, "brandNameTaken");
        }

        return catalogSuccess(redirectUrl, "brand_renamed");
    }

    @PostMapping("/catalog/models")
    @PreAuthorize("hasRole('ADMIN')")
    public ModelAndView createModel(
            @CurrentUser User currentUser,
            @Valid @ModelAttribute("createModelForm") AdminModelForm form,
            BindingResult errors,
            @RequestParam(value = "redirect", required = false) String redirectUrl
    ) {
        LOGGER.info("Admin {} requested creation of model '{}'", currentUser.getId(), form.getName());
        if (errors.hasErrors()) {
            return catalogError(redirectUrl, "modelInvalid");
        }
        try {
            catalogService.createModel(form.getName(), form.getBrandId(), form.getSubcategoryId());
        } catch (ModelAlreadyExistsException e) {
            LOGGER.warn("Model '{}' could not be created: {}", form.getName(), e.getMessage());
            return catalogError(redirectUrl, "modelNameTaken");
        }
        return catalogSuccess(redirectUrl, "model_created");
    }

    @PostMapping("/catalog/models/{id}/edit")
    @PreAuthorize("hasRole('ADMIN')")
    public ModelAndView editModel(
            @CurrentUser User currentUser,
            @PathVariable Long id,
            @Valid @ModelAttribute("editModelForm") AdminModelForm form,
            BindingResult errors,
            @RequestParam(value = "redirect", required = false) String redirectUrl
    ) {
        LOGGER.info("Admin {} requested edit of model {}", currentUser.getId(), id);
        if (errors.hasErrors()) {
            return catalogError(redirectUrl, "modelInvalid");
        }
        try {
            catalogService.updateModel(id, form.getName(), form.getBrandId(), form.getSubcategoryId());
        } catch (ModelAlreadyExistsException e) {
            LOGGER.warn("Model {} could not be updated: {}", id, e.getMessage());
            return catalogError(redirectUrl, "modelNameTaken");
        }
        return catalogSuccess(redirectUrl, "model_updated");
    }

    @PostMapping("/catalog/models/{id}/delete")
    @PreAuthorize("hasRole('ADMIN')")
    public ModelAndView deleteModel(
            @CurrentUser User currentUser,
            @PathVariable Long id,
            @RequestParam(value = "redirect", required = false) String redirectUrl
    ) {
        LOGGER.info("Admin {} requested deletion of model {}", currentUser.getId(), id);
        catalogService.deleteModel(id);
        return catalogSuccess(redirectUrl, "model_deleted");
    }

    @PostMapping("/catalog/models/{id}/replace")
    @PreAuthorize("hasRole('ADMIN')")
    public ModelAndView replaceModel(
            @CurrentUser User currentUser,
            @PathVariable Long id,
            @Valid @ModelAttribute("replaceModelForm") AdminReplaceModelForm form,
            BindingResult errors,
            @RequestParam(value = "redirect", required = false) String redirectUrl
    ) {
        LOGGER.info("Admin {} requested replacement of model {} with {}", currentUser.getId(), id, form.getReplacementModelId());
        if (errors.hasErrors()) {
            return catalogError(redirectUrl, "modelInvalidReplacement");
        }
        catalogService.replaceModel(id, form.getReplacementModelId());
        return catalogSuccess(redirectUrl, "model_replaced");
    }

    @PostMapping("/catalog/brands/{id}/delete")
    @PreAuthorize("hasRole('ADMIN')")
    public ModelAndView deleteBrand(
            @CurrentUser User currentUser,
            @PathVariable Long id,
            @RequestParam(value = "redirect", required = false) String redirectUrl
    ) {
        LOGGER.info("Admin {} requested deletion of brand {}", currentUser.getId(), id);
        catalogService.deleteBrand(id);
        return catalogSuccess(redirectUrl, "brand_deleted");
    }

    @GetMapping("/listings")
    @PreAuthorize("hasRole('ADMIN')")
    public ModelAndView listings(
            @CurrentUser User currentUser,
            @Valid @ModelAttribute("filterForm") AdminListingFilterForm filterForm,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @ModelAttribute("takeDownListingForm") AdminTakeDownListingForm takeDownListingForm
    ) {
        LOGGER.debug("Admin {} accessed listings page: page={}, query={}, status={}", currentUser.getId(), page, filterForm.getQuery(), filterForm.getStatus());
        ModelAndView mav = new ModelAndView("admin/listings");
        var filter = filterForm.toDto(page, DEFAULT_PAGE_SIZE);
        mav.addObject("listingsPage", listingService.searchForAdmin(filter));
        mav.addObject("statusOptions", Arrays.stream(ListingStatus.values())
                .map(status -> new StringSelectOption(status.name(), "listing.status." + status.name()))
                .toList());
        mav.addObject("listingsReturnUrl", buildListingsReturnUrl(page, filterForm));
        return mav;
    }

    private String buildListingsReturnUrl(final int page, final AdminListingFilterForm filterForm) {
        final var builder = UriComponentsBuilder.fromPath("/admin/listings")
                .queryParam("page", page);
        if (filterForm.getQuery() != null && !filterForm.getQuery().isBlank()) {
            builder.queryParam("query", filterForm.getQuery());
        }
        if (filterForm.getStatus() != null) {
            builder.queryParam("status", filterForm.getStatus().name());
        }
        return builder.build().encode().toUriString();
    }

    @PostMapping("/listings/takedown")
    @PreAuthorize("hasRole('ADMIN')")
    public ModelAndView takeDownListingByForm(
            @CurrentUser User currentUser,
            @Valid @ModelAttribute("takeDownListingForm") AdminTakeDownListingForm form,
            BindingResult errors,
            @RequestParam(value = "redirect", required = false) String redirectUrl
    ) {
        LOGGER.info("Admin {} requested takedown for listingId={}", currentUser.getId(), form.getListingId());
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
        } catch (ListingNotCancelableException e) {
            LOGGER.warn("Listing {} cannot be taken down: {}", form.getListingId(), e.getMessage());
            errors.rejectValue("listingId", "admin.error.listingCannotBeCanceled");
            return new ModelAndView("admin/dashboard")
                    .addObject("takeDownOfferForm", new AdminTakeDownOfferForm())
                    .addObject("suspendUserForm", new AdminSuspendUserForm())
                    .addObject("grantRoleForm", new AdminGrantRoleForm());
        }

        final var target = RedirectHelper.resolvePath(redirectUrl, "/admin/listings", ADMIN_PATH_FILTER);
        return new ModelAndView("redirect:"
                + RedirectHelper.appendQueryParam(target, "success", "listing_takedown"));
    }

    @PostMapping("/listings/{id}/takedown")
    @PreAuthorize("hasRole('ADMIN')")
    public ModelAndView takeDownListingById(
            @CurrentUser User currentUser,
            @PathVariable Long id,
            @RequestParam(value = "redirect", required = false) String redirectUrl
    ) {
        String key = "error";
        String code = "listingNotFound";
        try {
            adminService.takeDownListing(id);
            key = "success";
            code = "listing_takedown";
        } catch (NotFoundException e) {
            LOGGER.warn("Listing {} not found for takedown", id);
        } catch (ListingNotCancelableException e) {
            LOGGER.warn("Listing {} cannot be taken down: {}", id, e.getMessage());
            code = "listingCannotBeCanceled";
        }
        final var target = RedirectHelper.resolvePath(redirectUrl, "/admin/listings", ADMIN_PATH_FILTER);
        return new ModelAndView("redirect:" + RedirectHelper.appendQueryParam(target, key, code));
    }

    @GetMapping("/offers")
    @PreAuthorize("hasRole('ADMIN')")
    public ModelAndView offers(
            @CurrentUser User currentUser,
            @ModelAttribute("filterForm") AdminOfferFilterForm filterForm,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @ModelAttribute("takeDownOfferForm") AdminTakeDownOfferForm takeDownOfferForm
    ) {
        LOGGER.debug("Admin {} accessed offers page: page={}, statusGroup={}", currentUser.getId(), page, filterForm.getStatusGroup());
        ModelAndView mav = new ModelAndView("admin/offers");
        var filter = filterForm.toDto(page, DEFAULT_PAGE_SIZE);
        mav.addObject("offersPage", offerService.get(filter, false));
        mav.addObject("statusGroups", Arrays.stream(OfferStatusGroup.values()).toList());
        mav.addObject("offersReturnUrl", buildOffersReturnUrl(page, filterForm));
        return mav;
    }

    private String buildOffersReturnUrl(final int page, final AdminOfferFilterForm filterForm) {
        final var builder = UriComponentsBuilder.fromPath("/admin/offers")
                .queryParam("page", page);
        if (filterForm.getStatusGroup() != null) {
            builder.queryParam("statusGroup", filterForm.getStatusGroup().name());
        }
        return builder.build().encode().toUriString();
    }

    @PostMapping("/offers/takedown")
    @PreAuthorize("hasRole('ADMIN')")
    public ModelAndView takeDownOfferByForm(
            @CurrentUser User currentUser,
            @Valid @ModelAttribute("takeDownOfferForm") AdminTakeDownOfferForm form,
            BindingResult errors,
            @RequestParam(value = "redirect", required = false) String redirectUrl
    ) {
        LOGGER.info("Admin {} requested takedown for offerId={}", currentUser.getId(), form.getOfferId());
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
        } catch (OfferNotCancelableException e) {
            LOGGER.warn("Offer {} cannot be taken down: {}", form.getOfferId(), e.getMessage());
            errors.rejectValue("offerId", "admin.error.offerCannotBeCanceled");
            return new ModelAndView("admin/dashboard")
                    .addObject("takeDownListingForm", new AdminTakeDownListingForm())
                    .addObject("suspendUserForm", new AdminSuspendUserForm())
                    .addObject("grantRoleForm", new AdminGrantRoleForm());
        }

        final var target = RedirectHelper.resolvePath(redirectUrl, "/admin/offers", ADMIN_PATH_FILTER);
        return new ModelAndView("redirect:"
                + RedirectHelper.appendQueryParam(target, "success", "offer_takedown"));
    }

    @PostMapping("/offers/{id}/takedown")
    @PreAuthorize("hasRole('ADMIN')")
    public ModelAndView takeDownOfferById(
            @CurrentUser User currentUser,
            @PathVariable Long id,
            @RequestParam(value = "redirect", required = false) String redirectUrl
    ) {
        String key = "error";
        String code = "offerNotFound";
        try {
            adminService.takeDownOffer(id);
            key = "success";
            code = "offer_takedown";
        } catch (NotFoundException e) {
            LOGGER.warn("Offer {} not found for takedown", id);
        } catch (OfferNotCancelableException e) {
            LOGGER.warn("Offer {} cannot be taken down: {}", id, e.getMessage());
            code = "offerCannotBeCanceled";
        }
        final var target = RedirectHelper.resolvePath(redirectUrl, "/admin/offers", ADMIN_PATH_FILTER);
        return new ModelAndView("redirect:" + RedirectHelper.appendQueryParam(target, key, code));
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
        LOGGER.debug("Admin {} accessed users management page", currentUser.getId());
        ModelAndView mav = new ModelAndView("admin/users");
        if (username != null && !username.isBlank()) {
            Optional<User> targetUser = userService.getByUsernameOrEmail(username);
            if (targetUser.isPresent()) {
                User user = targetUser.get();
                final List<Role> targetUserRoles = userService.getRoles(user);
                mav.addObject("targetUser", user);
                mav.addObject("targetUserRoles", targetUserRoles);
                mav.addObject("isTargetUserAdmin", targetUserRoles.contains(Role.ADMIN));
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
            @ModelAttribute("grantRoleForm") AdminGrantRoleForm grantRoleForm,
            @ModelAttribute("filterForm") AdminUserFilterForm filterForm
    ) {
        final var formUsernameOrEmail = form.getUsernameOrEmail();
        LOGGER.info("Admin {} requested user suspension", currentUser.getId());

        if (errors.hasErrors()) {
            return new ModelAndView("admin/users").addObject("filterForm", filterForm);
        }

        final User user;
        try {
            user = adminService.suspendUser(currentUser, formUsernameOrEmail);
        } catch (SelfSuspendException e) {
            LOGGER.warn("Admin {} prevented from suspending theirself", currentUser.getId());
            errors.rejectValue("username", "admin.users.error.selfSuspend");
            return new ModelAndView("admin/users").addObject("filterForm", filterForm);
        }

        LOGGER.info("Admin {} logically suspended user id={}", currentUser.getId(), user.getId());
        return new ModelAndView("redirect:/admin/users?success=user_suspended&username=" + URLEncoder.encode(user.getUsername(), StandardCharsets.UTF_8));
    }

    @PostMapping("/users/unsuspend")
    @PreAuthorize("hasRole('ADMIN')")
    public ModelAndView unsuspendUser(
            @CurrentUser User currentUser,
            @Valid @ModelAttribute("suspendUserForm") AdminSuspendUserForm form,
            BindingResult errors,
            @ModelAttribute("grantRoleForm") AdminGrantRoleForm grantRoleForm,
            @ModelAttribute("filterForm") AdminUserFilterForm filterForm
    ) {
        final var formUsernameOrEmail = form.getUsernameOrEmail();
        LOGGER.info("Admin {} requested user unsuspension", currentUser.getId());

        if (errors.hasErrors()) {
            return new ModelAndView("admin/users").addObject("filterForm", filterForm);
        }

        User user = adminService.unsuspendUser(formUsernameOrEmail);

        LOGGER.info("Admin {} unsuspended user id={}", currentUser.getId(), user.getId());
        return new ModelAndView("redirect:/admin/users?success=user_unsuspended&username=" + URLEncoder.encode(user.getUsername(), StandardCharsets.UTF_8));
    }

    @PostMapping("/users/grant-admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ModelAndView grantAdminRole(
            @CurrentUser User currentUser,
            @Valid @ModelAttribute("grantRoleForm") AdminGrantRoleForm form,
            BindingResult errors,
            @ModelAttribute("suspendUserForm") AdminSuspendUserForm suspendUserForm,
            @ModelAttribute("filterForm") AdminUserFilterForm filterForm
    ) {
        LOGGER.info("Admin {} requested granting ADMIN role", currentUser.getId());
        if (errors.hasErrors()) {
            return new ModelAndView("admin/users").addObject("filterForm", filterForm);
        }

        User user = adminService.grantRole(form.getUsernameOrEmail(), Role.ADMIN);

        LOGGER.info("Admin {} granted ADMIN role to user id={}", currentUser.getId(), user.getId());
        return new ModelAndView("redirect:/admin/users?success=role_granted&username=" + URLEncoder.encode(user.getUsername(), StandardCharsets.UTF_8));
    }
}
