package ar.edu.itba.paw.webapp.config;

import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.MessageSource;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import ar.edu.itba.paw.service.UserService;
import ar.edu.itba.paw.webapp.auth.AuthHelper;
import ar.edu.itba.paw.webapp.auth.CurrentUserArgumentResolver;
import ar.edu.itba.paw.webapp.i18n.DatabaseLocaleResolver;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.view.InternalResourceViewResolver;
import org.springframework.web.servlet.view.JstlView;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.springframework.validation.Validator;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

@Configuration
@EnableWebMvc
@EnableTransactionManagement
@EnableScheduling
@ComponentScan({
        "ar.edu.itba.paw.webapp.controller",
        "ar.edu.itba.paw.service",
        "ar.edu.itba.paw.persistence"
})
@PropertySource("classpath:app.properties")
public class WebConfig implements WebMvcConfigurer {

    /* --------------------------------------------------------------- */
    /* ENVIRONMENT (properties management) */
    /* --------------------------------------------------------------- */

    private final Environment env;

    @Autowired
    public WebConfig(Environment env) {
        this.env = env;
    }

    /* --------------------------------------------------------------- */
    /* FRONTEND (resource management and view resolvers) */
    /* --------------------------------------------------------------- */

    @Bean
    public ViewResolver viewResolver() {
        final var viewResolver = new InternalResourceViewResolver();
        viewResolver.setViewClass(JstlView.class);
        viewResolver.setPrefix("/WEB-INF/jsp/");
        viewResolver.setSuffix(".jsp");
        return viewResolver;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/css/**").addResourceLocations("/css/");
        registry.addResourceHandler("/static-image/**").addResourceLocations("/static-image/");
        registry.addResourceHandler("/favicon.ico").addResourceLocations("/static-image/favicon.ico");
    }

    /* --------------------------------------------------------------- */
    /* IMAGE SUPPORT */
    /* --------------------------------------------------------------- */

    @Bean
    public MultipartResolver multipartResolver() {
        return new StandardServletMultipartResolver();
    }

    /* --------------------------------------------------------------- */
    /* I18N (messages, locale resolution and validation) */
    /* --------------------------------------------------------------- */

    @Bean
    public MessageSource messageSource() {
        final var messageSource = new ReloadableResourceBundleMessageSource();
        messageSource.setBasename("classpath:i18n/messages");
        messageSource.setDefaultEncoding(StandardCharsets.UTF_8.name());
        messageSource.setCacheSeconds(5);
        messageSource.setUseCodeAsDefaultMessage(true);
        messageSource.setFallbackToSystemLocale(false);
        return messageSource;
    }

    @Bean
    public LocaleResolver localeResolver(final UserService userService, final AuthHelper authHelper) {
        return new DatabaseLocaleResolver(Locale.ENGLISH, userService, authHelper);
    }

    @Bean
    public LocaleChangeInterceptor localeChangeInterceptor() {
        final var interceptor = new LocaleChangeInterceptor();
        interceptor.setParamName("lang");
        return interceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(localeChangeInterceptor());
    }

    @Bean
    public LocalValidatorFactoryBean validator() {
        final var validator = new LocalValidatorFactoryBean();
        validator.setValidationMessageSource(messageSource());
        return validator;
    }

    @Override
    public Validator getValidator() {
        return validator();
    }

    /* --------------------------------------------------------------- */
    /* DATABASE (DataSource, Flyway, JPA / Hibernate ORM) */
    /* --------------------------------------------------------------- */

    @Bean
    public DataSource dataSource() {
        final var dataSource = new SimpleDriverDataSource();
        dataSource.setDriverClass(org.postgresql.Driver.class);
        dataSource.setUrl(env.getRequiredProperty("database.jdbc.url"));
        dataSource.setUsername(env.getRequiredProperty("database.jdbc.username"));
        dataSource.setPassword(env.getRequiredProperty("database.jdbc.password"));
        return dataSource;
    }

    @Bean(initMethod = "migrate")
    public Flyway flyway(DataSource dataSource) {
        return Flyway.configure()
                .baselineOnMigrate(true)
                .dataSource(dataSource)
                .locations("classpath:/db/migration")
                .load();
    }

    @Bean
    @DependsOn("flyway")
    public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
        final var factoryBean = new LocalContainerEntityManagerFactoryBean();
        factoryBean.setPackagesToScan("ar.edu.itba.paw.model");             // Set Model package to be scanned by JPA
        factoryBean.setDataSource(dataSource());                            // Set JPA DataSource configured previously
        factoryBean.setJpaVendorAdapter(new HibernateJpaVendorAdapter());   // Set Hibernate ORM as JPA vendor
        factoryBean.setJpaProperties(createHibernateProperties());          // Set Hibernate Properties
        return factoryBean;
    }

    private static Properties createHibernateProperties() {
        final Properties properties = new Properties();
        properties.setProperty("hibernate.hbm2ddl.auto", "none");
        properties.setProperty("hibernate.dialect", "org.hibernate.dialect.PostgreSQL92Dialect");

        /* IMPORTANT: REVIEW BEFORE DEPLOY. DO NOT CHANGE THIS COMMENT AS IT IS SEARCHED BY GREP */
        properties.setProperty("hibernate.show_sql", "true");               // Remove this before deploy
        /* IMPORTANT: REVIEW BEFORE DEPLOY. DO NOT CHANGE THIS COMMENT AS IT IS SEARCHED BY GREP */
        properties.setProperty("hibernate.format_sql", "true");             // Remove this before deploy

        return properties;
    }

    /* --------------------------------------------------------------- */
    /* TRANSACTION MANAGEMENT (@Transactional annotations) */
    /* --------------------------------------------------------------- */

    @Bean
    public PlatformTransactionManager transactionManager(final EntityManagerFactory emf) {
        return new JpaTransactionManager(emf);
    }

    /* --------------------------------------------------------------- */
    /* Current User HandlerMethodArgumentResolver (for @CurrentUser) */
    /* --------------------------------------------------------------- */

    @Bean
    public CurrentUserArgumentResolver currentUserArgumentResolver() {
        return new CurrentUserArgumentResolver();
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(currentUserArgumentResolver());
    }
}
