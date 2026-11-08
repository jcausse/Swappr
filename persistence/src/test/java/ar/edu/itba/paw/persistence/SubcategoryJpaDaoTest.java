package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.model.Subcategory;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

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

    @Autowired
    private SubcategoryDao subcategoryDao;

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
    public void testGetAllLoadsCategoriesAndIsSorted() {
        final List<Subcategory> subcategories = subcategoryDao.getAll();

        Assert.assertEquals(2, subcategories.size());
        Assert.assertEquals(List.of("Laptops", "Phones"),
                subcategories.stream().map(Subcategory::getName).toList());
        Assert.assertEquals(List.of("Electronics", "Mobile Devices"),
                subcategories.stream().map(s -> s.getCategory().getName()).toList());
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
    public void testGetByCategoryIdOnlyReturnsThatCategory() {
        // Act
        final List<Subcategory> computers = subcategoryDao.getByCategoryId(CATEGORY_ID);
        final List<Subcategory> mobile = subcategoryDao.getByCategoryId(MOBILE_CATEGORY_ID);

        // Assert
        final List<Long> computerIds = computers.stream().map(Subcategory::getId).sorted().collect(Collectors.toList());
        Assert.assertEquals(List.of(SUBCATEGORY_ID), computerIds);
        Assert.assertEquals(1, mobile.size());
        Assert.assertEquals(PHONES_SUBCATEGORY_ID, (long) mobile.get(0).getId());
    }
}
