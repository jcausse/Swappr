package ar.edu.itba.paw.webapp.controller;

import java.util.Arrays;
import java.util.List;

import javax.validation.Valid;

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

import ar.edu.itba.paw.model.Condition;
import ar.edu.itba.paw.model.ListingSort;
import ar.edu.itba.paw.model.ListingStatus;
import ar.edu.itba.paw.model.Model;
import ar.edu.itba.paw.model.Page;
import ar.edu.itba.paw.model.User;
import ar.edu.itba.paw.service.CatalogService;
import ar.edu.itba.paw.service.ListingCancellationService;
import ar.edu.itba.paw.service.ListingService;
import ar.edu.itba.paw.service.OfferService;
import ar.edu.itba.paw.service.ProvinceService;
import ar.edu.itba.paw.webapp.auth.CurrentUser;
import ar.edu.itba.paw.webapp.form.ChooseProductForm;
import ar.edu.itba.paw.webapp.form.ListingDetailsForm;
import ar.edu.itba.paw.webapp.form.ListingFilterForm;
import ar.edu.itba.paw.webapp.form.ProductSearchForm;
import ar.edu.itba.paw.webapp.form.SelectOption;
import ar.edu.itba.paw.webapp.form.StringSelectOption;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Controller
@RequestMapping("/listing")
public class ListingController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ListingController.class);

    private static final int DISCOVERY_PAGE_SIZE = 12;
    private static final int PRODUCTS_PAGE_SIZE = 10;


    private final ListingService listingService;
    private final ListingCancellationService listingCancellationService;
    private final OfferService offerService;
    private final CatalogService catalogService;
    private final ProvinceService provinceService;

    @GetMapping
    @PreAuthorize("permitAll()")
    public ModelAndView discovery(@ModelAttribute("filterForm") ListingFilterForm filterForm) {
        LOGGER.debug("Accessing discovery page with query '{}', category {}", filterForm.getQuery(), filterForm.getCategoryId());
        final var filter = filterForm.toDto(null, ListingStatus.ACTIVE, DISCOVERY_PAGE_SIZE);

        final var listingPage = listingService.search(filter);

        final var mav = new ModelAndView("listing/discovery");
        mav.addObject("listingPage", listingPage);
        mav.addObject("listings", listingPage.getContent());

        mav.addObject("conditionOptions", buildConditionOptions());
        mav.addObject("sortOptions", Arrays.stream(ListingSort.values())
            .map(s -> new StringSelectOption(s.name(), "listing.sort." + s.getKey()))
            .toList());

        var categories = catalogService.getAllCategories();
        mav.addObject("categories", categories);
        mav.addObject("categoryOptions", categories.stream()
            .map(cat -> new SelectOption(cat.getId(), "category." + cat.getName()))
            .toList());

        mav.addObject("provinceOptions", provinceService.getAll().stream()
            .map(province -> new SelectOption(province.getId(), "province." + province.getName()))
            .toList());

        if (filterForm.getCategoryId() != null) {
            mav.addObject("subcategoryOptions", catalogService.getSubcategoriesByCategory(filterForm.getCategoryId()).stream()
                .map(sub -> new SelectOption(sub.getId(), "subcategory." + sub.getName()))
                .toList());

            if (filterForm.getSubcategoryId() != null) {
                mav.addObject("brandOptions", catalogService.getBrandsBySubcategory(filterForm.getSubcategoryId()));

                if (filterForm.getBrandId() != null) {
                    mav.addObject("modelOptions", catalogService.getModelsBySubcategoryAndBrand(
                        filterForm.getSubcategoryId(), filterForm.getBrandId()));
                }
            }
        }
        return mav;
    }

    @GetMapping("/by-product")
    public ModelAndView product(@ModelAttribute("productForm") ProductSearchForm productForm) {
        LOGGER.debug("Accessing products by brand and model page");
        final var mav = new ModelAndView("listing/byProduct");

        if (productForm.getSubcategoryId() != null) {
            final var productPage = catalogService.getListingInfoSummary(
                productForm.getBrandId(),
                productForm.getModelId(),
                productForm.getYearMin(),
                productForm.getYearMax(),
                productForm.getSubcategoryId(),
                productForm.getPage(),
                PRODUCTS_PAGE_SIZE
            );
            mav.addObject("productPage", productPage);
            mav.addObject("productSummaries", productPage.getContent());
        } else {
            mav.addObject("productPage", new Page<>(List.of(), 1, PRODUCTS_PAGE_SIZE, 0));
            mav.addObject("productSummaries", List.of());
        }

        if (productForm.getCategoryId() != null) {
            mav.addObject("subcategories", catalogService.getSubcategoriesByCategory(productForm.getCategoryId()));
        }

        if (productForm.getSubcategoryId() != null) {
            mav.addObject("brands", catalogService.getBrandsBySubcategory(productForm.getSubcategoryId()));

            if (productForm.getBrandId() != null) {
                mav.addObject("models", catalogService.getModelsBySubcategoryAndBrand(
                    productForm.getSubcategoryId(), productForm.getBrandId()));
            }
        }

        return mav;
    }

    @GetMapping("/{id}")
    @PreAuthorize("permitAll()")
    public ModelAndView listing(@PathVariable Long id, @CurrentUser(required = false) User currentUser) {
        LOGGER.debug("Accessing listing {}", id);
        var listing = listingService.getById(id);
        var userPendingOffer = offerService.getByListingAndBuyer(listing, currentUser);

        return new ModelAndView("listing/index")
                .addObject("listing", listing)
                .addObject("userPendingOffer", userPendingOffer);
    }

    @PostMapping("/{id}/interest")
    @PreAuthorize("isAuthenticated()")
    public ModelAndView interest(@PathVariable Long id, @CurrentUser User currentUser) {
        offerService.createInterest(id, currentUser.getId());
        return new ModelAndView("redirect:/listing/" + id);
    }

    @GetMapping("/new/choose-product")
    @PreAuthorize("isAuthenticated()")
    public ModelAndView chooseProduct(@ModelAttribute("chooseProductForm") ChooseProductForm form,
                                      @RequestParam(value = "modelId", required = false) Long modelId,
                                      @RequestParam(value = "year", required = false) Integer year) {
        LOGGER.debug("Accessing choose product page");
        var mav = new ModelAndView("listing/new/chooseProduct");
        // Coming back from step 2: rehydrate the form from the already chosen model
        // so the user sees and can change their selection instead of starting over.
        if (modelId != null) {
            var model = catalogService.getModelById(modelId);
            var subcategory = model.getSubcategory();
            form.setCategoryId(subcategory.getCategory().getId());
            form.setSubcategoryId(subcategory.getId());
            form.setBrandId(model.getBrand().getId());
            form.setModelId(model.getId());
            form.setYear(year);
            form.setStep(3);
            form.updatePreviousValues();
        } else {
            form.setStep(1);
        }
        populateModel(mav, form);
        return mav;
    }

    @PostMapping("/new/choose-product")
    @PreAuthorize("isAuthenticated()")
    public ModelAndView chooseProductPost(@Valid @ModelAttribute("chooseProductForm") ChooseProductForm form, BindingResult bindingResult) {
        LOGGER.debug("Submitted choose product form, step {}", form.getStep());
        var mav = new ModelAndView("listing/new/chooseProduct");

        // Skip validation for auto-submits (triggered by field changes during form filling)
        var isAutoSubmit = Boolean.TRUE.equals(form.getIsAutoSubmit());
        if (isAutoSubmit) {
            form.setIsAutoSubmit(false); // Reset for next request
        }

        // Reset downstream fields when user goes back and changes a previous field
        var step = form.getStep();
        if (step != null && step > 1 && form.isCategoryChanged()) {
            // Category was changed - reset subcategory and catalog fields
            form.setSubcategoryId(null);
            form.setBrandId(null);
            form.setModelId(null);
            form.setYear(null);
            form.setStep(2);
        } else if (step != null && step > 2 && form.isSubcategoryChanged()) {
            // Subcategory was changed - reset catalog fields
            form.setBrandId(null);
            form.setModelId(null);
            form.setYear(null);
            form.setStep(3);
        }

        // Brand is only used to scope the model options.
        if (form.getBrandId() == null) {
            form.setModelId(null);
        }

        if (bindingResult.hasErrors()) {
            LOGGER.debug("Validation failed for choose product form");
            form.updatePreviousValues();
            populateModel(mav, form);
            return mav;
        }

        if (form.getStep() != null && form.getStep() == 1) {
            if (form.getCategoryId() != null) {
                form.setStep(2);
                form.setSubcategoryId(null);
            }
        } else if (form.getStep() != null && form.getStep() == 2) {
            if (form.getSubcategoryId() != null) {
                form.setStep(3);
                form.setBrandId(null);
                form.setModelId(null);
                form.setYear(null);
            }
        } else if (form.getStep() != null && form.getStep() == 3 && !isAutoSubmit) {
            return new ModelAndView(
                "redirect:/listing/new/details?modelId=" + form.getModelId() + "&year=" + form.getYear()
            );
        }

        // Update previous values for next request
        form.updatePreviousValues();

        populateModel(mav, form);
        return mav;
    }

    private void populateModel(ModelAndView mav, ChooseProductForm form) {
        var categories = catalogService.getAllCategories();
        mav.addObject("categories", categories);
        mav.addObject("categoryOptions", categories.stream()
            .map(cat -> new SelectOption(cat.getId(), "category." + cat.getName()))
            .toList());

        if (form.getCategoryId() != null) {
            var subcategories = catalogService.getSubcategoriesByCategory(form.getCategoryId());
            mav.addObject("subcategoryOptions", subcategories.stream()
                .map(sub -> new SelectOption(sub.getId(), "subcategory." + sub.getName()))
                .toList());
        }

        if (form.getSubcategoryId() != null) {
            mav.addObject("brands", catalogService.getBrandsBySubcategory(form.getSubcategoryId()));
            List<Model> models = List.of();
            if (form.getBrandId() != null) {
                models = catalogService.getModelsBySubcategoryAndBrand(form.getSubcategoryId(), form.getBrandId());
            }
            mav.addObject("models", models);
            mav.addObject("modelsEmpty", models.isEmpty());
        }
    }

    @GetMapping("/new/details")
    @PreAuthorize("isAuthenticated()")
    public ModelAndView details(
        @RequestParam("modelId") Long modelId,
        @RequestParam("year") Integer year,
        @ModelAttribute("detailsForm") ListingDetailsForm form,
        @CurrentUser User currentUser
    ) {
        LOGGER.debug("Accessing new listing details page for model {}", modelId);
        var model = catalogService.getModelById(modelId);
        form.setModelId(model.getId());
        form.setYear(year);

        return detailsView(model, null);
    }

    @PostMapping("/new/details")
    @PreAuthorize("isAuthenticated()")
    public ModelAndView detailsPost(
        @Valid @ModelAttribute("detailsForm") ListingDetailsForm form,
        BindingResult bindingResult,
        @CurrentUser User currentUser
    ) {
        LOGGER.info("User {} submitted listing creation for model {}", currentUser.getId(), form.getModelId());
        if (bindingResult.hasErrors()) {
            LOGGER.debug("Validation failed for listing creation for model {}", form.getModelId());
            return detailsView(catalogService.getModelById(form.getModelId()), null);
        }

        listingService.create(form.toCreationDto(currentUser.getId()));
        return new ModelAndView("redirect:/account/listings");
    }

    @GetMapping("/{id}/edit")
    @PreAuthorize("@webPermissionService.canManageListing(authentication, #id)")
    public ModelAndView editListing(
        @PathVariable Long id,
        @ModelAttribute("detailsForm") ListingDetailsForm form,
        @CurrentUser User currentUser
    ) {
        LOGGER.debug("Accessing edit listing details page for {}", id);

        var listing = listingService.getById(id);
        form.setModelId(listing.getModel().getId());
        form.setYear(listing.getYear());
        form.setTitle(listing.getTitle());
        form.setPrice(listing.getPrice().getAmount());
        form.setCondition(listing.getCondition());
        form.setAcceptsTrade(listing.isAcceptsTrade());
        form.setAcceptsShipping(listing.isAcceptsShipping());
        form.setDescription(listing.getDescription());

        return detailsView(listing.getModel(), id)
            .addObject("existingImageIds", listing.getImageIds());
    }

    @PostMapping("/{id}/edit")
    @PreAuthorize("@webPermissionService.canManageListing(authentication, #id)")
    public ModelAndView editListing(
        @PathVariable Long id,
        @Valid @ModelAttribute("detailsForm") ListingDetailsForm form,
        BindingResult bindingResult,
        @CurrentUser User currentUser
    ) {
        LOGGER.info("User {} submitted listing edit for model {}", currentUser.getId(), form.getModelId());
        if (bindingResult.hasErrors()) {
            LOGGER.debug("Validation failed for listing edit for model {}", form.getModelId());
            return editDetailsWithErrors(id, form);
        }

        listingService.update(form.toUpdateDto(id), currentUser.getId());
        return new ModelAndView("redirect:/account/listings");
    }

    // Shared by the create and edit pages (GET and POST with errors). editedListingId is null when creating.
    private ModelAndView detailsView(final Model model, final Long editedListingId) {
        final boolean edit = editedListingId != null;
        return new ModelAndView("listing/new/details")
            .addObject("model", model)
            .addObject("conditionOptions", buildConditionOptions())
            .addObject("priceSummary",
                catalogService.getListingInfoSummaryForModel(model.getId(), editedListingId).orElse(null))
            .addObject("actionUrl", edit ? "/listing/" + editedListingId + "/edit" : "/listing/new/details")
            .addObject("edit", edit);
    }

    private ModelAndView editDetailsWithErrors(final Long listingId, final ListingDetailsForm form) {
        return detailsView(catalogService.getModelById(form.getModelId()), listingId)
            .addObject("existingImageIds", listingService.getById(listingId).getImageIds());
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("@webPermissionService.canManageListing(authentication, #id)")
    public ModelAndView cancelListing(@PathVariable Long id, @CurrentUser User currentUser) {
        LOGGER.info("User {} canceled listing {}", currentUser.getId(), id);
        listingCancellationService.cancel(id, currentUser.getId());
        return new ModelAndView("redirect:/account/listings");
    }

    private List<StringSelectOption> buildConditionOptions() {
        return Arrays.stream(Condition.values())
            .map(c -> new StringSelectOption(c.name(), "condition." + c.name()))
            .toList();
    }
}
