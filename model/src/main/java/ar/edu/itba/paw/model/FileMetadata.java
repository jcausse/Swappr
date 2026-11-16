package ar.edu.itba.paw.model;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class FileMetadata {
    Long id;
    String filename;
    String alt;
    String contentType;
    Long size;

    public FileMetadata(Long id, String filename, String alt, String contentType, Long size) {
        this.id = id;
        this.filename = filename;
        this.alt = alt;
        this.contentType = contentType;
        this.size = size;
    }

    public boolean isImage() {
        return contentType != null && contentType.startsWith("image/");
    }

    public long getSizeKb() {
        return size != null && size > 0 ? size / 1024 : 0L;
    }

    public String getExtension() {
        if (filename == null || filename.isBlank()) {
            return "";
        }
        final int lastDot = filename.lastIndexOf('.');
        if (lastDot == -1 || lastDot == filename.length() - 1) {
            return "";
        }
        return filename.substring(lastDot + 1).toLowerCase();
    }
}
