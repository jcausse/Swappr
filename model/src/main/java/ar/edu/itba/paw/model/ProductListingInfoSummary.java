package ar.edu.itba.paw.model;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class ProductListingInfoSummary {
    private String brand;
    private String model;
    private List<ProductListingInfo> listingInfoByCondition;
}
