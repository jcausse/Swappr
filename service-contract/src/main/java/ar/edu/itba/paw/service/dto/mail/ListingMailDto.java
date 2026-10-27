package ar.edu.itba.paw.service.dto.mail;

import ar.edu.itba.paw.model.Listing;
import ar.edu.itba.paw.model.Price;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class ListingMailDto {

    Long id;
    String title;
    Price price;
    ProductMailDto product;
    List<Long> imageIds;

    public static ListingMailDto from(final @NonNull Listing listing) {
        final var product = listing.getProduct() != null
                ? ProductMailDto.from(listing.getProduct())
                : null;
        final List<Long> imageIds = listing.getImageIds() != null
                ? List.copyOf(listing.getImageIds())
                : List.of();
        return ListingMailDto.builder()
                .id(listing.getId())
                .title(listing.getTitle())
                .price(listing.getPrice())
                .product(product)
                .imageIds(imageIds)
                .build();
    }
}
