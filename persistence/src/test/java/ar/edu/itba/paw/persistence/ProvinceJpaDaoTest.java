package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.model.Province;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.util.List;
import java.util.Optional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { TestConfig.class })
@Transactional
@Rollback
public class ProvinceJpaDaoTest {

    private static final long NON_EXISTING_ID = 9000L;

    @Autowired
    private ProvinceDao provinceDao;

    @Autowired
    private DataSource dataSource;

    private JdbcTemplate jdbcTemplate;

    @Before
    public void setUp() {
        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Test
    public void testGetByIdExistingReturnsProvince() {
        final Long cabaId = jdbcTemplate.queryForObject(
            "SELECT province_id FROM provinces WHERE name = 'caba'", Long.class);

        final Optional<Province> maybeProvince = provinceDao.getById(cabaId);

        Assert.assertTrue(maybeProvince.isPresent());
        Assert.assertEquals(cabaId, maybeProvince.get().getId());
        Assert.assertEquals("caba", maybeProvince.get().getName());
    }

    @Test
    public void testGetByIdNonExistingReturnsEmpty() {
        Assert.assertFalse(provinceDao.getById(NON_EXISTING_ID).isPresent());
    }

    @Test
    public void testGetAllReturnsAllProvincesInIdOrder() {
        final List<String> expected = jdbcTemplate.queryForList(
            "SELECT name FROM provinces ORDER BY province_id", String.class);

        final List<String> actual = provinceDao.getAll().stream().map(Province::getName).toList();

        Assert.assertEquals(expected, actual);
    }
}
