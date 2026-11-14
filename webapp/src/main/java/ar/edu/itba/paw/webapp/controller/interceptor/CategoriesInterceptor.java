package ar.edu.itba.paw.webapp.controller.interceptor;

import ar.edu.itba.paw.service.CatalogService;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

// Adds the navbar categories to rendered views only. A @ModelAttribute would query them on every request,
// including redirects after a POST and image/file responses that never render a page.
@RequiredArgsConstructor
public class CategoriesInterceptor implements HandlerInterceptor {

    private static final String CATEGORIES_ATTRIBUTE = "categories";
    private static final String REDIRECT_PREFIX = "redirect:";
    private static final String FORWARD_PREFIX = "forward:";

    private final CatalogService catalogService;

    @Override
    public void postHandle(
            @NonNull final HttpServletRequest request,
            @NonNull final HttpServletResponse response,
            @NonNull final Object handler,
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
            final String viewName = modelAndView.getViewName();
            return viewName != null
                    && !viewName.startsWith(REDIRECT_PREFIX)
                    && !viewName.startsWith(FORWARD_PREFIX);
        }
        return !(modelAndView.getView() instanceof RedirectView);
    }
}
