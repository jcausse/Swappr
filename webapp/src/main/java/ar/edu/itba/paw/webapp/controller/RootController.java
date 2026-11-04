package ar.edu.itba.paw.webapp.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

import ar.edu.itba.paw.service.ListingService;
import lombok.RequiredArgsConstructor;

@Controller
@PreAuthorize("permitAll()")
@RequiredArgsConstructor
public class RootController {

    private static final Logger LOGGER = LoggerFactory.getLogger(RootController.class);

    private final ListingService listingService;

    @GetMapping("/")
    public ModelAndView landing() {
        LOGGER.debug("Accessing landing page");
        final var landingListings = listingService.getForLanding();

        return new ModelAndView("landing")
                .addObject("hotItems", landingListings.hotItems())
                .addObject("trendingItems", landingListings.trendingItems())
                .addObject("newItems", landingListings.newItems());
    }
}
