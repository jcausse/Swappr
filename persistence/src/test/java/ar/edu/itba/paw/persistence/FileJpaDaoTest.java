package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.model.File;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.jdbc.Sql;
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
public class FileJpaDaoTest {

    private static final String TABLE = "files";

    private static final long FILE_ID = 1L;
    private static final long NON_EXISTING_ID = 9000L;
    private static final String FILENAME = "receipt.pdf";
    private static final String ALT = "Payment receipt";
    private static final String CONTENT_TYPE = "application/pdf";
    private static final byte[] DATA = { 0x00, 0x01, 0x7F, (byte) 0x80, (byte) 0xFF };
    private static final long PROOF_OFFER_ID = 5L;

    @Autowired
    private FileDao fileDao;

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

    private void insertFile(final long id, final String contentType) {
        jdbcTemplate.update(
            "INSERT INTO files (file_id, filename, alt, content_type, data) VALUES (?, ?, ?, ?, ?)",
            id, FILENAME, ALT, contentType, DATA
        );
    }

    /* ---------------------------------------------------------------------------------------------- */
    /* getById                                                                                         */
    /* ---------------------------------------------------------------------------------------------- */

    @Test
    public void testGetByIdExists() {
        // Arrange
        insertFile(FILE_ID, CONTENT_TYPE);

        // Act
        final Optional<File> maybeFile = fileDao.getById(FILE_ID);

        // Assert
        Assert.assertTrue(maybeFile.isPresent());
        final File file = maybeFile.get();
        Assert.assertEquals(FILE_ID, (long) file.getId());
        Assert.assertEquals(FILENAME, file.getFilename());
        Assert.assertEquals(ALT, file.getAlt());
        Assert.assertEquals(Optional.of(CONTENT_TYPE), file.getContentType());
        Assert.assertArrayEquals(DATA, file.getData());
    }

    @Test
    public void testGetByIdDoesNotExist() {
        // Act
        final Optional<File> maybeFile = fileDao.getById(NON_EXISTING_ID);

        // Assert
        Assert.assertFalse(maybeFile.isPresent());
    }

    @Test
    public void testGetByIdWithoutContentType() {
        // Arrange
        insertFile(FILE_ID, null);

        // Act
        final File file = fileDao.getById(FILE_ID).orElseThrow();

        // Assert
        Assert.assertEquals(Optional.empty(), file.getContentType());
    }

    /* ---------------------------------------------------------------------------------------------- */
    /* create                                                                                          */
    /* ---------------------------------------------------------------------------------------------- */

    @Test
    public void testCreateReturnsCreatedFile() {
        // Act
        final File file = fileDao.create(FILENAME, ALT, CONTENT_TYPE, DATA);

        // Assert
        Assert.assertNotNull(file.getId());
        Assert.assertEquals(FILENAME, file.getFilename());
        Assert.assertEquals(ALT, file.getAlt());
        Assert.assertEquals(Optional.of(CONTENT_TYPE), file.getContentType());
        Assert.assertArrayEquals(DATA, file.getData());
    }

    @Test
    public void testCreatePersistsFile() {
        // Act
        final File file = fileDao.create(FILENAME, ALT, CONTENT_TYPE, DATA);

        // Assert
        em.flush();
        Assert.assertEquals(1, JdbcTestUtils.countRowsInTable(jdbcTemplate, TABLE));
        Assert.assertEquals(1, JdbcTestUtils.countRowsInTableWhere(jdbcTemplate, TABLE,
            "file_id = " + file.getId()
                + " AND filename = '" + FILENAME + "'"
                + " AND alt = '" + ALT + "'"
                + " AND content_type = '" + CONTENT_TYPE + "'"));
    }

    @Test
    public void testCreateStoresBinaryDataUnchanged() {
        // Act
        final File created = fileDao.create(FILENAME, ALT, CONTENT_TYPE, DATA);

        // Assert
        final byte[] stored = jdbcTemplate.queryForObject(
            "SELECT data FROM files WHERE file_id = ?", byte[].class, created.getId());
        Assert.assertArrayEquals(DATA, stored);
    }

    @Test
    public void testCreateWithoutContentType() {
        // Act
        final File file = fileDao.create(FILENAME, ALT, null, DATA);

        // Assert
        Assert.assertEquals(Optional.empty(), file.getContentType());
        Assert.assertEquals(1, JdbcTestUtils.countRowsInTableWhere(jdbcTemplate, TABLE,
            "file_id = " + file.getId() + " AND content_type IS NULL"));
    }

    @Test(expected = javax.persistence.PersistenceException.class)
    public void testCreateWithoutDataFails() {
        fileDao.create(FILENAME, ALT, CONTENT_TYPE, null);
    }

    /* ---------------------------------------------------------------------------------------------- */
    /* delete                                                                                          */
    /* ---------------------------------------------------------------------------------------------- */

    @Test
    public void testDeleteExisting() {
        // Arrange
        insertFile(FILE_ID, CONTENT_TYPE);

        // Act
        fileDao.delete(FILE_ID);

        // Assert
        em.flush();
        Assert.assertEquals(0, JdbcTestUtils.countRowsInTableWhere(jdbcTemplate, TABLE, "file_id = " + FILE_ID));
    }

    @Test
    public void testDeleteOnlyRemovesTheGivenFile() {
        // Arrange
        insertFile(FILE_ID, CONTENT_TYPE);
        insertFile(FILE_ID + 1, CONTENT_TYPE);

        // Act
        fileDao.delete(FILE_ID);

        // Assert
        em.flush();
        Assert.assertEquals(1, JdbcTestUtils.countRowsInTable(jdbcTemplate, TABLE));
        Assert.assertEquals(1, JdbcTestUtils.countRowsInTableWhere(jdbcTemplate, TABLE, "file_id = " + (FILE_ID + 1)));
    }

    @Test
    public void testDeleteNonExistingDoesNothing() {
        // Arrange
        insertFile(FILE_ID, CONTENT_TYPE);

        // Act
        fileDao.delete(NON_EXISTING_ID);

        // Assert
        em.flush();
        Assert.assertEquals(1, JdbcTestUtils.countRowsInTable(jdbcTemplate, TABLE));
    }

    @Test
    @Sql({ "classpath:sql/listing-data.sql", "classpath:sql/offer-data.sql" })
    public void testDeleteProofOfPaymentClearsOfferReference() {
        // Act
        fileDao.delete(FILE_ID);

        // Assert
        em.flush();
        Assert.assertEquals(1, JdbcTestUtils.countRowsInTableWhere(jdbcTemplate, "offers",
            "offer_id = " + PROOF_OFFER_ID + " AND proof_of_payment_id IS NULL"));
    }
}