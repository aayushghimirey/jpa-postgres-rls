package io.github.AyushGhimire077.hibernate_postgres_rls.core;

import io.github.AyushGhimire077.hibernate_postgres_rls.users.RlsUserManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

public class PostgreSqlRlsUserManager implements RlsUserManager {

    private static final Logger log = LoggerFactory.getLogger(PostgreSqlRlsUserManager.class);
    private final JdbcTemplate jdbcTemplate;

    public PostgreSqlRlsUserManager(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    @Transactional
    public void createTenant(String username, String password, String schema) {
        createTenant(username, password, schema, true);
    }

    @Override
    @Transactional
    public void createTenant(String username, String password, String schema, boolean grantUsage) {
        String createRoleSql = String.format(
                "DO $$ BEGIN IF NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = '%s') " +
                        "THEN CREATE ROLE \"%s\" WITH LOGIN PASSWORD '%s'; END IF; END $$",
                username, username, password
        );
        jdbcTemplate.execute(createRoleSql);
        log.info(">>> RLS Manager: Role '{}' checked/created", username);

        if (grantUsage) {
            grantAllPermissions(username, schema);
        }
    }

    @Override
    @Transactional
    public void grantPermissions(String username, String schema, List<RlsPermission> permissions) {
        String privs = permissions.stream()
                .map(Enum::name)
                .collect(Collectors.joining(", "));

        jdbcTemplate.execute(String.format("GRANT USAGE ON SCHEMA \"%s\" TO \"%s\"", schema, username));
        jdbcTemplate.execute(String.format("GRANT %s ON ALL TABLES IN SCHEMA \"%s\" TO \"%s\"", privs, schema, username));
        jdbcTemplate.execute(String.format("GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA \"%s\" TO \"%s\"", schema, username));

        log.info(">>> RLS Manager: Granted {} to {}", privs, username);
    }

    @Override
    @Transactional
    public void grantAllPermissions(String username, String schema) {
        jdbcTemplate.execute(String.format("GRANT USAGE ON SCHEMA \"%s\" TO \"%s\"", schema, username));
        jdbcTemplate.execute(String.format("GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA \"%s\" TO \"%s\"", schema, username));
        jdbcTemplate.execute(String.format("GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA \"%s\" TO \"%s\"", schema, username));
        log.info(">>> RLS Manager: Granted ALL PRIVILEGES to {}", username);
    }

    @Override
    @Transactional
    public void revokeAllPermissions(String username, String schema) {
        jdbcTemplate.execute(String.format("REVOKE ALL PRIVILEGES ON ALL TABLES IN SCHEMA \"%s\" FROM \"%s\"", schema, username));
        jdbcTemplate.execute(String.format("REVOKE ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA \"%s\" FROM \"%s\"", schema, username));
        log.info(">>> RLS Manager: Revoked all permissions from {}", username);
    }

    @Override
    @Transactional
    public void dropTenant(String username) {
        // Drop owned objects first or the drop role will fail
        jdbcTemplate.execute(String.format("DROP ROLE IF EXISTS \"%s\"", username));
        log.info(">>> RLS Manager: Dropped role {}", username);
    }
}