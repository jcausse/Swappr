package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.model.File;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Optional;

@Repository
public class FileJpaDao implements FileDao {

    @PersistenceContext
    private EntityManager em;

    @Override
    public Optional<File> getById(Long id) {
        return Optional.ofNullable(em.find(File.class, id));
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
        em.flush(); // TODO: [JPA Migration Cleanup]
        return file;
    }

    @Override
    public void delete(Long id) {
        final File file = em.find(File.class, id);
        if (file != null) {
            em.remove(file);
            em.flush(); // TODO: [JPA Migration Cleanup]
        }
    }
}
