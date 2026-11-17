package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.model.Role;
import ar.edu.itba.paw.model.User;
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

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.sql.DataSource;
import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { TestConfig.class })
@Sql("classpath:sql/user-data.sql")
@Transactional
@Rollback
public class UserRoleJpaDaoTest {

    private static final long VERIFIED_USER_ID = 2L;
    private static final long PLAIN_USER_ID = 3L;

    @Autowired
    private UserRoleDao userRoleDao;

    @Autowired
    private DataSource dataSource;

    private JdbcTemplate jdbcTemplate;

    @PersistenceContext
    private EntityManager em;

    @Before
    public void setUp() {
        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    private static User buildUser(final long id) {
        return User.builder().id(id).build();
    }

    @Test
    public void testAddRolePersistsRole() {
        userRoleDao.addRole(buildUser(VERIFIED_USER_ID), Role.ADMIN);

        em.flush();
        final int count = JdbcTestUtils.countRowsInTableWhere(jdbcTemplate, "users_roles",
            "user_id = " + VERIFIED_USER_ID + " AND role_name = 'ADMIN'");
        Assert.assertEquals(1, count);
    }

    @Test
    public void testGetRolesReturnsStoredRoles() {
        jdbcTemplate.update("INSERT INTO users_roles (user_id, role_name) VALUES (?, ?)", PLAIN_USER_ID, "USER");
        jdbcTemplate.update("INSERT INTO users_roles (user_id, role_name) VALUES (?, ?)", PLAIN_USER_ID, "ADMIN");

        final List<Role> roles = userRoleDao.getRoles(buildUser(PLAIN_USER_ID));

        Assert.assertEquals(2, roles.size());
        Assert.assertTrue(roles.contains(Role.USER));
        Assert.assertTrue(roles.contains(Role.ADMIN));
    }
}
