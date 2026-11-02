package ar.edu.itba.paw.webapp.controller;

import ar.edu.itba.paw.model.Listing;
import ar.edu.itba.paw.model.ListingStatus;
import ar.edu.itba.paw.model.User;
import ar.edu.itba.paw.service.ListingService;
import ar.edu.itba.paw.service.OfferService;
import ar.edu.itba.paw.service.dto.ListingFilterDto;
import ar.edu.itba.paw.service.dto.OfferCreationDto;
import ar.edu.itba.paw.webapp.auth.CurrentUser;
import ar.edu.itba.paw.webapp.form.CheckoutForm;
import java.math.BigDecimal;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RequiredArgsConstructor
@Controller
@RequestMapping("/checkout")
public class CheckoutController {

    private static final Logger LOGGER = LoggerFactory.getLogger(CheckoutController.class);

    private final ListingService listingService;
    private final OfferService offerService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ModelAndView checkout(@RequestParam("listingId") Long listingId, @ModelAttribute("checkoutForm") CheckoutForm form, @CurrentUser(required = false) User currentUser) {
        LOGGER.debug("User accessing checkout page for listing {}", listingId);
        Listing listing = listingService.getById(listingId);
        form.setListingId(listingId);
        form.setOfferType("full");

        return withUserListings(listing, currentUser);
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
            return withUserListings(listing, currentUser);
        }

        offerService.create(new OfferCreationDto(
            form.getListingId(),
            currentUser.getId(),
            form.getOfferType(),
            form.getCustomAmount(),
            form.getTradeAmount(),
            form.getMessage(),
            form.getOfferedListingId()
        ));
        LOGGER.info("User {} submitted {} offer for listing {}", currentUser.getId(), form.getOfferType(), form.getListingId());

        return new ModelAndView("redirect:/listing/" + form.getListingId());
    }

    private ModelAndView withUserListings(Listing listing, User currentUser) {
        if (currentUser != null && listing.isAcceptsTrade()) {
            var filter = ListingFilterDto.builder()
                .creatorId(currentUser.getId())
                .status(ListingStatus.ACTIVE.name())
                .page(1)
                .pageSize(100)
                .build();
            var userListings = listingService.search(filter).getContent();
            return new ModelAndView("checkout/index")
                    .addObject("listing", listing)
                    .addObject("userListings", userListings);
        }
        return new ModelAndView("checkout/index").addObject("listing", listing);
    }
}
