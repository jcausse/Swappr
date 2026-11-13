package ar.edu.itba.paw.webapp.form;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class AdminCreateModelFilterForm {

    private Long categoryId;
    private Boolean isAutoSubmit = false;
}
