package ar.edu.itba.paw.model;

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
}
