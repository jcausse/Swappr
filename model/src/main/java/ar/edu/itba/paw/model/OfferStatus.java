package ar.edu.itba.paw.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum OfferStatus {
    PENDING("PENDING"),
    PENDING_PAYMENT("PENDING_PAYMENT"),
    ACCEPTED("ACCEPTED"),
    REJECTED("REJECTED"),
    WITHDRAWN("WITHDRAWN");

    @Getter
    private final String status;
}
