package ar.edu.itba.paw.webapp.controller;

import ar.edu.itba.paw.service.UserService;
import ar.edu.itba.paw.service.enumeration.OneTimePasswordVerificationResult;
import ar.edu.itba.paw.webapp.auth.AuthHelper;
import ar.edu.itba.paw.webapp.form.EmailVerificationForm;
import ar.edu.itba.paw.webapp.form.ResendVerificationForm;
import lombok.RequiredArgsConstructor;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

@RequiredArgsConstructor
@Controller
@PreAuthorize("permitAll()")
public class EmailVerificationController {
    private static final Logger LOGGER = LoggerFactory.getLogger(EmailVerificationController.class);

    private final UserService userService;
    private final AuthHelper authHelper;

    @GetMapping("/verify")
    public ModelAndView verifyForm(
            @RequestParam(value = "resent", required = false) Boolean resent,
            @ModelAttribute("emailVerificationForm") EmailVerificationForm form
    ) {
        LOGGER.debug("Accessing email verification form");
        return new ModelAndView("verify")
                .addObject("resent", Boolean.TRUE.equals(resent));
    }

    @PostMapping("/verify")
    public ModelAndView verify(
            @Valid @ModelAttribute("emailVerificationForm") EmailVerificationForm form,
            BindingResult errors,
            HttpServletRequest request
    ) {
        if (errors.hasErrors()) {
            LOGGER.debug("Validation failed for email verification form");
            return new ModelAndView("verify");
        }

        final var result = userService.verifyEmail(form.getUsernameOrEmail(), form.getOtp());
        if (result == OneTimePasswordVerificationResult.ACCEPTED) {
            LOGGER.info("User {} successfully verified email", form.getUsernameOrEmail());
            authHelper.login(request, form.getUsernameOrEmail());
            return new ModelAndView("redirect:/");
        }

        LOGGER.debug("Email verification failed for {} with result {}", form.getUsernameOrEmail(), result);
        errors.rejectValue("otp", result == OneTimePasswordVerificationResult.EXPIRED
                ? "verify.error.expiredOtp"
                : "verify.error.invalidOtp"
        );
        return new ModelAndView("verify");
    }

    @PostMapping("/verify/resend")
    public ModelAndView resendVerificationCode(
            @Valid @ModelAttribute("resendVerificationForm") ResendVerificationForm form,
            BindingResult errors,
            RedirectAttributes redirectAttributes
    ) {
        if (errors.hasErrors()) {
            return new ModelAndView("redirect:/verify");
        }
        LOGGER.info("Requested resend of verification email for {}", form.getUsernameOrEmail());
        userService.resendVerificationEmail(form.getUsernameOrEmail());
        redirectAttributes.addAttribute("email", form.getUsernameOrEmail().trim());
        redirectAttributes.addAttribute("resent", true);
        return new ModelAndView("redirect:/verify");
    }
}
