package ar.edu.itba.paw.webapp.controller.interceptor;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

final class InterceptorUtils {

    private static final String REDIRECT_PREFIX = "redirect:";
    private static final String FORWARD_PREFIX = "forward:";

    private InterceptorUtils() {
    }

    static boolean rendersView(final ModelAndView modelAndView) {
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
