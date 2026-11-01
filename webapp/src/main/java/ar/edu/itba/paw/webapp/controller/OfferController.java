package ar.edu.itba.paw.webapp.controller;

import ar.edu.itba.paw.model.User;
import ar.edu.itba.paw.service.OfferService;
import ar.edu.itba.paw.webapp.auth.CurrentUser;
import lombok.RequiredArgsConstructor;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RequiredArgsConstructor
@Controller
@RequestMapping("/offer")
public class OfferController {

    private static final Logger LOGGER = LoggerFactory.getLogger(OfferController.class);

    private final OfferService offerService;

    @PostMapping("/{offerId}/accept")
    @PreAuthorize("@webPermissionService.canManageOfferAsSeller(authentication, #offerId)")
    public ModelAndView acceptOffer(@PathVariable Long offerId, @CurrentUser User currentUser) {
        LOGGER.info("User {} accepted offer {}", currentUser.getId(), offerId);
        offerService.accept(offerId, currentUser.getId());

        return new ModelAndView("redirect:/account/incoming-offers?statusGroup=pending_payment");
    }

    @PostMapping("/{offerId}/reject")
    @PreAuthorize("@webPermissionService.canManageOfferAsSeller(authentication, #offerId)")
    public ModelAndView rejectOffer(@PathVariable Long offerId, @CurrentUser User currentUser) {
        LOGGER.info("User {} rejected offer {}", currentUser.getId(), offerId);
        offerService.reject(offerId, currentUser.getId());

        return new ModelAndView("redirect:/account/incoming-offers?statusGroup=resolved");
    }

    @PostMapping("/{offerId}/withdraw")
    @PreAuthorize("@webPermissionService.canManageOfferAsBuyer(authentication, #offerId)")
    public ModelAndView withdrawOffer(@PathVariable Long offerId,
                                      @CurrentUser User currentUser,
                                      @RequestHeader(value = "Referer", required = false) String referer) {
        LOGGER.info("User {} withdrew offer {}", currentUser.getId(), offerId);

        final var offer = offerService.withdraw(offerId, currentUser.getId());

        String redirectUrl = "/listing/" + offer.getListing().getId();
        if (referer != null && (referer.contains("/account/my-offers") || referer.contains("/account/incoming-offers"))) {
            redirectUrl = referer;
        }
        return new ModelAndView("redirect:" + redirectUrl);
    }

    @PostMapping("/{offerId}/confirm-payment")
    @PreAuthorize("@webPermissionService.canManageOfferAsSeller(authentication, #offerId)")
    public ModelAndView confirmPayment(@PathVariable Long offerId, @CurrentUser User currentUser) {
        LOGGER.info("User {} confirmed payment for offer {}", currentUser.getId(), offerId);
        offerService.confirmPayment(offerId, currentUser.getId());

        return new ModelAndView("redirect:/account/incoming-offers?statusGroup=resolved");
    }
}
