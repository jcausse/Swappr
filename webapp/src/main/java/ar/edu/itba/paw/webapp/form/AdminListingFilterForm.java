package ar.edu.itba.paw.webapp.form;

import ar.edu.itba.paw.model.ListingStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class AdminListingFilterForm {

    private String query;
    private ListingStatus status;
}
