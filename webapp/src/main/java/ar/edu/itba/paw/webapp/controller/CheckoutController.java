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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import ar.edu.itba.paw.model.Listing;
import ar.edu.itba.paw.model.User;
import ar.edu.itba.paw.service.ListingService;
import ar.edu.itba.paw.service.OfferService;
import ar.edu.itba.paw.webapp.auth.CurrentUser;
import ar.edu.itba.paw.webapp.form.CheckoutForm;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Controller
@RequestMapping("/checkout")
public class CheckoutController {

    private static final Logger LOGGER = LoggerFactory.getLogger(CheckoutController.class);
    private static final int TRADE_LISTINGS_PAGE_SIZE = 4;

    private final ListingService listingService;
    private final OfferService offerService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ModelAndView checkout(
        @RequestParam("listingId") Long listingId,
        @ModelAttribute("checkoutForm") CheckoutForm form,
        @CurrentUser User currentUser
    ) {
        LOGGER.debug("User accessing checkout page for listing {}", listingId);
        Listing listing = listingService.getById(listingId);
        form.setListingId(listingId);

        return withUserListings(listing, currentUser, form);
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ModelAndView checkoutPost(
            @Valid @ModelAttribute("checkoutForm") CheckoutForm form,
            BindingResult bindingResult,
            @CurrentUser User currentUser
    ) {
        if (bindingResult.hasErrors()) {
            var listing = listingService.getById(form.getListingId());
            LOGGER.debug("Validation failed for checkout on listing {}", form.getListingId());
            return withUserListings(listing, currentUser, form);
        }

        offerService.create(form.toOfferDto(currentUser.getId()));
        LOGGER.info("User {} submitted {} offer for listing {}", currentUser.getId(), form.getOfferType(), form.getListingId());

        return new ModelAndView("redirect:/listing/" + form.getListingId());
    }

    private ModelAndView withUserListings(Listing listing, User currentUser, CheckoutForm form) {
        if (listing.isAcceptsTrade()) {
            var filter = form.toTradeListingsFilterDto(currentUser.getId(), TRADE_LISTINGS_PAGE_SIZE);
            var userListingsPage = listingService.search(filter);
            return new ModelAndView("checkout/index")
                    .addObject("listing", listing)
                    .addObject("userListingsPage", userListingsPage)
                    .addObject("userListings", userListingsPage.getContent());
        }
        return new ModelAndView("checkout/index").addObject("listing", listing);
    }
}
