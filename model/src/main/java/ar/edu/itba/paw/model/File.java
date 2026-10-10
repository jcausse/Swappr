package ar.edu.itba.paw.model;

import lombok.*;
import org.hibernate.annotations.Formula;

import javax.persistence.*;
import java.util.Optional;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "data")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "files")
public class File {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "file_id", nullable = false)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "filename", nullable = false)
    private String filename;

    @Column(name = "alt", nullable = false)
    private String alt;

    @Column(name = "content_type", length = 50)
    private String contentType;

    @Column(name = "data", nullable = false)
    private byte[] data;

    /*
     * Size of `data` in bytes, computed by the database (read-only). This allows showing the file size
     * without needing the blob itself (e.g. summaries of proofs of payment / shipping in offer cards).
     */
    @Formula("octet_length(data)")
    private Long size;

    // -------------------------------------------------------------------------------------------------

    public Optional<String> getContentType() {
        return Optional.ofNullable(contentType);
    }

    public String getExtension() {
        if (filename == null || filename.isBlank()) {
            return "";
        }
        int lastDot = filename.lastIndexOf('.');
        if (lastDot == -1 || lastDot == filename.length() - 1) {
            return "";
        }
        return filename.substring(lastDot + 1).toLowerCase();
    }

    public long getSize() {
        if (size != null) {
            return size;
        }
        // Formula columns are not populated for in-memory (not yet persisted / refreshed) instances
        return data != null ? data.length : 0L;
    }

    public long getSizeKb() {
        return getSize() / 1024;
    }

    public boolean isImage() {
        return contentType != null && contentType.startsWith("image/");
    }
}
