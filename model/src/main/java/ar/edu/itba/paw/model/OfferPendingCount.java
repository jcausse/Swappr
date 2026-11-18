package ar.edu.itba.paw.model;

import java.math.BigDecimal;

public record OfferPendingCount(Long listingId, long count, BigDecimal maxAmount) {}
