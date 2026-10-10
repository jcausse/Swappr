package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.model.Image;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Optional;

@Repository
public class ImageJpaDao implements ImageDao {

    @PersistenceContext
    private EntityManager em;

    @Override
    public Optional<Image> getById(Long id) {
        return Optional.ofNullable(em.find(Image.class, id));
    }

    @Override
    public Image create(String filename, String alt, String contentType, byte[] data) {
        final Image image = Image.builder()
                .filename(filename)
                .alt(alt)
                .contentType(contentType)
                .data(data)
                .build();
        em.persist(image);
        em.flush(); // TODO: [JPA Migration Cleanup]
        return image;
    }

    @Override
    public void delete(Long id) {
        final Image image = em.find(Image.class, id);
        if (image != null) {
            em.remove(image);
            em.flush(); // TODO: [JPA Migration Cleanup]
        }
    }
}
