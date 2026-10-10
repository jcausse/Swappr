package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.model.Subcategory;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.jdbc.JdbcTestUtils;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { TestConfig.class })
@Transactional
@Rollback
public class SubcategoryJpaDaoTest {

    private static final long SUBCATEGORY_ID = 1L;
    private static final String SUBCATEGORY_NAME = "Laptops";
    private static final long CATEGORY_ID = 1L;
    private static final long NON_EXISTING_ID = 9000L;
    private static final String CATEGORY_NAME = "Electronics";
    private static final long PHONES_SUBCATEGORY_ID = 2L;
    private static final String PHONES_SUBCATEGORY_NAME = "Phones";
    private static final long MOBILE_CATEGORY_ID = 2L;
    private static final String MOBILE_CATEGORY_NAME = "Mobile Devices";
    private static final String NEW_SUBCATEGORY_NAME = "Monitors";

    @Autowired
    private SubcategoryDao subcategoryDao;

    @Autowired
    private DataSource dataSource;

    private JdbcTemplate jdbcTemplate;

    @Before
    public void setUp() {
        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Test
    public void testGetByIdExists() {
        // Act
        final Optional<Subcategory> maybeSubcategory = subcategoryDao.getById(SUBCATEGORY_ID);

        // Assert
        Assert.assertTrue(maybeSubcategory.isPresent());
        Assert.assertEquals(SUBCATEGORY_ID, (long) maybeSubcategory.get().getId());
        Assert.assertEquals(SUBCATEGORY_NAME, maybeSubcategory.get().getName());
        Assert.assertEquals(CATEGORY_ID, (long) maybeSubcategory.get().getCategory().getId());
    }

    @Test
    public void testGetByIdDoesNotExist() {
        // Act
        final Optional<Subcategory> maybeSubcategory = subcategoryDao.getById(NON_EXISTING_ID);

        // Assert
        Assert.assertFalse(maybeSubcategory.isPresent());
    }

    @Test
    public void testGetByIdInvalidId() {
        // Act
        final Optional<Subcategory> maybeSubcategory = subcategoryDao.getById(-1L);

        // Assert
        Assert.assertFalse(maybeSubcategory.isPresent());
    }

    @Test
    public void testGetByNameExists() {
        // Act
        final Optional<Subcategory> maybeSubcategory = subcategoryDao.getByName(SUBCATEGORY_NAME);

        // Assert
        Assert.assertTrue(maybeSubcategory.isPresent());
        Assert.assertEquals(SUBCATEGORY_ID, (long) maybeSubcategory.get().getId());
    }

    @Test
    public void testGetByNameDoesNotExist() {
        // Act
        final Optional<Subcategory> maybeSubcategory = subcategoryDao.getByName("Nonexistent Subcategory");

        // Assert
        Assert.assertFalse(maybeSubcategory.isPresent());
    }

    @Test
    public void testGetAll() {
        // Act
        final List<Subcategory> subcategories = subcategoryDao.getAll();

        // Assert
        Assert.assertEquals(2, subcategories.size());
    }

    @Test
    public void testGetByCategoryIdExists() {
        // Act
        final List<Subcategory> subcategories = subcategoryDao.getByCategoryId(CATEGORY_ID);

        // Assert
        Assert.assertEquals(1, subcategories.size());
        Assert.assertEquals(SUBCATEGORY_ID, (long) subcategories.get(0).getId());
    }

    @Test
    public void testGetByCategoryIdDoesNotExist() {
        // Act
        final List<Subcategory> subcategories = subcategoryDao.getByCategoryId(NON_EXISTING_ID);

        // Assert
        Assert.assertTrue(subcategories.isEmpty());
    }

    @Test
    public void testCreate() {
        // Act
        final Subcategory created = subcategoryDao.create("Monitors", CATEGORY_ID);

        // Assert
        Assert.assertNotNull(created.getId());
        Assert.assertEquals("Monitors", created.getName());
        Assert.assertEquals(CATEGORY_ID, (long) created.getCategory().getId());

        final int count = JdbcTestUtils.countRowsInTableWhere(
            jdbcTemplate,
            "subcategories",
            "name = 'Monitors' AND category_id = " + CATEGORY_ID
        );
        Assert.assertEquals(1, count);
    }

    @Test
    public void testGetByIdLoadsCategoryName() {
        // Act
        final Subcategory subcategory = subcategoryDao.getById(SUBCATEGORY_ID).orElseThrow();

        // Assert (both tables have a "name" column: the category must not get the subcategory's one)
        Assert.assertEquals(SUBCATEGORY_NAME, subcategory.getName());
        Assert.assertEquals(CATEGORY_NAME, subcategory.getCategory().getName());
    }

    @Test
    public void testGetByIdReturnsTheRequestedSubcategory() {
        // Act
        final Subcategory subcategory = subcategoryDao.getById(PHONES_SUBCATEGORY_ID).orElseThrow();

        // Assert
        Assert.assertEquals(PHONES_SUBCATEGORY_ID, (long) subcategory.getId());
        Assert.assertEquals(PHONES_SUBCATEGORY_NAME, subcategory.getName());
        Assert.assertEquals(MOBILE_CATEGORY_ID, (long) subcategory.getCategory().getId());
        Assert.assertEquals(MOBILE_CATEGORY_NAME, subcategory.getCategory().getName());
    }

    @Test
    public void testGetByNameIsCaseSensitive() {
        // Act
        final Optional<Subcategory> maybeSubcategory = subcategoryDao.getByName(SUBCATEGORY_NAME.toLowerCase());

        // Assert
        Assert.assertFalse(maybeSubcategory.isPresent());
    }

    @Test
    public void testGetAllReturnsEverySubcategoryWithItsCategory() {
        // Act
        final List<Subcategory> subcategories = subcategoryDao.getAll();

        // Assert 
        final List<Long> ids = subcategories.stream().map(Subcategory::getId).sorted().collect(Collectors.toList());
        Assert.assertEquals(List.of(SUBCATEGORY_ID, PHONES_SUBCATEGORY_ID), ids);
        for (final Subcategory subcategory : subcategories) {
            final long expectedCategoryId = subcategory.getId() == SUBCATEGORY_ID ? CATEGORY_ID : MOBILE_CATEGORY_ID;
            Assert.assertEquals(expectedCategoryId, (long) subcategory.getCategory().getId());
        }
    }

    @Test
    public void testGetByCategoryIdOnlyReturnsThatCategory() {
        // Arrange
        final Subcategory created = subcategoryDao.create(NEW_SUBCATEGORY_NAME, CATEGORY_ID);

        // Act
        final List<Subcategory> computers = subcategoryDao.getByCategoryId(CATEGORY_ID);
        final List<Subcategory> mobile = subcategoryDao.getByCategoryId(MOBILE_CATEGORY_ID);

        // Assert
        final List<Long> computerIds = computers.stream().map(Subcategory::getId).sorted().collect(Collectors.toList());
        Assert.assertEquals(List.of(SUBCATEGORY_ID, created.getId()), computerIds);
        Assert.assertEquals(1, mobile.size());
        Assert.assertEquals(PHONES_SUBCATEGORY_ID, (long) mobile.get(0).getId());
    }

    @Test
    public void testCreateLoadsCategory() {
        // Act
        final Subcategory created = subcategoryDao.create(NEW_SUBCATEGORY_NAME, CATEGORY_ID);

        // Assert
        Assert.assertEquals(CATEGORY_NAME, created.getCategory().getName());
    }

    @Test
    public void testCreatedSubcategoryCanBeFoundById() {
        // Arrange
        final Subcategory created = subcategoryDao.create(NEW_SUBCATEGORY_NAME, CATEGORY_ID);

        // Act
        final Optional<Subcategory> maybeSubcategory = subcategoryDao.getById(created.getId());

        // Assert
        Assert.assertTrue(maybeSubcategory.isPresent());
        Assert.assertEquals(NEW_SUBCATEGORY_NAME, maybeSubcategory.get().getName());
    }

    @Test
    public void testCreateSameNameInAnotherCategoryIsAllowed() {
        // Act
        final Subcategory created = subcategoryDao.create(SUBCATEGORY_NAME, MOBILE_CATEGORY_ID);

        // Assert
        Assert.assertEquals(MOBILE_CATEGORY_ID, (long) created.getCategory().getId());
        Assert.assertEquals(2, JdbcTestUtils.countRowsInTableWhere(
            jdbcTemplate, "subcategories", "name = '" + SUBCATEGORY_NAME + "'"));
    }

    @Test(expected = javax.persistence.PersistenceException.class)
    public void testCreateDuplicateNameInSameCategoryFails() {
        // Act
        subcategoryDao.create(SUBCATEGORY_NAME, CATEGORY_ID);
    }

    @Test(expected = javax.persistence.PersistenceException.class)
    public void testCreateWithNonExistingCategoryFails() {
        // Act 
        subcategoryDao.create(NEW_SUBCATEGORY_NAME, NON_EXISTING_ID);
    }
}