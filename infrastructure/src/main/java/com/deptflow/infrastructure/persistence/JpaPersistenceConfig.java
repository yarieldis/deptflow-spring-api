package com.deptflow.infrastructure.persistence;

import com.deptflow.application.ports.CurrentTenantProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Enables Spring Data JPA repositories in this package and registers the
 * tenant-context port. The {@code EntityManagerFactory}, {@code DataSource},
 * and transaction manager are provided by Spring Boot auto-configuration (or
 * by a test configuration).
 */
@Configuration
@EnableJpaRepositories(basePackageClasses = JpaInstitutionRepository.class)
public class JpaPersistenceConfig {

    @Bean
    CurrentTenantProvider currentTenantProvider() {
        return new ThreadLocalCurrentTenantProvider();
    }
}
