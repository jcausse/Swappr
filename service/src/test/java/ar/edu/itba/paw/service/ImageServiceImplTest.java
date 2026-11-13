package ar.edu.itba.paw.service;

import org.junit.Test;
import java.util.Optional;
import org.junit.Assert;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.when;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.junit.runner.RunWith;

import ar.edu.itba.paw.model.Image;
import ar.edu.itba.paw.persistence.ImageDao;

@RunWith(MockitoJUnitRunner.class)
public class ImageServiceImplTest {

    private static final long IMAGE_ID = 1;
    private static final String IMAGE_FILENAME = "photo.png";
    private static final String IMAGE_ALT = "profile picture";
    private static final String IMAGE_CONTENT_TYPE = "image/png";
    private static final byte[] IMAGE_DATA = new byte[]{1, 2, 3};

    @InjectMocks
    private ImageServiceImpl imageService;

    @Mock
    private ImageDao imageDao;

    private Image buildFakeImage() {
        return Image.builder()
            .id(IMAGE_ID)
            .filename(IMAGE_FILENAME)
            .alt(IMAGE_ALT)
            .contentType(IMAGE_CONTENT_TYPE)
            .data(IMAGE_DATA)
            .build();
    }

    @Test
    public void testGetByIdImageExists() {
        // Arrange
        final Image image = buildFakeImage();
        when(imageDao.getById(eq(IMAGE_ID))).thenReturn(Optional.of(image));

        // Act
        final Optional<Image> maybeImage = imageService.getById(IMAGE_ID);

        // Assert
        Assert.assertTrue(maybeImage.isPresent());
        Assert.assertEquals(IMAGE_ID, (long) maybeImage.get().getId());
        Assert.assertEquals(IMAGE_FILENAME, maybeImage.get().getFilename());
    }

    @Test
    public void testGetByIdImageDoesNotExist() {
        // Arrange
        when(imageDao.getById(eq(IMAGE_ID))).thenReturn(Optional.empty());

        // Act
        final Optional<Image> maybeImage = imageService.getById(IMAGE_ID);

        // Assert
        Assert.assertFalse(maybeImage.isPresent());
    }

    @Test
    public void testCreate() {
        // Arrange
        final Image createdImage = buildFakeImage();
        when(imageDao.create(eq(IMAGE_FILENAME), eq(IMAGE_ALT), eq(IMAGE_CONTENT_TYPE), eq(IMAGE_DATA)))
            .thenReturn(createdImage);

        // Act
        final Image result = imageService.create(IMAGE_FILENAME, IMAGE_ALT, IMAGE_CONTENT_TYPE, IMAGE_DATA);

        // Assert
        Assert.assertSame(createdImage, result);
    }
}
