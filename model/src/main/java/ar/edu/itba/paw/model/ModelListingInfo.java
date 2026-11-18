package ar.edu.itba.paw.model;

import java.math.BigDecimal;

import lombok.Builder;
import lombok.Value;

@Value
public class ModelListingInfo {
    Long brandId;
    String brandName;
    Long modelId;
    String modelName;
    long listingCount;
    Condition condition;
    Price minPrice;
    Price maxPrice;

    @Builder
    public ModelListingInfo(
            Long brandId,
            String brandName,
            Long modelId,
            String modelName,
            long listingCount,
            Condition condition,
            Price minPrice,
            Price maxPrice
    ) {
        this.brandId = brandId;
        this.brandName = brandName;
        this.modelId = modelId;
        this.modelName = modelName;
        this.listingCount = listingCount;
        this.condition = condition;
        this.minPrice = minPrice;
        this.maxPrice = maxPrice;
    }

    public ModelListingInfo(
            Long brandId,
            String brandName,
            Long modelId,
            String modelName,
            long listingCount,
            Condition condition,
            BigDecimal minPrice,
            BigDecimal maxPrice
    ) {
        this.brandId = brandId;
        this.brandName = brandName;
        this.modelId = modelId;
        this.modelName = modelName;
        this.condition = condition;
        this.listingCount = listingCount;
        this.minPrice = new Price(minPrice);
        this.maxPrice = new Price(maxPrice);
    }
}
