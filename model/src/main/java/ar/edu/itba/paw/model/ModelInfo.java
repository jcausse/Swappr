package ar.edu.itba.paw.model;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ModelInfo {
    Long id;
    String name;
    Long brandId;
    String brandName;
    Long subcategoryId;
    String subcategoryName;
    long listingCount;

    public ModelInfo(
            Long id,
            String name,
            Long brandId,
            String brandName,
            Long subcategoryId,
            String subcategoryName,
            long listingCount
    ) {
        this.id = id;
        this.name = name;
        this.brandId = brandId;
        this.brandName = brandName;
        this.subcategoryId = subcategoryId;
        this.subcategoryName = subcategoryName;
        this.listingCount = listingCount;
    }

    public boolean isHasListings() {
        return listingCount > 0;
    }
}
