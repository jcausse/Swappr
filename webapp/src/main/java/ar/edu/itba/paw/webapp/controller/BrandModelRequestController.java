package ar.edu.itba.paw.webapp.controller;

import ar.edu.itba.paw.model.BrandModelRequestType;
import ar.edu.itba.paw.model.User;
import ar.edu.itba.paw.service.BrandModelRequestService;
import ar.edu.itba.paw.service.CatalogService;
import ar.edu.itba.paw.service.dto.BrandModelRequestCreationDto;
import ar.edu.itba.paw.service.exception.BadParameterException;
import ar.edu.itba.paw.service.exception.NotFoundException;
import ar.edu.itba.paw.webapp.auth.CurrentUser;
import ar.edu.itba.paw.webapp.form.BrandModelRequestForm;
import ar.edu.itba.paw.webapp.form.SelectOption;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import javax.validation.Valid;

@RequiredArgsConstructor
@Controller
@RequestMapping("/catalog-requests")
public class BrandModelRequestController {

    private static final Logger LOGGER = LoggerFactory.getLogger(BrandModelRequestController.class);

    private final CatalogService catalogService;
    private final BrandModelRequestService brandModelRequestService;

    @GetMapping("/new")
    @PreAuthorize("isAuthenticated()")
    public ModelAndView form(
            @ModelAttribute("requestForm") BrandModelRequestForm form,
            @RequestParam(value = "subcategoryId", required = false) Long subcategoryId,
            @RequestParam(value = "brandId", required = false) Long brandId
    ) {
        if (form.getSubcategoryId() == null) {
            form.setSubcategoryId(subcategoryId);
        }
        if (form.getBrandId() == null) {
            form.setBrandId(brandId);
        }
        if (brandId != null) {
            form.setType(BrandModelRequestType.EXISTING_BRAND);
        }
        return render(form);
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ModelAndView submit(
            @Valid @ModelAttribute("requestForm") BrandModelRequestForm form,
            BindingResult errors,
            @CurrentUser User currentUser
    ) {
        validate(form, errors);
        if (errors.hasErrors()) {
            return render(form);
        }

        LOGGER.info("User {} requested a new brand/model ({})", currentUser.getId(), form.getModelName());
        try {
            brandModelRequestService.create(new BrandModelRequestCreationDto(
                    currentUser.getId(),
                    form.getType(),
                    form.getSubcategoryId(),
                    form.getBrandId(),
                    form.getNewBrandName(),
                    form.getModelName()
            ));
        } catch (BadParameterException | NotFoundException e) {
            LOGGER.warn("Brand/model request could not be created: {}", e.getMessage());
            return render(form).addObject("requestError", true);
        }

        return new ModelAndView("redirect:/catalog-requests/success");
    }

    @GetMapping("/success")
    @PreAuthorize("isAuthenticated()")
    public ModelAndView success() {
        return new ModelAndView("catalogRequest/success");
    }

    private ModelAndView render(BrandModelRequestForm form) {
        return new ModelAndView("catalogRequest/new")
                .addObject("subcategoryOptions", catalogService.getAllSubcategories().stream()
                        .map(subcategory -> new SelectOption(subcategory.getId(), "subcategory." + subcategory.getName()))
                        .toList())
                .addObject("brandOptions", catalogService.getAllBrands());
    }

    private static void validate(BrandModelRequestForm form, BindingResult errors) {
        if (form.getType() == BrandModelRequestType.EXISTING_BRAND && form.getBrandId() == null) {
            errors.rejectValue("brandId", "error.catalogRequest.existingBrandRequired");
        }
        if (form.getType() == BrandModelRequestType.NEW_BRAND
                && (form.getNewBrandName() == null || form.getNewBrandName().isBlank())) {
            errors.rejectValue("newBrandName", "error.catalogRequest.newBrandRequired");
        }
    }
}
