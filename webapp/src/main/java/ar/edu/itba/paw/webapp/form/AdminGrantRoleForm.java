package ar.edu.itba.paw.webapp.form;

import ar.edu.itba.paw.webapp.form.validation.UserExists;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

@NoArgsConstructor
@Getter
@Setter
public class AdminGrantRoleForm {

    @NotEmpty
    @Size(min = 3, max = 254)
    @UserExists
    private String username;

    public String getUsernameOrEmail() {
        return username;
    }

    public void setUsernameOrEmail(String usernameOrEmail) {
        this.username = usernameOrEmail;
    }
}
