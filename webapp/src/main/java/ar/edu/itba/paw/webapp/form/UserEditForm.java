package ar.edu.itba.paw.webapp.form;

import ar.edu.itba.paw.service.dto.ImageData;
import ar.edu.itba.paw.webapp.form.validation.EmptyOrSize;
import ar.edu.itba.paw.webapp.form.validation.FieldMatch;
import ar.edu.itba.paw.webapp.form.validation.ValidFile;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Optional;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

@NoArgsConstructor
@Getter
@Setter
@FieldMatch(first = "password", second = "confirmPassword")
public class UserEditForm {

    @Size(max = 50)
    private String displayName;

    @EmptyOrSize(min = 8)
    private String password;

    @EmptyOrSize(min = 8)
    private String confirmPassword;

    @ValidFile(message = "{ValidFiles.userForm.profilePicture}", allowedTypes = {"image/*"}, optional = true)
    private MultipartFile profilePicture;

    private Long provinceId;

    @Size(max = 100)
    private String locationDetail;

    @NotEmpty
    private String preferredLanguage;

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
            var rte = new RuntimeException("Failed to read profile picture for user profile modification");
            rte.initCause(e);
            throw rte;
        }
    }
}
