package ar.edu.itba.paw.webapp.controller;

import ar.edu.itba.paw.model.Condition;
import ar.edu.itba.paw.model.ListingSort;
import ar.edu.itba.paw.model.ListingStatus;
import ar.edu.itba.paw.model.Price;
import ar.edu.itba.paw.model.User;
import ar.edu.itba.paw.service.ListingService;
import ar.edu.itba.paw.service.OfferService;
import ar.edu.itba.paw.service.ProductService;
import ar.edu.itba.paw.service.ProvinceService;
import ar.edu.itba.paw.service.dto.ImageData;
import ar.edu.itba.paw.service.dto.ListingCreationDto;
import ar.edu.itba.paw.service.dto.ListingFilterDto;
import ar.edu.itba.paw.service.dto.ListingUpdateDto;
import ar.edu.itba.paw.service.exception.ForbiddenException;
import ar.edu.itba.paw.webapp.auth.CurrentUser;
import ar.edu.itba.paw.webapp.form.ChooseProductForm;
import ar.edu.itba.paw.webapp.form.ListingDetailsForm;
import ar.edu.itba.paw.webapp.form.ListingFilterForm;
import ar.edu.itba.paw.webapp.form.SelectOption;
import ar.edu.itba.paw.webapp.form.StringSelectOption;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.validation.Valid;
import org.springframework.validation.BindingResult;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private final MessageSource messageSource;

    @GetMapping
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
            .status(ListingStatus.ACTIVE.getStatus())
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

        // Create translated condition options for paw:formSelect
        var conditionOptions = new java.util.ArrayList<StringSelectOption>();
        for (var c : Condition.values()) {
            conditionOptions.add(new StringSelectOption(
                c.name(),
                messageSource.getMessage("condition." + c.name(), null, LocaleContextHolder.getLocale())
            ));
        }
        mav.addObject("conditionOptions", conditionOptions);

        // Create translated sort options for paw:formSelect
        var sortOptions = new java.util.ArrayList<StringSelectOption>();
        for (var s : ListingSort.values()) {
            sortOptions.add(new StringSelectOption(
                s.getKey(),
                messageSource.getMessage("discovery.sort." + s.getKey(), null, LocaleContextHolder.getLocale())
            ));
        }
        mav.addObject("sortOptions", sortOptions);

        // Create translated category options for paw:formSelect
        var categories = productService.getAllCategories();
        var categoryOptions = new java.util.ArrayList<SelectOption>();
        for (var cat : categories) {
            categoryOptions.add(new SelectOption(cat.getId(), messageSource.getMessage("category." + cat.getName(), null, LocaleContextHolder.getLocale())));
        }
        mav.addObject("categoryOptions", categoryOptions);

        // Create translated province options for paw:formSelect
        var provinceOptions = new java.util.ArrayList<SelectOption>();
        for (var province : provinceService.getAll()) {
            provinceOptions.add(new SelectOption(province.getId(), messageSource.getMessage("province." + province.getName(), null, LocaleContextHolder.getLocale())));
        }
        mav.addObject("provinceOptions", provinceOptions);

        if (filterForm.getCategoryId() != null) {
            // Create translated subcategory options for paw:formSelect
            var subcategories = productService.getSubcategoriesByCategory(filterForm.getCategoryId());
            var subcategoryOptions = new java.util.ArrayList<SelectOption>();
            for (var sub : subcategories) {
                subcategoryOptions.add(new SelectOption(sub.getId(), messageSource.getMessage("subcategory." + sub.getName(), null, LocaleContextHolder.getLocale())));
            }
            mav.addObject("subcategoryOptions", subcategoryOptions);
        }

        if (filterForm.getSubcategoryId() != null) {
            var brandOptions = new java.util.ArrayList<StringSelectOption>();
            for (var brand : productService.getBrandsBySubcategory(filterForm.getSubcategoryId())) {
                brandOptions.add(new StringSelectOption(brand, brand));
            }
            mav.addObject("brandOptions", brandOptions);

            if (filterForm.getBrand() != null && !filterForm.getBrand().isBlank()) {
                var modelOptions = new java.util.ArrayList<StringSelectOption>();
                for (var model : productService.getModelsBySubcategoryAndBrand(filterForm.getSubcategoryId(), filterForm.getBrand())) {
                    modelOptions.add(new StringSelectOption(model, model));
                }
                mav.addObject("modelOptions", modelOptions);
            }
        }
        return mav;
    }

    @GetMapping("/{id}")
    public ModelAndView listing(@PathVariable Long id, @CurrentUser(required = false) User currentUser) {
        LOGGER.debug("Accessing listing {}", id);
        var listing = listingService.getById(id);
        var isCreator = currentUser != null && currentUser.getId().equals(listing.getCreator().getId());
        var isCanceled = listing.getStatus() == ListingStatus.CANCELED;
        // Pending transaction and offered in trade listings count as sold for public viewing purposes
        var isSold = listing.getStatus() == ListingStatus.SOLD
                || listing.getStatus() == ListingStatus.PENDING_TRANSACTION
                || listing.getStatus() == ListingStatus.OFFERED_IN_TRADE;

        var userPendingOffer = offerService.getByListingAndBuyer(listing, currentUser);

        return new ModelAndView("listing/index")
                .addObject("listing", listing)
                .addObject("isCreator", isCreator)
                .addObject("isSold", isSold)
                .addObject("isCanceled", isCanceled)
                .addObject("userPendingOffer", userPendingOffer);
    }

    @GetMapping("/new/choose-product")
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

    @GetMapping("/new/details")
    public ModelAndView details(@RequestParam("productId") Long productId,
                                @RequestParam(value = "editListingId", required = false) Long editListingId,
                                @ModelAttribute("detailsForm") ListingDetailsForm form,
                                @CurrentUser User currentUser) {
        LOGGER.debug("Accessing new listing details page for product {}", productId);
        var product = productService.getById(productId);
        var mav = new ModelAndView("listing/new/details");

        mav.addObject("product", product);
        mav.addObject("conditionOptions", buildConditionOptions());
        form.setProductId(product.getId());
        form.setEditListingId(editListingId);

        if (editListingId != null) {
            var listing = listingService.getById(editListingId);
            if (!listing.getCreator().getId().equals(currentUser.getId())) {
                throw new ForbiddenException("Not authorized to edit this listing");
            }
            form.setTitle(listing.getTitle());
            form.setPrice(listing.getPrice().getAmount());
            form.setCondition(listing.getCondition().name());
            form.setAcceptsTrade(listing.isAcceptsTrade());
            form.setDescription(listing.getDescription());
        }
        return mav;
    }

    @PostMapping("/new/details")
    public ModelAndView detailsPost(
            @Valid @ModelAttribute("detailsForm") ListingDetailsForm form,
            BindingResult bindingResult,
            @CurrentUser User currentUser
    ) {
        LOGGER.info("User {} submitted listing creation/edit for product {}", currentUser.getId(), form.getProductId());
        if (bindingResult.hasErrors()) {
            LOGGER.debug("Validation failed for listing creation/edit for product {}", form.getProductId());
            return detailsWithErrors(form);
        }

        List<ImageData> imageDataList = new ArrayList<>();
        if (form.getImages() != null) {
            for (MultipartFile imageFile : form.getImages()) {
                if (imageFile != null && !imageFile.isEmpty()) {
                    try {
                        imageDataList.add(new ImageData(
                            imageFile.getBytes(),
                            imageFile.getOriginalFilename(),
                            imageFile.getContentType()
                        ));
                    } catch (IOException e) {
                        LOGGER.error("Failed to read uploaded image", e);
                        bindingResult.rejectValue("images", "error.image.upload");
                        return detailsWithErrors(form);
                    }
                }
            }
        }

        if (form.getEditListingId() != null) {
            var updated = listingService.update(new ListingUpdateDto(
                form.getEditListingId(),
                form.getTitle(),
                new Price(form.getPrice()),
                form.getProductId(),
                form.getCondition(),
                form.isAcceptsTrade(),
                form.isAcceptsShipping(),
                form.getDescription()
            ), currentUser.getId());
            return new ModelAndView("redirect:/listing/" + updated.getId());
        }

        var newListing = listingService.create(new ListingCreationDto(
                form.getTitle(),
                new Price(form.getPrice()),
                currentUser.getId(),
                form.getProductId(),
                form.getCondition(),
                form.isAcceptsTrade(),
                form.isAcceptsShipping(),
                form.getDescription(),
                imageDataList
        ));
        return new ModelAndView("redirect:/listing/" + newListing.getId());
    }

    private ModelAndView detailsWithErrors(ListingDetailsForm form) {
        var product = productService.getById(form.getProductId());
        return new ModelAndView("listing/new/details")
                .addObject("product", product)
                .addObject("conditionOptions", buildConditionOptions());
    }

    private void populateModel(ModelAndView mav, ChooseProductForm form) {
        var categories = productService.getAllCategories();
        mav.addObject("categories", categories);

        // Create translated category options
        var categoryOptions = new java.util.ArrayList<SelectOption>();
        for (var cat : categories) {
            categoryOptions.add(new SelectOption(cat.getId(), messageSource.getMessage("category." + cat.getName(), null, LocaleContextHolder.getLocale())));
        }
        mav.addObject("categoryOptions", categoryOptions);

        if (form.getCategoryId() != null) {
            var subcategories = productService.getSubcategoriesByCategory(form.getCategoryId());
            mav.addObject("subcategories", subcategories);

            // Create translated subcategory options
            var subcategoryOptions = new java.util.ArrayList<SelectOption>();
            for (var sub : subcategories) {
                subcategoryOptions.add(new SelectOption(sub.getId(), messageSource.getMessage("subcategory." + sub.getName(), null, LocaleContextHolder.getLocale())));
            }
            mav.addObject("subcategoryOptions", subcategoryOptions);
        }

        if (form.getSubcategoryId() != null) {
            mav.addObject("brands", productService.getBrandsBySubcategory(form.getSubcategoryId()));
            List<String> models = List.of();
            // Only fetch models if brand is selected and not "Other"
            if (form.getNewProductBrand() != null && !form.getNewProductBrand().isBlank() && !OTHER_VALUE.equals(form.getNewProductBrand())) {
                models = productService.getModelsBySubcategoryAndBrand(form.getSubcategoryId(), form.getNewProductBrand());
            }
            mav.addObject("models", models);
            mav.addObject("modelsEmpty", models.isEmpty());
        }
    }

    @GetMapping("/{id}/edit")
    public ModelAndView editListing(@PathVariable Long id, @CurrentUser User currentUser) {
        LOGGER.debug("User {} accessing edit page for listing {}", currentUser.getId(), id);
        var listing = listingService.getById(id);
        if (!listing.getCreator().getId().equals(currentUser.getId())) {
            throw new ForbiddenException("Not authorized to edit this listing");
        }
        if (listing.getStatus() == ListingStatus.CANCELED) {
            throw new ForbiddenException("Cannot edit a canceled listing");
        }
        return new ModelAndView("redirect:/listing/new/details?productId="
            + listing.getProduct().getId() + "&editListingId=" + id);
    }

    @PostMapping("/{id}/cancel")
    public ModelAndView cancelListing(@PathVariable Long id, @CurrentUser User currentUser) {
        LOGGER.info("User {} canceled listing {}", currentUser.getId(), id);
        listingService.cancel(id, currentUser.getId());
        return new ModelAndView("redirect:/account/listings");
    }

    private List<StringSelectOption> buildConditionOptions() {
        var options = new java.util.ArrayList<StringSelectOption>();
        for (var c : Condition.values()) {
            options.add(new StringSelectOption(
                c.name(),
                messageSource.getMessage("condition." + c.name(), null, LocaleContextHolder.getLocale())
            ));
        }
        return options;
    }
}
