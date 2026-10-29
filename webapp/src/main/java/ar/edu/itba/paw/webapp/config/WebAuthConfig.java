package ar.edu.itba.paw.webapp.config;

import ar.edu.itba.paw.model.Role;
import ar.edu.itba.paw.webapp.auth.AuthUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@EnableWebSecurity
@ComponentScan("ar.edu.itba.paw.webapp.auth")
public class WebAuthConfig extends WebSecurityConfigurerAdapter {

    @Autowired  // No other way to break the circular dependency of the PasswordEncoder
    private AuthUserDetailsService userDetailsService;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Override
    public void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder());
    }

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Value("classpath:rememberMe.key")
    private Resource rememberMeKey;

    private String readRememberMeKey() {
        try {
            return new String (rememberMeKey.getInputStream().readAllBytes());
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /* --------------------------------------------------------------- */
    /* ACCESS CONTROL LIST */
    /* --------------------------------------------------------------- */

    @Override
    protected void configure(final HttpSecurity http) throws Exception {
        http.userDetailsService(userDetailsService).sessionManagement()

            /* Invalid Sessions */
            .invalidSessionUrl("/")

            /* Access Control */
            .and().authorizeRequests()

                /* Administrator Back-office */
                .antMatchers("/admin")                              // Administrator panel root
                    .hasRole(Role.ADMIN.getRoleName())
                .antMatchers("/admin/**")                           // Administrator panel subpages
                    .hasRole(Role.ADMIN.getRoleName())

                /* Login, Register, Logout and Password Recovery */
                .antMatchers("/login").anonymous()                  // User login page
                .antMatchers("/register").anonymous()               // User registration page
                .antMatchers("/logout").authenticated()             // User logout endpoint (Spring-managed)
                .antMatchers("/recovery/**").permitAll()            // Password recovery page
                .antMatchers("/verify/**").permitAll()              // Email verification page

                /* Profiles and User Accounts */
                .antMatchers("/profile").authenticated()            // Current user's profile
                .antMatchers("/profile/edit").authenticated()       // Profile editing
                .antMatchers("/profile/**").permitAll()             // Profiles of users other than the current user
                .antMatchers("/account/**").authenticated()         // Current user account details and settings

                /* Language Selector */
                .antMatchers("/language/**").permitAll()            // Language selector in the navbar

                /* Sell Page (create new listing) */
                .antMatchers("/listing/new/**").authenticated()     // Listing creation

                /* Listings */
                .antMatchers("/listing/**").permitAll()             // Listings

                /* Images */
                .antMatchers("/image/**").permitAll()               // Images

                /* Landing */
                .antMatchers("/").permitAll()                       // Landing page

                /* Miscellaneous */
                .antMatchers("/appinfo").permitAll()                // Deploy info

                /* Default */
                .anyRequest().authenticated()                         // Non-listed endpoints require authentication

            /* Login */
            .and().formLogin()
                .loginPage("/login")
                .usernameParameter("usernameOrEmail")
                .passwordParameter("password")
                .defaultSuccessUrl("/", false)

            /* Logout */
            .and().logout()
                .logoutUrl("/logout")
                .logoutSuccessUrl("/")

            /* Remember Me */
            .and().rememberMe()
                .rememberMeParameter("rememberMe")
                .tokenValiditySeconds((int) TimeUnit.DAYS.toSeconds(30))
                .key(readRememberMeKey())
                .userDetailsService(userDetailsService)

            /* Exceptions */
            .and().exceptionHandling()
                .accessDeniedPage("/403")

            /* Miscellaneous */
            .and().csrf().disable();

    }

    /* --------------------------------------------------------------- */
    /* STATIC RESOURCES */
    /* --------------------------------------------------------------- */

    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring().antMatchers(
                "/css/**",
                "/js/**",
                "/static-image/**",
                "/favicon.ico"
        );
    }
}
