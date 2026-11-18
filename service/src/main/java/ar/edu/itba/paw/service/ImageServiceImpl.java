package ar.edu.itba.paw.service;

import ar.edu.itba.paw.model.Image;
import ar.edu.itba.paw.persistence.ImageDao;
import java.util.Optional;
import java.util.List;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class ImageServiceImpl implements ImageService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ImageServiceImpl.class);

    private final ImageDao imageDao;

    @Override
    public Optional<Image> getById(Long id) {
        return imageDao.getById(id);
    }

    @Override
    @Transactional
    public Image create(String filename, String alt, String contentType, byte[] data) {
        LOGGER.debug("Creating image: filename='{}', contentType='{}', size={} bytes",
                filename, contentType, data != null ? data.length : 0);
        Image image = imageDao.create(filename, alt, contentType, data);
        LOGGER.info("Image created with id={}", image.getId());
        return image;
    }

    @Override
    @Transactional
    public void delete(Long id) {
        LOGGER.info("Deleting image with id={}", id);
        imageDao.delete(id);
    }

    @Override
    @Transactional
    public List<Image> createAll(final List<Image> images) {
        LOGGER.debug("Creating {} images", images.size());
        final List<Image> created = imageDao.createAll(images);
        LOGGER.info("Images created with ids={}", created.stream().map(Image::getId).toList());
        return created;
    }

    @Override
    @Transactional
    public void deleteAll(final List<Long> ids) {
        LOGGER.info("Deleting images with ids={}", ids);
        imageDao.deleteAll(ids);
    }
}
