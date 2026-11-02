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

    private RatingRole role;
    private OfferRating type;
    @Min(1)
    private Integer page;
}