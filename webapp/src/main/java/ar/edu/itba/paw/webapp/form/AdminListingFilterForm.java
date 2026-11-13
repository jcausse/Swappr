package ar.edu.itba.paw.webapp.form;

import ar.edu.itba.paw.model.ListingStatus;
import ar.edu.itba.paw.service.dto.ListingFilterDto;
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

    public ListingFilterDto toDto(int page, int pageSize) {
        // REVIEW: page and pageSize come from the controller, not the form.
        return ListingFilterDto.builder()
                .query(query)
                .status(status)
                .page(page)
                .pageSize(pageSize)
                .build();
    }
}
