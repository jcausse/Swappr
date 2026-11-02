package ar.edu.itba.paw.webapp.form;

import javax.validation.constraints.Min;

import ar.edu.itba.paw.model.OfferStatusGroup;
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
}
