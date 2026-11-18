package ar.edu.itba.paw.webapp.form;

import ar.edu.itba.paw.model.OfferStatusGroup;
import ar.edu.itba.paw.service.dto.OfferFilterDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class AdminOfferFilterForm {

    private OfferStatusGroup statusGroup;

    public OfferFilterDto toDto(int page, int pageSize) {
        return OfferFilterDto.builder()
                .statusGroup(statusGroup)
                .page(page)
                .pageSize(pageSize)
                .build();
    }
}
