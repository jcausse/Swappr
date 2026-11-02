package ar.edu.itba.paw.model;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public final class RatingFilter {

    private final Long ratedId;
    private final List<RatingRole> roles;
    private final List<OfferRating> types;
    private final int page;
    private final int pageSize;

    public static RatingFilter forProfile(
        final Long ratedId,
        final String role,
        final String type,
        final Integer page,
        final int pageSize
    ) {
        final RatingRole resolvedRole = role == null || role.isBlank()
            ? RatingRole.SELLER
            : RatingRole.fromString(role).orElse(RatingRole.SELLER);

        final List<OfferRating> types = type == null || type.isBlank()
            ? null
            : List.of(OfferRating.fromString(type)
                .orElseThrow(() -> new IllegalArgumentException("Illegal rating filter type")));

        return RatingFilter.builder()
            .ratedId(ratedId)
            .roles(List.of(resolvedRole))
            .types(types)
            .page(page == null || page < 1 ? 1 : page)
            .pageSize(pageSize)
            .build();
    }
}
