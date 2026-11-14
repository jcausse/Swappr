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
        return ListingFilterDto.builder()
                .query(query)
                .status(status)
                .page(page)
                .pageSize(pageSize)
                .build();
    }
}
