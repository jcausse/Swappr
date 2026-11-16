package ar.edu.itba.paw.webapp.form;

import ar.edu.itba.paw.model.OfferRating;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@NoArgsConstructor
@Getter
@Setter
public class RateForm {

    @NotNull
    private OfferRating rating;

    @Size(max = 2000)
    private String reviewText;
}