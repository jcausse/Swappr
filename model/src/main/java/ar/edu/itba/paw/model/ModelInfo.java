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
    Long categoryId;
    Long subcategoryId;
    String subcategoryName;
    boolean hasListings;
}
