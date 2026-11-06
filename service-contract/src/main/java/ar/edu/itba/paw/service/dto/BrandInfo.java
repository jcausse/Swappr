package ar.edu.itba.paw.service.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class BrandInfo {
    Long id;
    String name;
    long modelCount;
    boolean hasListings;
}
