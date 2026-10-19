package ar.edu.itba.paw.webapp.controller;

import javax.servlet.http.HttpServletRequest;
import java.net.URI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
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
        final var target = (referer != null && isSameHost(referer, request)) ? referer : "/";
        LOGGER.debug("Changing language, redirecting to {}", target);
        return new ModelAndView("redirect:" + target);
    }

    private boolean isSameHost(String referer, HttpServletRequest request) {
        try {
            URI refererUri = new URI(referer);
            String refererHost = refererUri.getHost();
            String requestHost = request.getServerName();
            int refererPort = refererUri.getPort();
            int requestPort = request.getServerPort();
            // Default ports: 80 for http, 443 for https
            if (refererPort == -1) {
                refererPort = "https".equalsIgnoreCase(refererUri.getScheme()) ? 443 : 80;
            }
            return refererHost != null && refererHost.equalsIgnoreCase(requestHost) && refererPort == requestPort;
        } catch (Exception e) {
            LOGGER.warn("Invalid referer URL: {}", referer);
            return false;
        }
    }
}
