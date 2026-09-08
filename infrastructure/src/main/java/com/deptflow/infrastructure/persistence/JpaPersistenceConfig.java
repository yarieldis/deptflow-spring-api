package com.deptflow.infrastructure.persistence;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Enables Spring Data JPA repositories in this package. The
 * {@code EntityManagerFactory}, {@code DataSource}, and transaction manager are
 * provided by Spring Boot auto-configuration (or by a test configuration); the
 * tenant/user providers are wired by the composition root (api) or tests.
 */
@Configuration
@EnableJpaRepositories(basePackageClasses = JpaInstitutionRepository.class)
public class JpaPersistenceConfig {
}
