package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.model.File;
import ar.edu.itba.paw.model.FileMetadata;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class FileJpaDao implements FileDao {

    @PersistenceContext
    private EntityManager em;

    @Override
    public Optional<File> getById(Long id) {
        return Optional.ofNullable(em.find(File.class, id));
    }

    @Override
    public Map<Long, FileMetadata> getMetadataByIds(Collection<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Map.of();
        }
        return em.createQuery(
                        "SELECT new ar.edu.itba.paw.model.FileMetadata(f.id, f.filename, f.alt, f.contentType, f.size)"
                        + " FROM File f WHERE f.id IN :ids",
                        FileMetadata.class)
                .setParameter("ids", ids)
                .getResultStream()
                .collect(Collectors.toMap(FileMetadata::getId, metadata -> metadata));
    }

    @Override
    public File create(String filename, String alt, String contentType, byte[] data) {
        final File file = File.builder()
                .filename(filename)
                .alt(alt)
                .contentType(contentType)
                .data(data)
                .build();
        em.persist(file);
        return file;
    }
}
