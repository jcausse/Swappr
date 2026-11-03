package ar.edu.itba.paw.webapp.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

import ar.edu.itba.paw.model.Category;
import ar.edu.itba.paw.model.Listing;
import ar.edu.itba.paw.model.ListingSort;
import ar.edu.itba.paw.model.ListingStatus;
import ar.edu.itba.paw.model.Page;
import ar.edu.itba.paw.service.ListingService;
import ar.edu.itba.paw.service.ProductService;
import ar.edu.itba.paw.service.dto.ListingFilterDto;

@Controller
@PreAuthorize("permitAll()")
public class RootController {

    private static final Logger LOGGER = LoggerFactory.getLogger(RootController.class);

    private static final int LANDING_LIST_SIZE = 12;

    private final ListingService listingService;
    private final ProductService productService;

    public RootController(ListingService listingService, ProductService productService) {
        this.listingService = listingService;
        this.productService = productService;
    }

    @GetMapping("/")
    public ModelAndView landing() {
        LOGGER.debug("Accessing landing page");
        // Hot items: active listings with most active offers
        var hotItemsFilter = ListingFilterDto.builder()
            .sort(ListingSort.MOST_OFFERS)
            .status(ListingStatus.ACTIVE)
            .page(1)
            .pageSize(LANDING_LIST_SIZE)
            .hasActiveOffers(true)
            .build();
        Page<Listing> hotItemsPage = listingService.search(hotItemsFilter);

        // Trending items: active listings with most recent active offers
        var trendingItemsFilter = ListingFilterDto.builder()
            .sort(ListingSort.RECENT_OFFERS)
            .status(ListingStatus.ACTIVE)
            .page(1)
            .pageSize(LANDING_LIST_SIZE)
            .hasActiveOffers(true)
            .build();
        Page<Listing> trendingItemsPage = listingService.search(trendingItemsFilter);

        // New items: most recently created active listings
        var newItemsFilter = ListingFilterDto.builder()
            .sort(ListingSort.RECENT)
            .status(ListingStatus.ACTIVE)
            .page(1)
            .pageSize(LANDING_LIST_SIZE)
            .build();
        Page<Listing> newItemsPage = listingService.search(newItemsFilter);

        var mav = new ModelAndView("landing");
        mav.addObject("hotItems", hotItemsPage.getContent());
        mav.addObject("trendingItems", trendingItemsPage.getContent());
        mav.addObject("newItems", newItemsPage.getContent());
        return mav;
    }
}
