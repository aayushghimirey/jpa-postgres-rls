package io.github.AyushGhimire077.hibernate_postgres_rls;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = TestApplication.class)
@ActiveProfiles("test")
public class RlsIntegrationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void verifyRlsIsActive() {
        // 1. Warm up: Force a query to ensure Hibernate is fully initialized
        jdbcTemplate.execute("SELECT 1");

        // 2. Check pg_class using a case-insensitive match for 'test_entity'
        String sql = "SELECT relrowsecurity FROM pg_class WHERE lower(relname) = lower('test_entity')";

        Boolean rlsEnabled = jdbcTemplate.queryForObject(sql, Boolean.class);

        System.out.println(">>> Database Check: RLS Enabled Status = " + rlsEnabled);

        assertNotNull(rlsEnabled, "Table 'test_entity' was not found in Postgres catalog!");
        assertTrue(rlsEnabled, "Postgres Row Level Security should be TRUE. " +
                "If it's FALSE, check if the @EnableRls annotation is on TestEntity.");
    }
}