package ar.edu.itba.paw.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum BrandSort {
    ID("id"),
    BRAND("brand");

    @Getter private final String key;
}
