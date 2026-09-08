package com.deptflow.infrastructure;

import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/**
 * Regenerates the committed Flyway baseline migrations (V1) for PostgreSQL and
 * SQL Server from {@code META-INF/orm.xml}, using the JPA schema-generation
 * script mechanism (offline, no database connection).
 *
 * Run via:
 * {@code ./mvnw -pl infrastructure -am -Pgenerate-schema process-test-classes}
 *
 * The module base directory is passed in via the {@code schema.baseDir} system
 * property (set by the Maven profile). Only regenerate while the schema is
 * still greenfield: modifying an already applied migration (V1) breaks
 * Flyway's checksum validation. Future schema changes should go into V2__, V3__.
 */
public final class SchemaGenerator {

    private SchemaGenerator() {
    }

    public static void main(String[] args) {
        Path base = Path.of(System.getProperty("schema.baseDir", ".")).toAbsolutePath().normalize();
        generate(base, "org.hibernate.dialect.PostgreSQLDialect",
                "src/main/resources/db/migration/postgresql/V1__init.sql",
                "-- DeptFlow baseline schema (PostgreSQL). Generated from META-INF/orm.xml.");
        generate(base, "org.hibernate.dialect.SQLServerDialect",
                "src/main/resources/db/migration/sqlserver/V1__init.sql",
                "-- DeptFlow baseline schema (SQL Server). Generated from META-INF/orm.xml.");
    }

    private static void generate(Path base, String dialect, String relativeTarget, String header) {
        Path temp = base.resolve("target").resolve("schema-gen.sql");
        Path target = base.resolve(relativeTarget);

        LocalContainerEntityManagerFactoryBean emf = new LocalContainerEntityManagerFactoryBean();
        emf.setDataSource(new DriverManagerDataSource());
        emf.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
        emf.setMappingResources("META-INF/orm.xml");

        Properties props = new Properties();
        props.setProperty("hibernate.dialect", dialect);
        props.setProperty("jakarta.persistence.schema-generation.scripts.action", "create");
        props.setProperty("jakarta.persistence.schema-generation.scripts.create-target", temp.toString());
        emf.setJpaProperties(props);

        try {
            emf.afterPropertiesSet();
            String ddl = Files.readString(temp);
            Files.writeString(target,
                    header + System.lineSeparator() + System.lineSeparator() + ddl.trim() + System.lineSeparator());
            System.out.println("Generated " + target);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } finally {
            emf.destroy();
            try {
                Files.deleteIfExists(temp);
            } catch (IOException ignored) {
                // best effort cleanup
            }
        }
    }
}
