package ar.edu.itba.paw.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.List;

@RequiredArgsConstructor
public enum OfferStatusGroup {
    PENDING("pending"),
    PENDING_PAYMENT("pending_payment"),
    RESOLVED("resolved");

    @Getter
    private final String status;

    public static OfferStatusGroup fromStatus(final OfferStatus status) {
        return Arrays.stream(OfferStatusGroup.values())
                .filter(s -> s.toStatusList().contains(status))
                .findFirst()
                .get();
    }

    public List<OfferStatus> toStatusList() {
        return switch (this) {
            case PENDING -> List.of(OfferStatus.PENDING);
            case PENDING_PAYMENT -> List.of(OfferStatus.PENDING_PAYMENT);
            case RESOLVED -> List.of(OfferStatus.ACCEPTED, OfferStatus.REJECTED, OfferStatus.WITHDRAWN);
        };
    }
}
