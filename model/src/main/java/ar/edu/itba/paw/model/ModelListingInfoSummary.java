package ar.edu.itba.paw.model;

import java.util.List;

import lombok.Builder;
import lombok.Value;

@Value
public class ModelListingInfoSummary {
    Long brandId;
    String brandName;
    Long modelId;
    String modelName;
    List<ModelListingInfo> listingInfoByCondition;

    @Builder
    public ModelListingInfoSummary(
            Long brandId,
            String brandName,
            Long modelId,
            String modelName,
            List<ModelListingInfo> listingInfoByCondition
    ) {
        this.brandId = brandId;
        this.brandName = brandName;
        this.modelId = modelId;
        this.modelName = modelName;
        this.listingInfoByCondition = listingInfoByCondition == null
                ? List.of()
                : List.copyOf(listingInfoByCondition);
    }
}
