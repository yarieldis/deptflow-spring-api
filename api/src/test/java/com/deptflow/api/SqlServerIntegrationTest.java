package com.deptflow.api;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.mssqlserver.MSSQLServerContainer;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Boots the full application against a real SQL Server container, applying the
 * Flyway baseline migration and exercising auth + a tenant-root write.
 * Skipped automatically when no container engine is available.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("sqlserver")
@Testcontainers(disabledWithoutDocker = true)
class SqlServerIntegrationTest {

    @Container
    static MSSQLServerContainer sqlserver = new MSSQLServerContainer("mcr.microsoft.com/mssql/server:2022-latest")
            .acceptLicense();

    @DynamicPropertySource
    static void datasource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url",
                () -> sqlserver.getJdbcUrl() + ";encrypt=false;trustServerCertificate=true");
        registry.add("spring.datasource.username", sqlserver::getUsername);
        registry.add("spring.datasource.password", sqlserver::getPassword);
    }

    @Autowired
    MockMvc mockMvc;

    @Test
    void registerLoginAndInstitutionWriteAgainstSqlServer() throws Exception {
        String token = registerAndLogin("bob", "bob@example.com", "secret");

        mockMvc.perform(post("/api/institutions")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Beta\",\"code\":\"beta\"}"))
                .andExpect(status().isCreated());
    }

    private String registerAndLogin(String username, String email, String password) throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + username + "\",\"email\":\"" + email + "\",\"password\":\"" + password + "\"}"))
                .andExpect(status().isCreated());

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}"))
                .andExpect(status().isOk())
                .andReturn();

        return JsonPath.read(result.getResponse().getContentAsString(), "$.token");
    }
}
