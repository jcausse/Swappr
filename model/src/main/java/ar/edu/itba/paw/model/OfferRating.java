package ar.edu.itba.paw.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum OfferRating {
    POSITIVE("POSITIVE"),
    NEUTRAL("NEUTRAL"),
    NEGATIVE("NEGATIVE");

    @Getter
    private final String rating;
}