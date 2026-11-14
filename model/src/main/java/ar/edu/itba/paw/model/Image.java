package ar.edu.itba.paw.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Optional;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "data")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "images")
public class Image {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "image_id", nullable = false)
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

    public Optional<String> getContentType() {
        return Optional.ofNullable(contentType);
    }
}

