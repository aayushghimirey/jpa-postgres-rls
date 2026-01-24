package io.github.AyushGhimire077.hibernate_postgres_rls.core;

import io.github.AyushGhimire077.hibernate_postgres_rls.annotation.RlsRule;
import io.github.AyushGhimire077.hibernate_postgres_rls.util.SqlIdentifierValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

/**
 * Validates RLS annotations against PostgreSQL database state at startup.
 * Schema is resolved using PostgreSQL search_path.
 */
public class StartupValidation {

    private static final Logger log = LoggerFactory.getLogger(StartupValidation.class);
    private final DataSource dataSource;

    public StartupValidation(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void validate(List<RlsRule> rules) {
        log.info("Starting RLS validation for {} rule(s)", rules.size());

        try (Connection conn = dataSource.getConnection()) {
            for (RlsRule rule : rules) {
                validateAnnotation(rule);
                validateDbState(conn, rule);
            }
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException("RLS startup validation failed", e);
        }

        log.info("RLS validation completed successfully");
    }

    // Annotation validation

    private void validateAnnotation(RlsRule rule) {

        if (rule.table() == null || rule.table().isBlank()) {
            throw new IllegalStateException("RLS rule missing table name");
        }

        if (rule.policy() == null || rule.policy().isBlank()) {
            throw new IllegalStateException("RLS rule missing policy name");
        }

        if (rule.requiredVariable() == null || rule.requiredVariable().isBlank()) {
            throw new IllegalStateException("RLS rule missing requiredVariable");
        }

        SqlIdentifierValidator.validateTable(rule.table()); // name of table
        SqlIdentifierValidator.validatePolicy(rule.policy()); // name of policy
        SqlIdentifierValidator.validateSessionKey(rule.requiredVariable()); // name of session variable
    }

    // Database validation

    private void validateDbState(Connection conn, RlsRule rule) throws Exception {

        log.debug("Validating RLS for table '{}'", rule.table());

        // 1️⃣ Resolve table OID using search_path
        Long tableOid;
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT to_regclass(?)"
        )) {
            ps.setString(1, rule.table());

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next() || rs.getObject(1) == null) {
                    throw new IllegalStateException(
                            "CRITICAL: Table '" + rule.table() +
                                    "' not found in current search_path"
                    );
                }
                tableOid = rs.getLong(1);
            }
        }

        // 2️⃣ Check RLS enabled
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT relrowsecurity FROM pg_class WHERE oid = ?"
        )) {
            ps.setLong(1, tableOid);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next() || !rs.getBoolean(1)) {
                    throw new IllegalStateException(
                            "CRITICAL: RLS is not enabled on table '" +
                                    rule.table() + "'"
                    );
                }
            }
        }

        // 3️⃣ Validate policy and required variable
        try (PreparedStatement ps = conn.prepareStatement(
                """
                        SELECT qual
                        FROM pg_policies
                        WHERE tablename = ?
                          AND polname   = ?
                        """
        )) {
            ps.setString(1, rule.table());
            ps.setString(2, rule.policy());

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new IllegalStateException(
                            "CRITICAL: RLS policy '" + rule.policy() +
                                    "' does not exist on table '" +
                                    rule.table() + "'"
                    );
                }

                String qualifier = rs.getString(1);
                if (qualifier == null || !qualifier.contains(rule.requiredVariable())) {
                    throw new IllegalStateException(
                            "CRITICAL: RLS policy '" + rule.policy() +
                                    "' does not reference required variable '" +
                                    rule.requiredVariable() + "'"
                    );
                }
            }
        }

        log.debug("RLS validated for table '{}'", rule.table());
    }
}
