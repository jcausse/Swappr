package ar.edu.itba.paw.webapp.form;

import javax.validation.constraints.Min;

import ar.edu.itba.paw.model.OfferStatusGroup;
import ar.edu.itba.paw.service.dto.OfferFilterDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class OfferFilterForm {

    private OfferStatusGroup statusGroup = OfferStatusGroup.PENDING;
    @Min(1)
    private Integer page;

    public void setStatusGroup(OfferStatusGroup statusGroup) {
        this.statusGroup = statusGroup == null ? OfferStatusGroup.PENDING : statusGroup;
    }

    public Integer getPage() {
        return page == null || page < 1 ? 1 : page;
    }

    public OfferFilterDto toDto(Long sellerId, Long buyerId, int pageSize) {
        // REVIEW: exactly one of sellerId/buyerId and pageSize are supplied by the controller, not the form.
        return OfferFilterDto.builder()
                .sellerId(sellerId)
                .buyerId(buyerId)
                .statusGroup(statusGroup)
                .page(getPage())
                .pageSize(pageSize)
                .build();
    }
}
