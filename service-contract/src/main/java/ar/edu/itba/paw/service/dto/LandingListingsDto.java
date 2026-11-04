package ar.edu.itba.paw.service.dto;

import ar.edu.itba.paw.model.Listing;
import java.util.List;

public record LandingListingsDto(
    List<Listing> hotItems,
    List<Listing> trendingItems,
    List<Listing> newItems
) {}
