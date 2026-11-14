package ar.edu.itba.paw.model;

import java.math.BigDecimal;

import lombok.Builder;
import lombok.Value;

@Value
public class ModelListingInfo {
    Brand brand;
    Model model;
    long listingCount;
    Condition condition;
    Price minPrice;
    Price maxPrice;

    @Builder
    public ModelListingInfo(
            Brand brand,
            Model model,
            long listingCount,
            Condition condition,
            Price minPrice,
            Price maxPrice
    ) {
        this.brand = brand;
        this.model = model;
        this.listingCount = listingCount;
        this.condition = condition;
        this.minPrice = minPrice;
        this.maxPrice = maxPrice;
    }

    public ModelListingInfo(
            Brand brand,
            Model model,
            Condition condition,
            long listingCount,
            BigDecimal minPrice,
            BigDecimal maxPrice
    ) {
        this.brand = brand;
        this.model = model;
        this.condition = condition;
        this.listingCount = listingCount;
        this.minPrice = new Price(minPrice);
        this.maxPrice = new Price(maxPrice);
    }
}
