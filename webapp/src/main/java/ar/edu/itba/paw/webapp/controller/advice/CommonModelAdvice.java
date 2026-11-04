package ar.edu.itba.paw.webapp.controller.advice;

import ar.edu.itba.paw.model.Category;
import ar.edu.itba.paw.service.ProductService;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.List;

@ControllerAdvice
public class CommonModelAdvice {

    private final ProductService productService;

    public CommonModelAdvice(ProductService productService) {
        this.productService = productService;
    }

    // Fetch categories on all routes, since we show them in the navbar
    @ModelAttribute(value = "categories", binding = false)
    public List<Category> categories() {
        return productService.getAllCategories();
    }
}
