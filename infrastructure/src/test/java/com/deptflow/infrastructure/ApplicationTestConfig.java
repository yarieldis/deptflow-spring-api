package com.deptflow.infrastructure;

import com.deptflow.application.ports.CurrentUserProvider;
import com.deptflow.application.ports.StorageService;
import com.deptflow.infrastructure.persistence.JpaPersistenceConfig;
import com.deptflow.infrastructure.security.ThreadLocalCurrentUserProvider;
import com.deptflow.infrastructure.storage.FileSystemStorageService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.util.Properties;

/**
 * Full application test context: H2 datasource, orm.xml-driven JPA, Spring Data
 * repositories, application use cases, filesystem storage, and thread-local
 * tenant/user providers.
 */
@Configuration
@EnableTransactionManagement
@Import(JpaPersistenceConfig.class)
@ComponentScan(basePackages = "com.deptflow.application")
public class ApplicationTestConfig {

    @Bean
    DataSource dataSource() {
        DriverManagerDataSource ds = new DriverManagerDataSource();
        ds.setDriverClassName("org.h2.Driver");
        ds.setUrl("jdbc:h2:mem:deptflow-app;DB_CLOSE_DELAY=-1");
        ds.setUsername("sa");
        ds.setPassword("");
        return ds;
    }

    @Bean
    LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource dataSource) {
        LocalContainerEntityManagerFactoryBean emf = new LocalContainerEntityManagerFactoryBean();
        emf.setDataSource(dataSource);
        emf.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
        emf.setMappingResources("META-INF/orm.xml");

        Properties props = new Properties();
        props.setProperty("hibernate.hbm2ddl.auto", "create-drop");
        props.setProperty("hibernate.show_sql", "false");
        emf.setJpaProperties(props);
        return emf;
    }

    @Bean
    PlatformTransactionManager transactionManager(jakarta.persistence.EntityManagerFactory emf) {
        return new JpaTransactionManager(emf);
    }

    @Bean
    CurrentUserProvider currentUserProvider() {
        return new ThreadLocalCurrentUserProvider();
    }

    @Bean
    StorageService storageService() {
        try {
            return new FileSystemStorageService(Files.createTempDirectory("deptflow-storage").toString());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
