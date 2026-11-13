package ar.edu.itba.paw.model;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum Condition {
    UNUSED("UNUSED"),
    LIKE_NEW("LIKE_NEW"),
    EXCELLENT("EXCELLENT"),
    GOOD("GOOD"),
    FAIR("FAIR"),
    AS_IS("AS_IS"),
    FOR_PARTS("FOR_PARTS");

    @Getter private final String condition;
}
