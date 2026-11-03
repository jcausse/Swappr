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
import ar.edu.itba.paw.model.User;
import ar.edu.itba.paw.service.ListingService;
import ar.edu.itba.paw.service.OfferService;
import ar.edu.itba.paw.service.ProductService;
import ar.edu.itba.paw.service.ProvinceService;
import ar.edu.itba.paw.service.dto.ListingCreationDto;
import ar.edu.itba.paw.service.dto.ListingFilterDto;
import ar.edu.itba.paw.service.dto.ListingUpdateDto;
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

    private static final String OTHER_VALUE = "__OTHER__";

    private final ListingService listingService;
    private final OfferService offerService;
    private final ProductService productService;
    private final ProvinceService provinceService;

    @GetMapping
    @PreAuthorize("permitAll()")
    public ModelAndView discovery(@ModelAttribute("filterForm") ListingFilterForm filterForm) {
        LOGGER.debug("Accessing discovery page with query '{}', category {}", filterForm.getQuery(), filterForm.getCategoryId());
        final var filter = ListingFilterDto.builder()
            .categoryId(filterForm.getCategoryId())
            .subcategoryId(filterForm.getSubcategoryId())
            .minPrice(filterForm.getMinPrice())
            .maxPrice(filterForm.getMaxPrice())
            .condition(filterForm.getCondition())
            .acceptsTrade(filterForm.getAcceptsTrade())
            .query(filterForm.getQuery())
            .sort(filterForm.getSort())
            .status(ListingStatus.ACTIVE)
            .page(filterForm.getPage())
            .pageSize(DISCOVERY_PAGE_SIZE)
            .provinceId(filterForm.getProvinceId())
            .acceptsShipping(filterForm.getAcceptsShipping())
            .brand(filterForm.getBrand())
            .model(filterForm.getModel())
            .yearMin(filterForm.getYearMin())
            .yearMax(filterForm.getYearMax())
            .build();

        final var listingPage = listingService.search(filter);

        final var mav = new ModelAndView("listing/discovery");
        mav.addObject("listingPage", listingPage);
        mav.addObject("listings", listingPage.getContent());

        mav.addObject("conditionOptions", buildConditionOptions());
        mav.addObject("sortOptions", Arrays.stream(ListingSort.values())
            .map(s -> new StringSelectOption(s.name(), "discovery.sort." + s.getKey()))
            .toList());

        var categories = productService.getAllCategories();
        mav.addObject("categoryOptions", categories.stream()
            .map(cat -> new SelectOption(cat.getId(), "category." + cat.getName()))
            .toList());

        mav.addObject("provinceOptions", provinceService.getAll().stream()
            .map(province -> new SelectOption(province.getId(), "province." + province.getName()))
            .toList());

        if (filterForm.getCategoryId() != null) {
            mav.addObject("subcategoryOptions", productService.getSubcategoriesByCategory(filterForm.getCategoryId()).stream()
                .map(sub -> new SelectOption(sub.getId(), "subcategory." + sub.getName()))
                .toList());

            if (filterForm.getSubcategoryId() != null) {
                mav.addObject("brandOptions", productService.getBrandsBySubcategory(filterForm.getSubcategoryId()));

                if (filterForm.getBrand() != null && !filterForm.getBrand().isBlank()) {
                    mav.addObject("modelOptions", productService.getModelsBySubcategoryAndBrand(filterForm.getSubcategoryId(), filterForm.getBrand()));
                }
            }
        }
        return mav;
    }

    @GetMapping("/by-product")
    public ModelAndView product(@ModelAttribute("productForm") ProductSearchForm productForm) {
        LOGGER.debug("Accessing products by brand and model page");
        final var mav = new ModelAndView("listing/byProduct");

        final var brand = productForm.getBrand();
        final var model = productForm.getModel();

        if (productForm.getSubcategoryId() != null) {
            mav.addObject("productSummaries", productService.getListingInfoSummary(
                brand,
                model,
                productForm.getYearMin(),
                productForm.getYearMax(),
                productForm.getSubcategoryId()
            ));
        } else {
            mav.addObject("productSummaries", List.of());
        }

        mav.addObject("categories", productService.getAllCategories());
        if (productForm.getCategoryId() != null) {
            mav.addObject("subcategories", productService.getSubcategoriesByCategory(productForm.getCategoryId()));
        }

        if (productForm.getSubcategoryId() != null) {
            mav.addObject("brands", productService.getBrandsBySubcategory(productForm.getSubcategoryId()));

            if (brand != null) {
                mav.addObject("models", productService.getModelsBySubcategoryAndBrand(productForm.getSubcategoryId(), brand));
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

    @GetMapping("/new/choose-product")
    @PreAuthorize("isAuthenticated()")
    public ModelAndView chooseProduct(@ModelAttribute("chooseProductForm") ChooseProductForm form,
                                      @RequestParam(value = "productId", required = false) Long productId) {
        LOGGER.debug("Accessing choose product page");
        var mav = new ModelAndView("listing/new/chooseProduct");
        // Coming back from step 2: rehydrate the form from the already chosen product
        // so the user sees and can change their selection instead of starting over.
        if (productId != null) {
            var product = productService.getById(productId);
            var subcategory = product.getSubcategory();
            form.setCategoryId(subcategory.getCategory().getId());
            form.setSubcategoryId(subcategory.getId());
            form.setNewProductBrand(product.getBrand());
            form.setNewProductModel(product.getModel());
            form.setNewProductYear(product.getYear());
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
            // Category was changed - reset subcategory and product fields
            form.setSubcategoryId(null);
            form.setNewProductBrand(null);
            form.setNewProductModel(null);
            form.setNewProductYear(null);
            form.setStep(2);
        } else if (step != null && step > 2 && form.isSubcategoryChanged()) {
            // Subcategory was changed - reset product fields
            form.setNewProductBrand(null);
            form.setNewProductModel(null);
            form.setNewProductYear(null);
            form.setStep(3);
        }

        // Reset model if brand is empty (empty string)
        if (form.getNewProductBrand() == null || form.getNewProductBrand().isBlank()) {
            form.setNewProductModel(null);
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
                form.setNewProductBrand(null);
                form.setNewProductModel(null);
                form.setNewProductYear(null);
            }
        } else if (form.getStep() != null && form.getStep() == 3 && !isAutoSubmit) {
            var brand = form.getNewProductBrand();
            var model = form.getNewProductModel();

            if (OTHER_VALUE.equals(brand)) {
                form.setNewProductModel(OTHER_VALUE);
                model = OTHER_VALUE;
            }

            var otherBrand = form.getOtherBrand();
            var otherModel = form.getOtherModel();

            brand = OTHER_VALUE.equals(brand) ? otherBrand : brand;
            model = OTHER_VALUE.equals(model) ? otherModel : model;

            var product = productService.findOrCreateByBrandModelYear(
                    brand,
                    model,
                    form.getNewProductYear(),
                    form.getSubcategoryId()
            );
            return new ModelAndView("redirect:/listing/new/details?productId=" + product.getId());
        }

        // Update previous values for next request
        form.updatePreviousValues();

        populateModel(mav, form);
        return mav;
    }

    private void populateModel(ModelAndView mav, ChooseProductForm form) {
        var categories = productService.getAllCategories();
        mav.addObject("categories", categories);

        mav.addObject("categoryOptions", categories.stream()
            .map(cat -> new SelectOption(cat.getId(), "category." + cat.getName()))
            .toList());

        if (form.getCategoryId() != null) {
            var subcategories = productService.getSubcategoriesByCategory(form.getCategoryId());
            mav.addObject("subcategories", subcategories);

            mav.addObject("subcategoryOptions", subcategories.stream()
                .map(sub -> new SelectOption(sub.getId(), "subcategory." + sub.getName()))
                .toList());
        }

        if (form.getSubcategoryId() != null) {
            mav.addObject("brands", productService.getBrandsBySubcategory(form.getSubcategoryId()));
            List<String> models = List.of();
            // Only fetch models if brand is selected and not "Other"
            if (form.getNewProductBrand() != null && !form.getNewProductBrand().isBlank() && !OTHER_VALUE.equals(form.getNewProductBrand())) {
                models = productService.getModelsBySubcategoryAndBrand(form.getSubcategoryId(), form.getNewProductBrand());
            }
            // A custom brand has no known models, so the model is necessarily custom too
            if (OTHER_VALUE.equals(form.getNewProductBrand())) {
                form.setNewProductModel(OTHER_VALUE);
            }
            mav.addObject("models", models);
            mav.addObject("modelsEmpty", models.isEmpty());
        }
    }

    @GetMapping("/new/details")
    @PreAuthorize("isAuthenticated()")
    public ModelAndView details(
        @RequestParam("productId") Long productId,
        @ModelAttribute("detailsForm") ListingDetailsForm form,
        @CurrentUser User currentUser
    ) {
        LOGGER.debug("Accessing new listing details page for product {}", productId);
        var product = productService.getById(productId);
        form.setProductId(product.getId());

        var mav = new ModelAndView("listing/new/details");
        mav.addObject("product", product);
        mav.addObject("conditionOptions", buildConditionOptions());
        mav.addObject("actionUrl", "/listing/new/details");
        mav.addObject("edit", false);
        return mav;
    }

    @PostMapping("/new/details")
    @PreAuthorize("isAuthenticated()")
    public ModelAndView detailsPost(
        @Valid @ModelAttribute("detailsForm") ListingDetailsForm form,
        BindingResult bindingResult,
        @CurrentUser User currentUser
    ) {
        LOGGER.info("User {} submitted listing creation for product {}", currentUser.getId(), form.getProductId());
        if (bindingResult.hasErrors()) {
            LOGGER.debug("Validation failed for listing creation for product {}", form.getProductId());
            return detailsWithErrors(form);
        }

        listingService.create(new ListingCreationDto(
                form.getTitle(),
                form.getPrice(),
                currentUser.getId(),
                form.getProductId(),
                form.getCondition(),
                form.isAcceptsTrade(),
                form.isAcceptsShipping(),
                form.getDescription(),
                form.getImages()
        ));
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
        form.setProductId(listing.getProduct().getId());
        form.setTitle(listing.getTitle());
        form.setPrice(listing.getPrice().getAmount());
        form.setCondition(listing.getCondition());
        form.setAcceptsTrade(listing.isAcceptsTrade());
        form.setDescription(listing.getDescription());

        var mav = new ModelAndView("listing/new/details");
        mav.addObject("conditionOptions", buildConditionOptions());
        mav.addObject("product", listing.getProduct());
        mav.addObject("actionUrl", "/listing/" + id + "/edit");
        mav.addObject("edit", true);
        return mav;
    }

    @PostMapping("/{id}/edit")
    @PreAuthorize("@webPermissionService.canManageListing(authentication, #id)")
    public ModelAndView editListing(
        @PathVariable Long id,
        @Valid @ModelAttribute("detailsForm") ListingDetailsForm form,
        BindingResult bindingResult,
        @CurrentUser User currentUser
    ) {
        LOGGER.info("User {} submitted listing edit for product {}", currentUser.getId(), form.getProductId());
        if (bindingResult.hasErrors()) {
            LOGGER.debug("Validation failed for listing edit for product {}", form.getProductId());
            return detailsWithErrors(form);
        }

        listingService.update(new ListingUpdateDto(
            id,
            form.getTitle(),
            form.getPrice(),
            form.getProductId(),
            form.getCondition(),
            form.isAcceptsTrade(),
            form.isAcceptsShipping(),
            form.getDescription()
        ), currentUser.getId());
        return new ModelAndView("redirect:/account/listings");
    }

    private ModelAndView detailsWithErrors(ListingDetailsForm form) {
        var product = productService.getById(form.getProductId());
        return new ModelAndView("listing/new/details")
                .addObject("product", product)
                .addObject("conditionOptions", buildConditionOptions());
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("@webPermissionService.canManageListing(authentication, #id)")
    public ModelAndView cancelListing(@PathVariable Long id, @CurrentUser User currentUser) {
        LOGGER.info("User {} canceled listing {}", currentUser.getId(), id);
        listingService.cancel(id, currentUser.getId());
        return new ModelAndView("redirect:/account/listings");
    }

    private List<StringSelectOption> buildConditionOptions() {
        return Arrays.stream(Condition.values())
            .map(c -> new StringSelectOption(c.name(), "condition." + c.name()))
            .toList();
    }
}
