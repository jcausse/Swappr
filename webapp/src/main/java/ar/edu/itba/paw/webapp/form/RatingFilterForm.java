package ar.edu.itba.paw.webapp.form;

import ar.edu.itba.paw.model.OfferRating;
import ar.edu.itba.paw.model.RatingRole;

import javax.validation.constraints.Min;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class RatingFilterForm {

    private RatingRole role = RatingRole.SELLER;
    private OfferRating type;
    @Min(1)
    private Integer page;

    public void setRole(RatingRole role) {
        this.role = role == null ? RatingRole.SELLER : role;
    }

    public Integer getPage() {
        return page == null || page < 1 ? 1 : page;
    }
}