package com.deptflow.infrastructure;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;

import java.util.Properties;

/**
 * Generates the baseline DDL for Postgres and SQL Server from the shared
 * {@code META-INF/orm.xml} mappings using the JPA schema-generation script
 * mechanism (offline; no database connection). Run once, then copy the output
 * into {@code db/migration/{postgresql,sqlserver}/V1__init.sql}.
 *
 * Disabled by default; remove {@code @Disabled} to regenerate.
 */
@Disabled("one-time DDL generation utility")
class SchemaDdlGeneratorTest {

    @Test
    void generate() {
        generate("org.hibernate.dialect.PostgreSQLDialect", "target/schema-postgres.sql");
        generate("org.hibernate.dialect.SQLServerDialect", "target/schema-sqlserver.sql");
    }

    private void generate(String dialect, String target) {
        LocalContainerEntityManagerFactoryBean emf = new LocalContainerEntityManagerFactoryBean();
        emf.setDataSource(new DriverManagerDataSource());
        emf.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
        emf.setMappingResources("META-INF/orm.xml");

        Properties props = new Properties();
        props.setProperty("hibernate.dialect", dialect);
        props.setProperty("jakarta.persistence.schema-generation.scripts.action", "create");
        props.setProperty("jakarta.persistence.schema-generation.scripts.create-target", target);
        emf.setJpaProperties(props);

        emf.afterPropertiesSet();
        emf.destroy();
    }
}
