package ar.edu.itba.paw.service.dto.mail;

import ar.edu.itba.paw.model.Offer;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.Optional;

@Value
@Builder
public class OfferMailDto {

    Long id;
    BigDecimal amount;
    Boolean isFullPrice;
    String trackingNumber;
    UserMailDto buyer;
    UserMailDto seller;
    ListingMailDto listing;
    ListingMailDto offeredListing;
    boolean forFreeListing;

    public Optional<ListingMailDto> getOfferedListing() {
        return Optional.ofNullable(offeredListing);
    }

    public static OfferMailDto from(final @NonNull Offer offer) {
        final var listing = offer.getListing();
        Objects.requireNonNull(listing, "offer.listing cannot be null");
        return OfferMailDto.builder()
                .id(offer.getId())
                .amount(offer.getAmount())
                .isFullPrice(offer.isFullPrice())
                .trackingNumber(offer.getTrackingNumber())
                .buyer(UserMailDto.from(offer.getBuyer()))
                .seller(UserMailDto.from(listing.getCreator()))
                .listing(ListingMailDto.from(listing))
                .offeredListing(offer.getOfferedListing().map(ListingMailDto::from).orElse(null))
                .forFreeListing(offer.isForFreeListing())
                .build();
    }
}
