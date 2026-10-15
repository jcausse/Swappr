package ar.edu.itba.paw.webapp.controller;

import ar.edu.itba.paw.model.Listing;
import ar.edu.itba.paw.model.Offer;
import ar.edu.itba.paw.model.User;
import ar.edu.itba.paw.service.OfferService;
import ar.edu.itba.paw.service.exception.NotFoundException;
import ar.edu.itba.paw.webapp.auth.CurrentUser;
import lombok.RequiredArgsConstructor;
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
    public ModelAndView acceptOffer(@PathVariable Long offerId, @CurrentUser User currentUser) {
        LOGGER.info("User {} accepted offer {}", currentUser.getId(), offerId);
        offerService.accept(offerId, currentUser.getId());

        return new ModelAndView("redirect:/account/incoming-offers?statusGroup=pending_payment");
    }

    @PostMapping("/{offerId}/reject")
    public ModelAndView rejectOffer(@PathVariable Long offerId, @CurrentUser User currentUser) {
        LOGGER.info("User {} rejected offer {}", currentUser.getId(), offerId);
        offerService.reject(offerId, currentUser.getId());

        return new ModelAndView("redirect:/account/incoming-offers?statusGroup=resolved");
    }

    @PostMapping("/{offerId}/withdraw")
    public ModelAndView withdrawOffer(@PathVariable Long offerId,
                                      @CurrentUser User currentUser,
                                      @RequestHeader(value = "Referer", required = false) String referer) {
        LOGGER.info("User {} withdrew offer {}", currentUser.getId(), offerId);
        final Offer offer = offerService.getById(offerId)
                .orElseThrow(() -> NotFoundException.createFor("Offer"));           // TODO: Mover al Service

        final Listing listing = offer.getListing();
        offerService.withdraw(offerId, currentUser.getId());

        String redirectUrl = "/listing/" + listing.getId();
        if (referer != null && (referer.contains("/account/my-offers") || referer.contains("/account/incoming-offers"))) {
            redirectUrl = referer;
        }
        return new ModelAndView("redirect:" + redirectUrl);
    }

    @PostMapping("/{offerId}/confirm-payment")
    public ModelAndView confirmPayment(@PathVariable Long offerId, @CurrentUser User currentUser) {
        LOGGER.info("User {} confirmed payment for offer {}", currentUser.getId(), offerId);
        offerService.confirmPayment(offerId, currentUser.getId());

        return new ModelAndView("redirect:/account/incoming-offers?statusGroup=resolved");
    }
}
