/*
 * Copyright (C) 2026 Aayush Ghimire
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */


package io.github.AyushGhimire077.hibernate_postgres_rls.core;

import io.github.AyushGhimire077.hibernate_postgres_rls.exception.RlsPolicyException;
import io.github.AyushGhimire077.hibernate_postgres_rls.exception.RlsSecurityException;
import io.github.AyushGhimire077.hibernate_postgres_rls.users.RlsUserManager;
import io.github.AyushGhimire077.hibernate_postgres_rls.util.SqlIdentifierValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * PostgreSQL implementation of {@link RlsUserManager}.
 */
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
        log.info(">>> RLS Manager: Checking/Creating role '{}' for schema '{}'", username, schema);

        SqlIdentifierValidator.validateIdentifier(username, "username");
        SqlIdentifierValidator.validateIdentifier(schema, "schema");
        if (password == null || password.isEmpty()) {
            throw new RlsSecurityException("Password cannot be null or empty");
        }

        try {
            // We use quoted identifiers for the role name and quote_literal for the password.
            // Since we are inside a DO block, we use EXECUTE to handle the dynamic role name.
            String escapedUser = username.replace("'", "''");
            String escapedPassword = password.replace("'", "''");
            String quotedUser = SqlIdentifierValidator.quoteIdentifier(username).replace("'", "''");

            String createRoleSql = String.format(
                    "DO $$ BEGIN IF NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = '%s') " +
                            "THEN EXECUTE 'CREATE ROLE %s WITH LOGIN PASSWORD ' || quote_literal('%s'); END IF; END $$",
                    escapedUser,
                    quotedUser,
                    escapedPassword
            );
            jdbcTemplate.execute(createRoleSql);
            log.info(">>> RLS Manager: Role '{}' checked/created", username);

            if (grantUsage) {
                grantAllPermissions(username, schema);
            }
        } catch (Exception e) {
            log.error("Failed to create tenant: {}", username, e);
            throw new RlsPolicyException("Failed to create database role for tenant: " + username, e);
        }
    }

    @Override
    @Transactional
    public void grantPermissions(String username, String schema, List<RlsPermission> permissions) {
        SqlIdentifierValidator.validateIdentifier(username, "username");
        SqlIdentifierValidator.validateIdentifier(schema, "schema");

        if (permissions == null || permissions.isEmpty()) {
            return;
        }

        String quotedUser = SqlIdentifierValidator.quoteIdentifier(username);
        String quotedSchema = SqlIdentifierValidator.quoteIdentifier(schema);

        String privs = permissions.stream()
                .map(Enum::name)
                .collect(Collectors.joining(", "));

        try {
            jdbcTemplate.execute(String.format("GRANT USAGE ON SCHEMA %s TO %s", quotedSchema, quotedUser));
            jdbcTemplate.execute(String.format("GRANT %s ON ALL TABLES IN SCHEMA %s TO %s", privs, quotedSchema, quotedUser));
            jdbcTemplate.execute(String.format("GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA %s TO %s", quotedSchema, quotedUser));

            log.info(">>> RLS Manager: Granted {} to {}", privs, username);
        } catch (Exception e) {
            log.error("Failed to grant permissions to user: {}", username, e);
            throw new RlsPolicyException("Failed to grant permissions to database role: " + username, e);
        }
    }

    @Override
    @Transactional
    public void grantAllPermissions(String username, String schema) {
        SqlIdentifierValidator.validateIdentifier(username, "username");
        SqlIdentifierValidator.validateIdentifier(schema, "schema");

        String quotedUser = SqlIdentifierValidator.quoteIdentifier(username);
        String quotedSchema = SqlIdentifierValidator.quoteIdentifier(schema);

        try {
            jdbcTemplate.execute(String.format("GRANT USAGE ON SCHEMA %s TO %s", quotedSchema, quotedUser));
            jdbcTemplate.execute(String.format("GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA %s TO %s", quotedSchema, quotedUser));
            jdbcTemplate.execute(String.format("GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA %s TO %s", quotedSchema, quotedUser));
            log.info(">>> RLS Manager: Granted ALL PRIVILEGES to {}", username);
        } catch (Exception e) {
            log.error("Failed to grant all permissions to user: {}", username, e);
            throw new RlsPolicyException("Failed to grant all privileges to database role: " + username, e);
        }
    }

    @Override
    @Transactional
    public void revokeAllPermissions(String username, String schema) {
        SqlIdentifierValidator.validateIdentifier(username, "username");
        SqlIdentifierValidator.validateIdentifier(schema, "schema");

        String quotedUser = SqlIdentifierValidator.quoteIdentifier(username);
        String quotedSchema = SqlIdentifierValidator.quoteIdentifier(schema);

        try {
            jdbcTemplate.execute(String.format("REVOKE ALL PRIVILEGES ON ALL TABLES IN SCHEMA %s FROM %s", quotedSchema, quotedUser));
            jdbcTemplate.execute(String.format("REVOKE ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA %s FROM %s", quotedSchema, quotedUser));
            log.info(">>> RLS Manager: Revoked all permissions from {}", username);
        } catch (Exception e) {
            log.error("Failed to revoke permissions from user: {}", username, e);
            throw new RlsPolicyException("Failed to revoke all privileges from database role: " + username, e);
        }
    }

    @Override
    @Transactional
    public void dropTenant(String username) {
        SqlIdentifierValidator.validateIdentifier(username, "username");
        String quotedUser = SqlIdentifierValidator.quoteIdentifier(username);

        try {
            jdbcTemplate.execute(String.format("DROP ROLE IF EXISTS %s", quotedUser));
            log.info(">>> RLS Manager: Dropped role {}", username);
        } catch (Exception e) {
            log.error("Failed to drop user: {}", username, e);
            throw new RlsPolicyException("Failed to drop database role: " + username, e);
        }
    }

    @Override
    public void setActiveRole(String username) {
        SqlIdentifierValidator.validateIdentifier(username, "username");

        try {
            String checkRoleSql = "SELECT count(*) FROM pg_roles WHERE rolname = ?";
            Integer count = jdbcTemplate.queryForObject(checkRoleSql, Integer.class, username);

            if (count != null && count > 0) {
                log.info(">>> RLS Manager: Setting active role to '{}'", username);
                String quotedUser = SqlIdentifierValidator.quoteIdentifier(username);
                jdbcTemplate.execute(String.format("SET ROLE %s", quotedUser));
            } else {
                log.warn(">>> RLS Manager: Role '{}' does not exist. Skipping SET ROLE.", username);
            }
        } catch (Exception e) {
            log.error("Failed to set active role: {}", username, e);
            throw new RlsSecurityException("Failed to set active database role: " + username, e);
        }
    }
}