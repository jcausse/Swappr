package ar.edu.itba.paw.model;

import lombok.RequiredArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Locale;

@RequiredArgsConstructor
public enum Language {
    SPANISH("es"),
    ENGLISH("en");

    @Getter private final String code;

    public Locale toLocale() {
        return Locale.of(code);
    }

    public static Language getDefault() {
        return Language.ENGLISH;
    }

    public static Language fromCode(final String code) {
        return Arrays.stream(Language.values())
                .filter(l -> l.getCode().equalsIgnoreCase(code) || l.name().equalsIgnoreCase(code))
                .findFirst()
                .orElse(getDefault());
    }
}
