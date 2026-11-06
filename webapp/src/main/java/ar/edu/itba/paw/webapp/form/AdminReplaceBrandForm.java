package ar.edu.itba.paw.webapp.form;

import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class AdminReplaceBrandForm {

    @NotNull
    private Long replacementBrandId;
}
