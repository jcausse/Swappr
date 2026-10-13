package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.model.Image;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.jdbc.JdbcTestUtils;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Optional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { TestConfig.class })
@Transactional
@Rollback
public class ImageJpaDaoTest {

    private static final String TABLE = "images";

    private static final long IMAGE_ID = 100L;
    private static final long NON_EXISTING_ID = 9000L;
    private static final String FILENAME = "photo.png";
    private static final String ALT = "A nice photo";
    private static final String CONTENT_TYPE = "image/png";
    private static final byte[] DATA = { 0x00, 0x01, 0x7F, (byte) 0x80, (byte) 0xFF };

    @Autowired
    private ImageDao imageDao;

    @Autowired
    private DataSource dataSource;

    private JdbcTemplate jdbcTemplate;
    @PersistenceContext
    private EntityManager em;

    @Before
    public void setUp() {
        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    /* ---------------------------------------------------------------------------------------------- */
    /* Fixtures & helpers                                                                              */
    /* ---------------------------------------------------------------------------------------------- */

    private void insertImage(final long id, final String contentType) {
        jdbcTemplate.update(
            "INSERT INTO images (image_id, filename, alt, content_type, data) VALUES (?, ?, ?, ?, ?)",
            id, FILENAME, ALT, contentType, DATA
        );
    }

    /* ---------------------------------------------------------------------------------------------- */
    /* getById                                                                                         */
    /* ---------------------------------------------------------------------------------------------- */

    @Test
    public void testGetByIdExists() {
        // Arrange
        insertImage(IMAGE_ID, CONTENT_TYPE);

        // Act
        final Optional<Image> maybeImage = imageDao.getById(IMAGE_ID);

        // Assert
        Assert.assertTrue(maybeImage.isPresent());
        final Image image = maybeImage.get();
        Assert.assertEquals(IMAGE_ID, (long) image.getId());
        Assert.assertEquals(FILENAME, image.getFilename());
        Assert.assertEquals(ALT, image.getAlt());
        Assert.assertEquals(Optional.of(CONTENT_TYPE), image.getContentType());
        Assert.assertArrayEquals(DATA, image.getData());
    }

    @Test
    public void testGetByIdDoesNotExist() {
        // Act
        final Optional<Image> maybeImage = imageDao.getById(NON_EXISTING_ID);

        // Assert
        Assert.assertFalse(maybeImage.isPresent());
    }

    @Test
    public void testGetByIdWithoutContentType() {
        // Arrange
        insertImage(IMAGE_ID, null);

        // Act
        final Image image = imageDao.getById(IMAGE_ID).orElseThrow();

        // Assert
        Assert.assertEquals(Optional.empty(), image.getContentType());
    }

    /* ---------------------------------------------------------------------------------------------- */
    /* create                                                                                          */
    /* ---------------------------------------------------------------------------------------------- */

    @Test
    public void testCreateReturnsCreatedImage() {
        // Act
        final Image image = imageDao.create(FILENAME, ALT, CONTENT_TYPE, DATA);

        // Assert
        Assert.assertNotNull(image.getId());
        Assert.assertEquals(FILENAME, image.getFilename());
        Assert.assertEquals(ALT, image.getAlt());
        Assert.assertEquals(Optional.of(CONTENT_TYPE), image.getContentType());
        Assert.assertArrayEquals(DATA, image.getData());
    }

    @Test
    public void testCreatePersistsImage() {
        // Act
        final Image image = imageDao.create(FILENAME, ALT, CONTENT_TYPE, DATA);

        // Assert
        Assert.assertEquals(1, JdbcTestUtils.countRowsInTableWhere(jdbcTemplate, TABLE,
            "image_id = " + image.getId()
                + " AND filename = '" + FILENAME + "'"
                + " AND alt = '" + ALT + "'"
                + " AND content_type = '" + CONTENT_TYPE + "'"));
    }

    @Test
    public void testCreateStoresBinaryDataUnchanged() {
        // Act
        final Image created = imageDao.create(FILENAME, ALT, CONTENT_TYPE, DATA);

        // Assert
        final byte[] stored = jdbcTemplate.queryForObject(
            "SELECT data FROM images WHERE image_id = ?", byte[].class, created.getId());
        Assert.assertArrayEquals(DATA, stored);
    }

    @Test
    public void testCreateWithoutContentType() {
        // Act
        final Image image = imageDao.create(FILENAME, ALT, null, DATA);

        // Assert
        Assert.assertEquals(Optional.empty(), image.getContentType());
        Assert.assertEquals(1, JdbcTestUtils.countRowsInTableWhere(jdbcTemplate, TABLE,
            "image_id = " + image.getId() + " AND content_type IS NULL"));
    }

    @Test(expected = javax.persistence.PersistenceException.class)
    public void testCreateWithoutDataFails() {
        imageDao.create(FILENAME, ALT, CONTENT_TYPE, null);
    }

    /* ---------------------------------------------------------------------------------------------- */
    /* delete                                                                                          */
    /* ---------------------------------------------------------------------------------------------- */

    @Test
    public void testDeleteExisting() {
        // Arrange
        insertImage(IMAGE_ID, CONTENT_TYPE);

        // Act
        imageDao.delete(IMAGE_ID);

        // Assert
        em.flush();
        Assert.assertEquals(0, JdbcTestUtils.countRowsInTableWhere(jdbcTemplate, TABLE, "image_id = " + IMAGE_ID));
    }

    @Test
    public void testDeleteOnlyRemovesTheGivenImage() {
        // Arrange
        insertImage(IMAGE_ID, CONTENT_TYPE);
        insertImage(IMAGE_ID + 1, CONTENT_TYPE);

        // Act
        imageDao.delete(IMAGE_ID);

        // Assert
        em.flush();
        Assert.assertEquals(1, JdbcTestUtils.countRowsInTableWhere(jdbcTemplate, TABLE, "image_id = " + (IMAGE_ID + 1)));
        Assert.assertEquals(0, JdbcTestUtils.countRowsInTableWhere(jdbcTemplate, TABLE, "image_id = " + IMAGE_ID));
    }

    @Test
    public void testDeleteNonExistingDoesNothing() {
        // Arrange
        insertImage(IMAGE_ID, CONTENT_TYPE);

        // Act
        imageDao.delete(NON_EXISTING_ID);

        // Assert
        em.flush();
        Assert.assertEquals(1, JdbcTestUtils.countRowsInTableWhere(jdbcTemplate, TABLE, "image_id = " + IMAGE_ID));
    }
}
