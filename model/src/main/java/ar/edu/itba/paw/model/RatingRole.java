package ar.edu.itba.paw.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum RatingRole {
    BUYER("BUYER"),
    SELLER("SELLER");

    @Getter
    private final String role;
}
