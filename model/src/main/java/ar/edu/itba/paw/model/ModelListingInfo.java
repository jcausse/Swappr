package ar.edu.itba.paw.model;

import java.math.BigDecimal;

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
public class ModelListingInfo {
    private Brand brand;
    private Model model;
    private Long listingCount;
    private Condition condition;
    private Price minPrice;
    private Price maxPrice;
    private Price avgPrice;

    public ModelListingInfo(
            Brand brand,
            Model model,
            Condition condition,
            Long listingCount,
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
