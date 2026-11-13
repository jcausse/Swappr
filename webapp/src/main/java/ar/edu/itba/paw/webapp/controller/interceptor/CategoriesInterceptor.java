package ar.edu.itba.paw.webapp.controller.interceptor;

import ar.edu.itba.paw.service.CatalogService;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

// Adds the navbar categories to rendered views only. A @ModelAttribute would query them on every request,
// including redirects after a POST and image/file responses that never render a page.
public class CategoriesInterceptor implements HandlerInterceptor {

    private static final String CATEGORIES_ATTRIBUTE = "categories";
    private static final String REDIRECT_PREFIX = "redirect:";

    private final CatalogService catalogService;

    public CategoriesInterceptor(final CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    @Override
    public void postHandle(
            final HttpServletRequest request,
            final HttpServletResponse response,
            final Object handler,
            final ModelAndView modelAndView
    ) {
        if (!rendersView(modelAndView) || modelAndView.getModel().containsKey(CATEGORIES_ATTRIBUTE)) {
            return;
        }
        modelAndView.addObject(CATEGORIES_ATTRIBUTE, catalogService.getAllCategories());
    }

    private static boolean rendersView(final ModelAndView modelAndView) {
        if (modelAndView == null || !modelAndView.hasView()) {
            return false;
        }
        if (modelAndView.isReference()) {
            return !modelAndView.getViewName().startsWith(REDIRECT_PREFIX);
        }
        return !(modelAndView.getView() instanceof RedirectView);
    }
}
