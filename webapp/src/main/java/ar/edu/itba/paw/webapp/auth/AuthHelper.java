package ar.edu.itba.paw.webapp.auth;

import ar.edu.itba.paw.model.User;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class AuthHelper {

    private final UserDetailsService userDetailsService;

    public Optional<User> getCurrentUser() {
        return getCurrentUser(SecurityContextHolder.getContext().getAuthentication());
    }

    public Optional<User> getCurrentUser(final Authentication auth) {
        if (auth != null && auth.getPrincipal() instanceof AuthUserDetails authUserDetails) {
            return Optional.of(authUserDetails.getDomainUser());
        }
        return Optional.empty();
    }

    public boolean isAuthenticated() {
        return getCurrentUser().isPresent();
    }

    public void login(final @NonNull HttpServletRequest request, final @NonNull String usernameOrEmail) {
        final UserDetails ud = userDetailsService.loadUserByUsername(usernameOrEmail);
        if (request.getSession(false) != null) {
            request.changeSessionId();
        }
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(ud, null, ud.getAuthorities())
        );
    }

    public void update(final @NonNull User updatedUser) {
        final Authentication currentAuth = SecurityContextHolder.getContext().getAuthentication();
        if (currentAuth != null && currentAuth.getPrincipal() instanceof AuthUserDetails oldDetails) {
            AuthUserDetails newDetails = new AuthUserDetails(updatedUser, oldDetails.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                    newDetails,
                    currentAuth.getCredentials(),
                    newDetails.getAuthorities()
            ));
        }
    }
}
