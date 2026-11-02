package ar.edu.itba.paw.webapp.form;

import ar.edu.itba.paw.model.OfferStatusGroup;
import javax.validation.constraints.Min;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class OfferFilterForm {

    private String statusGroup = OfferStatusGroup.PENDING.getStatus();
    @Min(1)
    private Integer page;

    public void setStatusGroup(String statusGroup) {
        this.statusGroup = statusGroup == null || statusGroup.isBlank()
            ? OfferStatusGroup.PENDING.getStatus()
            : statusGroup;
    }
}
