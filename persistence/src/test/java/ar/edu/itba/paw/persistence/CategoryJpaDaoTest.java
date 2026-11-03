package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.model.Category;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { TestConfig.class })
@Transactional
@Rollback
public class CategoryJpaDaoTest {

    private static final long CATEGORY_ID = 1L;
    private static final String CATEGORY_NAME = "Electronics";
    private static final long MOBILE_CATEGORY_ID = 2L;
    private static final String MOBILE_CATEGORY_NAME = "Mobile Devices";

    @Autowired
    private CategoryDao categoryDao;

    @Test
    public void testGetAll() {
        // Act
        final List<Category> categories = categoryDao.getAll();

        // Assert
        Assert.assertEquals(2, categories.size());
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
}
