package ar.edu.itba.paw.webapp.form.validation;

import org.springframework.web.multipart.MultipartFile;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.List;

public class ValidFilesValidator implements ConstraintValidator<ValidFile, List<MultipartFile>> {

    private long maxSizeBytes;
    private String[] allowedTypes;

    @Override
    public void initialize(ValidFile constraintAnnotation) {
        this.maxSizeBytes = constraintAnnotation.maxSizeBytes();
        this.allowedTypes = constraintAnnotation.allowedTypes();
    }

    @Override
    public boolean isValid(List<MultipartFile> files, ConstraintValidatorContext context) {
        if (files == null || files.isEmpty()) {
            return true;
        }
        for (MultipartFile file : files) {
            if (file == null || file.isEmpty()) {
                continue;
            }
            if (!FileValidation.isValidFile(file, maxSizeBytes, allowedTypes)) {
                return false;
            }
        }
        return true;
    }
}
