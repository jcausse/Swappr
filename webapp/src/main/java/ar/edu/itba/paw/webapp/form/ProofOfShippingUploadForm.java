package ar.edu.itba.paw.webapp.form;

import ar.edu.itba.paw.service.dto.FileDto;
import ar.edu.itba.paw.webapp.form.validation.ValidFile;
import ar.edu.itba.paw.webapp.form.validator.ValidProofOfShippingUploadForm;

import java.io.IOException;

import org.springframework.web.multipart.MultipartFile;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@ValidProofOfShippingUploadForm
@NoArgsConstructor
@Getter
@Setter
public class ProofOfShippingUploadForm {

    private String trackingNumber;

    @ValidFile(optional = true)
    private MultipartFile file;

    public FileDto getFileDto() {
        if (this.getFile() == null || this.getFile().isEmpty()) return new FileDto();
        try {
            return new FileDto(
                this.getFile().getBytes(),
                this.getFile().getOriginalFilename(),
                this.getFile().getContentType()
            );
        } catch (IOException e) {
            throw new RuntimeException("Failed to read uploaded file", e);
        }
    }
}
