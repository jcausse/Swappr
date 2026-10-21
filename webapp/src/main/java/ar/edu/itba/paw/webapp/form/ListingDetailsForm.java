package ar.edu.itba.paw.webapp.form;

import ar.edu.itba.paw.service.dto.ImageData;
import ar.edu.itba.paw.webapp.form.validation.ValidFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Digits;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@NoArgsConstructor
@Getter
@Setter
public class ListingDetailsForm {

    @NotNull
    private Long productId;

    @NotBlank
    @Size(min = 3, max = 100)
    private String title;

    @NotNull
    @DecimalMin(value = "0.01")
    @Digits(integer = 9, fraction = 2)
    private BigDecimal price;

    @ValidFile(message = "{ValidFiles.detailsForm.images}", allowedTypes = {"image/*"})
    private List<MultipartFile> images;

    @NotBlank
    private String condition;
    private boolean acceptsTrade;
    private boolean acceptsShipping;

    @Size(max = 2000)
    private String description;

    private Long editListingId;

    public List<ImageData> getImages() {
        List<ImageData> imageDataList = new ArrayList<>();
        if (this.images != null) {
            for (MultipartFile imageFile : this.images) {
                if (imageFile != null && !imageFile.isEmpty()) {
                    try {
                        imageDataList.add(new ImageData(
                            imageFile.getBytes(),
                            imageFile.getOriginalFilename(),
                            imageFile.getContentType()
                        ));
                    } catch (IOException e) {
                        var rte = new RuntimeException("Failed to read listing image for creation");
                        rte.initCause(e);
                        throw rte;
                    }
                }
            }
        }
        return imageDataList;
    }
}
