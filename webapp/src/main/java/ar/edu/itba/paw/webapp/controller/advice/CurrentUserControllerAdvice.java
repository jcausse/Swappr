package ar.edu.itba.paw.webapp.controller.advice;

import ar.edu.itba.paw.model.User;
import ar.edu.itba.paw.webapp.auth.AuthUserDetails;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.Optional;

@ControllerAdvice
public class CurrentUserControllerAdvice {

    /* Get the currently logged-in User */
    @ModelAttribute(value = "currentUser", binding = false)
    public Optional<User> currentUser() {
        final var auth = SecurityContextHolder.getContext().getAuthentication();
        // TODO use permission service
        if (auth != null && auth.getPrincipal() instanceof AuthUserDetails) {
            return Optional.of(((AuthUserDetails) auth.getPrincipal()).getDomainUser());
        }
        return Optional.empty();
    }
}
