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
    String brand;
    String model;
    int year;
    List<Long> imageIds;

    public static ListingMailDto from(final @NonNull Listing listing) {
        final var brand = listing.getBrand();
        final var model = listing.getModel();
        final List<Long> imageIds = listing.getImageIds() != null
                ? List.copyOf(listing.getImageIds())
                : List.of();
        return ListingMailDto.builder()
                .id(listing.getId())
                .title(listing.getTitle())
                .price(listing.getPrice())
                .brand(brand != null ? brand.getName() : null)
                .model(model != null ? model.getName() : null)
                .year(listing.getYear())
                .imageIds(imageIds)
                .build();
    }
}
