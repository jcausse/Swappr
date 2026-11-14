package ar.edu.itba.paw.model;

import java.util.List;

import lombok.Builder;
import lombok.Value;

@Value
public class ModelListingInfoSummary {
    Brand brand;
    Model model;
    List<ModelListingInfo> listingInfoByCondition;

    @Builder
    public ModelListingInfoSummary(
            Brand brand,
            Model model,
            List<ModelListingInfo> listingInfoByCondition
    ) {
        this.brand = brand;
        this.model = model;
        this.listingInfoByCondition = listingInfoByCondition == null
                ? List.of()
                : List.copyOf(listingInfoByCondition);
    }
}
