package ar.edu.itba.paw.webapp.controller.interceptor;

import ar.edu.itba.paw.service.CatalogService;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

// Adds the navbar categories to rendered views only. A @ModelAttribute would query them on every request,
// including redirects after a POST and image/file responses that never render a page.
@RequiredArgsConstructor
public class CategoriesInterceptor implements HandlerInterceptor {

    private static final String CATEGORIES_ATTRIBUTE = "categories";

    private final CatalogService catalogService;

    @Override
    public void postHandle(
            @NonNull final HttpServletRequest request,
            @NonNull final HttpServletResponse response,
            @NonNull final Object handler,
            final ModelAndView modelAndView
    ) {
        if (!InterceptorUtils.rendersView(modelAndView) || modelAndView.getModel().containsKey(CATEGORIES_ATTRIBUTE)) {
            return;
        }
        modelAndView.addObject(CATEGORIES_ATTRIBUTE, catalogService.getAllCategories());
    }
}
