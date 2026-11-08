package ar.edu.itba.paw.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum BrandSort {
    ID_ASC("id", true),
    ID_DESC("id", false),
    BRAND_ASC("brand", true),
    BRAND_DESC("brand", false);

    @Getter private final String key;
    @Getter private final boolean ascending;
}
