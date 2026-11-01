package ar.edu.itba.paw.webapp.form;

import ar.edu.itba.paw.service.dto.ImageData;
import ar.edu.itba.paw.webapp.form.validation.FieldMatch;
import ar.edu.itba.paw.webapp.form.validation.UniqueEmail;
import ar.edu.itba.paw.webapp.form.validation.UniqueUsername;
import ar.edu.itba.paw.webapp.form.validation.Username;
import ar.edu.itba.paw.webapp.form.validation.ValidFile;

import java.io.IOException;
import java.util.Optional;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@NoArgsConstructor
@Getter
@Setter
@FieldMatch(first = "password", second = "confirmPassword")
public class UserForm {

    @NotEmpty
    @Size(min = 3, max = 24)
    @Username
    @UniqueUsername
    private String username;

    @NotEmpty
    @Size(max = 50)
    private String displayName;

    @NotEmpty
    @Email
    @Size(max = 254)
    @UniqueEmail
    private String email;

    @NotEmpty
    @Size(min = 8)
    private String password;

    @NotEmpty
    @Size(min = 8)
    private String confirmPassword;

    @ValidFile(message = "{ValidFiles.userForm.profilePicture}", allowedTypes = {"image/*"}, optional = true)
    private MultipartFile profilePicture;

    public Optional<ImageData> getProfilePicture() {
        if (this.profilePicture == null || this.profilePicture.isEmpty()) {
            return Optional.empty();
        }

        try {
            return Optional.of(new ImageData(
                this.profilePicture.getBytes(),
                this.profilePicture.getOriginalFilename(),
                this.profilePicture.getContentType()
            ));
        } catch (IOException e) {
            var rte = new RuntimeException("Failed to read profile picture for user registration");
            rte.initCause(e);
            throw rte;
        }
    }
}
