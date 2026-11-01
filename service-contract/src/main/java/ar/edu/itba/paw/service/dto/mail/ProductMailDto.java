package ar.edu.itba.paw.service.dto.mail;

import ar.edu.itba.paw.model.Product;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
@Builder
public class ProductMailDto {

    String brand;
    String model;
    Integer year;

    public static ProductMailDto from(final @NonNull Product product) {
        return ProductMailDto.builder()
                .brand(product.getBrand())
                .model(product.getModel())
                .year(product.getYear())
                .build();
    }
}
