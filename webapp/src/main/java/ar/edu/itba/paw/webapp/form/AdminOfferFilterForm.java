package ar.edu.itba.paw.webapp.form;

import ar.edu.itba.paw.model.OfferStatusGroup;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class AdminOfferFilterForm {

    private OfferStatusGroup statusGroup;
}
