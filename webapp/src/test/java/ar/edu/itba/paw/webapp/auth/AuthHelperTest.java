package ar.edu.itba.paw.webapp.auth;

import ar.edu.itba.paw.model.User;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;

import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AuthHelperTest {

    private static final String USERNAME = "jdoe";

    @Mock
    private UserDetailsService userDetailsService;

    @InjectMocks
    private AuthHelper authHelper;

    @Before
    public void setUp() {
        SecurityContextHolder.clearContext();
        final User user = User.builder().id(1L).username(USERNAME).password("hash").build();
        when(userDetailsService.loadUserByUsername(USERNAME))
                .thenReturn(new AuthUserDetails(user, List.of(new SimpleGrantedAuthority("ROLE_USER"))));
    }

    @After
    public void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    public void testLoginRotatesExistingSessionId() {
        // Arrange
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final String sessionIdBeforeLogin = request.getSession(true).getId();

        // Act
        authHelper.login(request, USERNAME);

        // Assert
        Assert.assertNotEquals(sessionIdBeforeLogin, request.getSession(false).getId());
    }

    @Test
    public void testLoginAuthenticatesUser() {
        // Arrange
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.getSession(true);

        // Act
        authHelper.login(request, USERNAME);

        // Assert
        final Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Assert.assertNotNull(auth);
        Assert.assertTrue(auth.isAuthenticated());
        Assert.assertEquals(USERNAME, ((AuthUserDetails) auth.getPrincipal()).getDomainUser().getUsername());
    }

    @Test
    public void testLoginWithoutSessionStillAuthenticates() {
        // Arrange
        final MockHttpServletRequest request = new MockHttpServletRequest();

        // Act
        authHelper.login(request, USERNAME);

        // Assert
        Assert.assertNotNull(SecurityContextHolder.getContext().getAuthentication());
    }
}
