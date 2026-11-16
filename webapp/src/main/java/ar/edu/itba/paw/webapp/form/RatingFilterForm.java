package ar.edu.itba.paw.webapp.form;

import ar.edu.itba.paw.model.OfferRating;
import ar.edu.itba.paw.model.RatingRole;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class RatingFilterForm {

    private RatingRole role = RatingRole.SELLER;
    private OfferRating type;

    public void setRole(RatingRole role) {
        this.role = role == null ? RatingRole.SELLER : role;
    }
}