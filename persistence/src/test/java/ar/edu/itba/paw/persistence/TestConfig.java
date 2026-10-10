package ar.edu.itba.paw.persistence;

import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.UUID;
import javax.sql.DataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;
import org.springframework.jdbc.datasource.init.DataSourceInitializer;
import org.springframework.jdbc.datasource.init.DatabasePopulator;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@ComponentScan({ "ar.edu.itba.paw.persistence" })              
@EnableTransactionManagement     
public class TestConfig {

    private static final String MIGRATIONS_PATTERN = "classpath*:db/migration-hsqldb/*.sql";

	@Bean
	public DataSource dataSource() {
		final SimpleDriverDataSource dataSource = new SimpleDriverDataSource();
		dataSource.setDriverClass(org.hsqldb.jdbc.JDBCDriver.class);
		dataSource.setUrl("jdbc:hsqldb:mem:paw_" + UUID.randomUUID() + ";sql.syntax_pgs=true");                                  
		dataSource.setUsername("sa");                                                             
		dataSource.setPassword("");
		return dataSource;
	}

    @Bean
    public org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource dataSource) {
        final org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean factoryBean = new org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean();
        factoryBean.setPackagesToScan("ar.edu.itba.paw.model");
        factoryBean.setDataSource(dataSource);
        factoryBean.setJpaVendorAdapter(new org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter());
        
        final java.util.Properties properties = new java.util.Properties();
        properties.setProperty("hibernate.hbm2ddl.auto", "none");
        properties.setProperty("hibernate.dialect", "org.hibernate.dialect.HSQLDialect");
        properties.setProperty("hibernate.show_sql", "true");
        properties.setProperty("format_sql", "true");
        factoryBean.setJpaProperties(properties);
        
        return factoryBean;
    }

    @Bean
    public PlatformTransactionManager transactionManager(final javax.persistence.EntityManagerFactory emf) {
        return new org.springframework.orm.jpa.JpaTransactionManager(emf);
    }

    @Bean
    public DataSourceInitializer dataSourceInitializer(final DataSource dataSource) {
        final DataSourceInitializer initializer = new DataSourceInitializer();
        initializer.setDataSource(dataSource);
        initializer.setDatabasePopulator(databasePopulator());
        return initializer;
    }

    private DatabasePopulator databasePopulator() {
        final ResourceDatabasePopulator populator = new ResourceDatabasePopulator();

        for (final Resource script : loadMigrationsInOrder()) {
            populator.addScript(script);
        }

        populator.addScript(new ClassPathResource("initial-data.sql"));
        return populator;
    }

    private static Resource[] loadMigrationsInOrder() {
        final Resource[] scripts;
        try {
            scripts = new PathMatchingResourcePatternResolver().getResources(MIGRATIONS_PATTERN);
        } catch (final IOException e) {
            throw new IllegalStateException("Could not load test migration scripts", e);
        }
        if (scripts.length == 0) {
            throw new IllegalStateException("No test migrations found at " + MIGRATIONS_PATTERN);
        }
        Arrays.sort(scripts, Comparator
            .comparingInt(TestConfig::migrationVersion)
            .thenComparing(r -> String.valueOf(r.getFilename())));
        return scripts;
    }

    private static int migrationVersion(final Resource resource) {
        final String filename = String.valueOf(resource.getFilename());
        final int separator = filename.indexOf("__");
        if (!filename.startsWith("V") || separator <= 1) {
            return Integer.MAX_VALUE;
        }
        try {
            return Integer.parseInt(filename.substring(1, separator));
        } catch (final NumberFormatException e) {
            return Integer.MAX_VALUE;
        }
    }
}