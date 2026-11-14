package ar.edu.itba.paw.webapp.form;

import ar.edu.itba.paw.model.BrandModelRequestType;
import ar.edu.itba.paw.service.dto.BrandModelRequestAcceptanceDto;
import ar.edu.itba.paw.webapp.form.validation.ValidBrandModelRequestAccept;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@NoArgsConstructor
@Getter
@Setter
@ValidBrandModelRequestAccept
public class AdminBrandModelRequestAcceptForm implements BrandModelRequestFields {

    @NotNull
    private BrandModelRequestType type;

    private Long brandId;

    @Size(max = 100)
    private String newBrandName;

    @NotBlank
    @Size(max = 100)
    private String modelName;

    public BrandModelRequestAcceptanceDto toDto() {
        return new BrandModelRequestAcceptanceDto(type, brandId, newBrandName, modelName);
    }
}
