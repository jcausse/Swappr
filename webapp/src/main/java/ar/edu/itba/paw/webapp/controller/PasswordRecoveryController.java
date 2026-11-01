package ar.edu.itba.paw.webapp.controller;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;

import ar.edu.itba.paw.service.PasswordRecoveryService;
import ar.edu.itba.paw.service.enumeration.OneTimePasswordVerificationResult;
import ar.edu.itba.paw.webapp.auth.AuthHelper;
import ar.edu.itba.paw.webapp.form.PasswordRecoveryRequestForm;
import ar.edu.itba.paw.webapp.form.PasswordRecoveryVerificationForm;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Controller
@PreAuthorize("permitAll()")
public class PasswordRecoveryController {

    private static final Logger LOGGER = LoggerFactory.getLogger(PasswordRecoveryController.class);

    private final PasswordRecoveryService passwordRecoveryService;
    private final AuthHelper authHelper;

    @GetMapping("/recovery/request")
    public ModelAndView passwordRecoveryRequestGET(
            @ModelAttribute("passwordRecoveryRequestForm") PasswordRecoveryRequestForm form
    ) {
        LOGGER.debug("Accessing password recovery request form");
        return new ModelAndView("recovery/request");
    }

    @PostMapping("/recovery/request")
    public ModelAndView passwordRecoveryRequestPOST(
            @Valid @ModelAttribute("passwordRecoveryRequestForm") PasswordRecoveryRequestForm form,
            BindingResult errors
    ) {
        if (errors.hasErrors()) {
            LOGGER.debug("Validation failed for password recovery request form");
            return new ModelAndView("recovery/request");
        }

        LOGGER.info("Password recovery requested for {}", form.getUsernameOrEmail());
        passwordRecoveryService.startAndSendRecoveryEmail(form.getUsernameOrEmail());

        return new ModelAndView("recovery/request")
                .addObject("codeSent", true)
                .addObject("usernameOrEmail", form.getUsernameOrEmail().trim().toLowerCase());
    }

    @GetMapping("/recovery/verification")
    public ModelAndView passwordRecoveryVerificationGET(
            @ModelAttribute("passwordRecoveryVerificationForm") PasswordRecoveryVerificationForm form
    ) {
        LOGGER.debug("Accessing password recovery verification form");
        return new ModelAndView("recovery/verification");
    }

    @PostMapping("/recovery/verification")
    public ModelAndView passwordRecoveryVerificationPOST(
            @Valid @ModelAttribute("passwordRecoveryVerificationForm") PasswordRecoveryVerificationForm form,
            BindingResult errors
    ) {
        if (errors.hasErrors()) {
            LOGGER.debug("Validation failed for password recovery verification form");
            return new ModelAndView("recovery/verification");
        }

        final var result = passwordRecoveryService.verifyAndUpdatePassword(
                form.getUsernameOrEmail(),
                form.getPassword(),
                form.getOtp()
        );

        if (result == OneTimePasswordVerificationResult.ACCEPTED) {
            LOGGER.info("Password successfully recovered for {}", form.getUsernameOrEmail());
            authHelper.login(form.getUsernameOrEmail());
            return new ModelAndView("redirect:/");
        }

        LOGGER.debug("Password recovery verification failed for {} with result {}", form.getUsernameOrEmail(), result);
        errors.rejectValue("otp", result == OneTimePasswordVerificationResult.EXPIRED
                ? "recovery.verification.error.expiredOtp"
                : "recovery.verification.error.invalidOtp"
        );
        return new ModelAndView("recovery/verification");
    }
}
