package ar.edu.itba.paw.model;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class BrandInfo {
    Long id;
    String name;
    long modelCount;
    long listingCount;

    public BrandInfo(Long id, String name, long modelCount, long listingCount) {
        this.id = id;
        this.name = name;
        this.modelCount = modelCount;
        this.listingCount = listingCount;
    }

    public boolean isHasListings() {
        return listingCount > 0;
    }
}
