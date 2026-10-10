package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.model.Category;
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
public class CategoryJpaDaoTest {

    private static final long CATEGORY_ID = 1L;
    private static final String CATEGORY_NAME = "Electronics";
    private static final long NON_EXISTING_ID = 9000L;
    private static final long MOBILE_CATEGORY_ID = 2L;
    private static final String MOBILE_CATEGORY_NAME = "Mobile Devices";
    private static final String NEW_CATEGORY_NAME = "Tablets";

    @Autowired
    private CategoryDao categoryDao;

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
        final Optional<Category> maybeCategory = categoryDao.getById(CATEGORY_ID);

        // Assert
        Assert.assertTrue(maybeCategory.isPresent());
        Assert.assertEquals(CATEGORY_ID, (long) maybeCategory.get().getId());
        Assert.assertEquals(CATEGORY_NAME, maybeCategory.get().getName());
    }

    @Test
    public void testGetByIdDoesNotExist() {
        // Act
        final Optional<Category> maybeCategory = categoryDao.getById(NON_EXISTING_ID);

        // Assert
        Assert.assertFalse(maybeCategory.isPresent());
    }

    @Test
    public void testGetByIdInvalidId() {
        // Act
        final Optional<Category> maybeCategory = categoryDao.getById(-1L);

        // Assert
        Assert.assertFalse(maybeCategory.isPresent());
    }

    @Test
    public void testGetByNameExists() {
        // Act
        final Optional<Category> maybeCategory = categoryDao.getByName(CATEGORY_NAME);

        // Assert
        Assert.assertTrue(maybeCategory.isPresent());
        Assert.assertEquals(CATEGORY_ID, (long) maybeCategory.get().getId());
    }

    @Test
    public void testGetByNameDoesNotExist() {
        // Act
        final Optional<Category> maybeCategory = categoryDao.getByName("Nonexistent Category");

        // Assert
        Assert.assertFalse(maybeCategory.isPresent());
    }

    @Test
    public void testGetAll() {
        // Act
        final List<Category> categories = categoryDao.getAll();

        // Assert
        Assert.assertEquals(2, categories.size());
    }

    @Test
    public void testCreate() {
        // Act
        final Category created = categoryDao.create("Tablets");

        // Assert
        Assert.assertNotNull(created.getId());
        Assert.assertEquals("Tablets", created.getName());

        final int count = JdbcTestUtils.countRowsInTableWhere(
            jdbcTemplate,
            "categories",
            "name = 'Tablets'"
        );
        Assert.assertEquals(1, count);
    }

    @Test
    public void testGetByIdReturnsTheRequestedCategory() {
        // Act
        final Category category = categoryDao.getById(MOBILE_CATEGORY_ID).orElseThrow();

        // Assert
        Assert.assertEquals(MOBILE_CATEGORY_ID, (long) category.getId());
        Assert.assertEquals(MOBILE_CATEGORY_NAME, category.getName());
    }

    @Test
    public void testGetByNameIsCaseSensitive() {
        // Act
        final Optional<Category> maybeCategory = categoryDao.getByName(CATEGORY_NAME.toLowerCase());

        // Assert
        Assert.assertFalse(maybeCategory.isPresent());
    }

    @Test
    public void testGetAllReturnsEveryCategory() {
        // Act
        final List<Category> categories = categoryDao.getAll();

        // Assert (GET_ALL has no ORDER BY, so the order is not part of the contract)
        final List<Long> ids = categories.stream().map(Category::getId).sorted().collect(Collectors.toList());
        final List<String> names = categories.stream().map(Category::getName).sorted().collect(Collectors.toList());
        Assert.assertEquals(List.of(CATEGORY_ID, MOBILE_CATEGORY_ID), ids);
        Assert.assertEquals(List.of(CATEGORY_NAME, MOBILE_CATEGORY_NAME), names);
    }

    @Test
    public void testGetAllIncludesCreatedCategory() {
        // Arrange
        final Category created = categoryDao.create(NEW_CATEGORY_NAME);

        // Act
        final List<Category> categories = categoryDao.getAll();

        // Assert
        Assert.assertEquals(3, categories.size());
        Assert.assertTrue(categories.contains(created));
    }

    @Test
    public void testCreatedCategoryCanBeFoundById() {
        // Arrange
        final Category created = categoryDao.create(NEW_CATEGORY_NAME);

        // Act
        final Optional<Category> maybeCategory = categoryDao.getById(created.getId());

        // Assert
        Assert.assertTrue(maybeCategory.isPresent());
        Assert.assertEquals(NEW_CATEGORY_NAME, maybeCategory.get().getName());
    }

    @Test
    public void testCreateAssignsDifferentIds() {
        // Act
        final Category first = categoryDao.create(NEW_CATEGORY_NAME);
        final Category second = categoryDao.create("Smartwatches");

        // Assert
        Assert.assertNotEquals(first.getId(), second.getId());
        Assert.assertEquals(4, JdbcTestUtils.countRowsInTable(jdbcTemplate, "categories"));
    }

    @Test(expected = javax.persistence.PersistenceException.class)
    public void testCreateDuplicateNameFails() {
        // Act (categories.name is UNIQUE)
        categoryDao.create(CATEGORY_NAME);
    }
}