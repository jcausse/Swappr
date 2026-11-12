package ar.edu.itba.paw.webapp.form;

import ar.edu.itba.paw.model.ListingStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.Size;

@NoArgsConstructor
@Getter
@Setter
public class AdminListingFilterForm {

    @Size(max = 100)
    private String query;
    private ListingStatus status;
}
