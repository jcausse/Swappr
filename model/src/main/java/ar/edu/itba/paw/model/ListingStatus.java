package ar.edu.itba.paw.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum ListingStatus {
    ACTIVE("ACTIVE"),
    PENDING_TRANSACTION("PENDING_TRANSACTION"),
    OFFERED_IN_TRADE("OFFERED_IN_TRADE"),
    SOLD("SOLD"),
    CANCELED("CANCELED");

    @Getter private final String status;
}
