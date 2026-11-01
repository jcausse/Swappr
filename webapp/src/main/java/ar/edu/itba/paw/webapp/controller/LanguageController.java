package ar.edu.itba.paw.webapp.controller;

import javax.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
@PreAuthorize("permitAll()")
public class LanguageController {

    private static final Logger LOGGER = LoggerFactory.getLogger(LanguageController.class);

    /*
     * The '?lang=' param is consumed by LocaleChangeInterceptor, which delegates to the
     * DatabaseLocaleResolver. For a logged-in user that resolver persists the choice, so this
     * handler only needs to send the user back where they came from.
     */
    @GetMapping("/language")
    public ModelAndView changeLanguage(HttpServletRequest request) {
        final var referer = request.getHeader("Referer");
        final var target = (referer != null && !referer.isEmpty()) ? referer : "/";
        LOGGER.debug("Changing language, redirecting to {}", target);
        return new ModelAndView("redirect:" + target);
    }
}
