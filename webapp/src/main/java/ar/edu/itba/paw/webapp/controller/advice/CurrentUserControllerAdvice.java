package ar.edu.itba.paw.webapp.controller.advice;

import ar.edu.itba.paw.model.User;
import ar.edu.itba.paw.webapp.auth.AuthHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.Optional;

@ControllerAdvice
@RequiredArgsConstructor
public class CurrentUserControllerAdvice {

    private final AuthHelper authHelper;

    /* Get the currently logged-in User */
    @ModelAttribute(value = "currentUser", binding = false)
    public Optional<User> currentUser() {
        return authHelper.getCurrentUser();
    }
}
