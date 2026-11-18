package ar.edu.itba.paw.service.dto;

import ar.edu.itba.paw.model.BrandModelRequestType;

public record BrandModelRequestCreationDto(
    Long requesterId,
    BrandModelRequestType type,
    Long subcategoryId,
    Long brandId,
    String newBrandName,
    String modelName
) {}
