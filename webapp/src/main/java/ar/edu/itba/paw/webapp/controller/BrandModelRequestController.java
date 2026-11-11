package ar.edu.itba.paw.webapp.controller;

import ar.edu.itba.paw.model.User;
import ar.edu.itba.paw.service.BrandModelRequestService;
import ar.edu.itba.paw.service.CatalogService;
import ar.edu.itba.paw.service.dto.BrandModelRequestCreationDto;
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
            @RequestParam(value = "categoryId", required = false) Long categoryId,
            @RequestParam(value = "subcategoryId", required = false) Long subcategoryId,
            @RequestParam(value = "brandId", required = false) Long brandId,
            @RequestParam(value = "duplicate", defaultValue = "false") boolean duplicate
    ) {
        form.applyDefaults(categoryId, subcategoryId, brandId);
        final ModelAndView mav = render(form);
        if (duplicate) {
            mav.addObject("formError", "error.catalogRequest.duplicate");
        }
        return mav;
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ModelAndView submit(
            @Valid @ModelAttribute("requestForm") BrandModelRequestForm form,
            BindingResult errors,
            @CurrentUser User currentUser
    ) {
        if (errors.hasErrors()) {
            return render(form);
        }

        LOGGER.info("User {} requested a new brand/model ({})", currentUser.getId(), form.getModelName());
        brandModelRequestService.create(new BrandModelRequestCreationDto(
                currentUser.getId(),
                form.getType(),
                form.getSubcategoryId(),
                form.getBrandId(),
                form.getNewBrandName(),
                form.getModelName()
        ));

        return new ModelAndView("redirect:/catalog-requests/success");
    }

    @GetMapping("/success")
    @PreAuthorize("isAuthenticated()")
    public ModelAndView success() {
        return new ModelAndView("catalogRequest/success");
    }

    private ModelAndView render(BrandModelRequestForm form) {
        final ModelAndView mav = new ModelAndView("catalogRequest/new")
                .addObject("categoryOptions", catalogService.getAllCategories().stream()
                        .map(category -> new SelectOption(category.getId(), "category." + category.getName()))
                        .toList())
                .addObject("brandOptions", catalogService.getAllBrands());

        if (form.getCategoryId() != null) {
            mav.addObject("subcategoryOptions", catalogService.getSubcategoriesByCategory(form.getCategoryId())
                    .stream()
                    .map(subcategory -> new SelectOption(subcategory.getId(), "subcategory." + subcategory.getName()))
                    .toList());
        }

        return mav;
    }
}
