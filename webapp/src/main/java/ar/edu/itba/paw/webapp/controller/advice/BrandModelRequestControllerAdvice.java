package ar.edu.itba.paw.webapp.controller.advice;

import ar.edu.itba.paw.service.CatalogService;
import ar.edu.itba.paw.service.exception.BrandModelRequestAlreadyExistsException;
import ar.edu.itba.paw.service.dto.BrandModelRequestCreationDto;
import ar.edu.itba.paw.webapp.controller.BrandModelRequestController;
import ar.edu.itba.paw.webapp.form.BrandModelRequestForm;
import ar.edu.itba.paw.webapp.form.SelectOption;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

@ControllerAdvice(assignableTypes = BrandModelRequestController.class)
@RequiredArgsConstructor
public class BrandModelRequestControllerAdvice {

    private final CatalogService catalogService;

    @ExceptionHandler(BrandModelRequestAlreadyExistsException.class)
    public ModelAndView handleAlreadyExists(BrandModelRequestAlreadyExistsException ex) {
        final BrandModelRequestCreationDto request = ex.getRequest();
        final BrandModelRequestForm form = new BrandModelRequestForm();
        form.setType(request.type());
        form.setSubcategoryId(request.subcategoryId());
        form.setBrandId(request.brandId());
        form.setNewBrandName(request.newBrandName());
        form.setModelName(request.modelName());

        return new ModelAndView("catalogRequest/new")
                .addObject("requestForm", form)
                .addObject("formError", "error.catalogRequest.duplicate")
                .addObject("subcategoryOptions", catalogService.getAllSubcategories().stream()
                        .map(subcategory -> new SelectOption(
                                subcategory.getId(), "subcategory." + subcategory.getName()))
                        .toList())
                .addObject("brandOptions", catalogService.getAllBrands());
    }
}
