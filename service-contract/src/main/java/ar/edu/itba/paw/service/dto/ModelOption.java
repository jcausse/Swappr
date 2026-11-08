package ar.edu.itba.paw.service.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ModelOption {
    Long id;
    String label;
}
