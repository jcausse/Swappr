package ar.edu.itba.paw.webapp.form;

import ar.edu.itba.paw.model.BrandModelRequestType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@NoArgsConstructor
@Getter
@Setter
public class AdminBrandModelRequestAcceptForm {

    @NotNull
    private BrandModelRequestType type;

    private Long brandId;

    @Size(max = 100)
    private String newBrandName;

    @NotBlank
    @Size(max = 100)
    private String modelName;
}
