package ar.edu.itba.paw.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum ModelSort {
    ID_ASC("id", true),
    ID_DESC("id", false),
    NAME_ASC("name", true),
    NAME_DESC("name", false);

    @Getter private final String key;
    @Getter private final boolean ascending;
}
