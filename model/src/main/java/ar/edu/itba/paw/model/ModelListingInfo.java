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
    Price avgPrice;

    @Builder
    public ModelListingInfo(
            Brand brand,
            Model model,
            long listingCount,
            Condition condition,
            Price minPrice,
            Price maxPrice,
            Price avgPrice
    ) {
        this.brand = brand;
        this.model = model;
        this.listingCount = listingCount;
        this.condition = condition;
        this.minPrice = minPrice;
        this.maxPrice = maxPrice;
        this.avgPrice = avgPrice;
    }

    public ModelListingInfo(
            Brand brand,
            Model model,
            Condition condition,
            long listingCount,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Double avgPrice
    ) {
        this.brand = brand;
        this.model = model;
        this.condition = condition;
        this.listingCount = listingCount;
        this.minPrice = new Price(minPrice);
        this.maxPrice = new Price(maxPrice);
        this.avgPrice = avgPrice != null ? new Price(BigDecimal.valueOf(avgPrice)) : null;
    }
}
