package ar.edu.itba.paw.service.dto;

import java.util.Optional;

import ar.edu.itba.paw.model.File;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
public class FileDto {
    private byte[] fileData;
    private String filename;
    private String contentType;

    public Optional<File> toFile() {
        if (fileData == null) return Optional.empty();
        return Optional.of(
            File.builder()
                .data(fileData)
                .filename(filename)
                .contentType(contentType)
                .build()
        );
    }
}
