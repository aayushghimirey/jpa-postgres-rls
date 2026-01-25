package io.github.aayushghimirey.jpa_postgres_rls.core;

import io.github.aayushghimirey.jpa_postgres_rls.annotation.RlsRule;
import io.github.aayushghimirey.jpa_postgres_rls.exception.RlsConfigurationException;
import io.github.aayushghimirey.jpa_postgres_rls.util.SqlIdentifierValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

/**
 * Validates that RLS (Row-Level Security) annotations on JPA entities
 * are correctly configured and present in the PostgreSQL database at startup.
 *
 * <p>
 * - Only validates, no DDL or data changes performed.
 * - Resolves tables using PostgreSQL {@code search_path}.
 * - Ensures policies reference the required session variable.
 * </p>
 *
 * <p>
 * Usage:
 * <pre>
 * StartupValidation validation = new StartupValidation(dataSource);
 * validation.validate(listOfRlsRules);
 * </pre>
 * </p>
 *
 * @author Aayush Ghimire
 * @since 2026
 */
public class StartupValidation {

    private static final Logger log = LoggerFactory.getLogger(StartupValidation.class);

    private final DataSource dataSource;

    public StartupValidation(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * Validates a list of {@link RlsRule} against the database.
     *
     * @param rules list of RLS rules extracted from entity annotations
     */
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

    /*
       Annotation validation
        */

    private void validateAnnotation(RlsRule rule) {

        if (rule.table() == null || rule.table().isBlank()) {
            throw new RlsConfigurationException("RLS rule missing table name");
        }

        if (rule.policy() == null || rule.policy().isBlank()) {
            throw new RlsConfigurationException("RLS rule missing policy name");
        }

        if (rule.requiredVariable() == null || rule.requiredVariable().isBlank()) {
            throw new RlsConfigurationException("RLS rule missing requiredVariable");
        }

        SqlIdentifierValidator.validateTable(rule.table());
        SqlIdentifierValidator.validatePolicy(rule.policy());
        SqlIdentifierValidator.validateSessionKey(rule.requiredVariable());
    }

    /*
       Database validation
        */

    private void validateDbState(Connection conn, RlsRule rule) throws Exception {

        log.debug("Validating RLS for table '{}'", rule.table());

        // Make sure table exists and get OID
        long tableOid;
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT to_regclass(?)"
        )) {
            ps.setString(1, rule.table());

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next() || rs.getObject(1) == null) {
                    throw new RlsConfigurationException(
                            "CRITICAL: Table '" + rule.table() +
                                    "' not found in current search_path"
                    );
                }
                tableOid = rs.getLong(1);
            }
        }


        // Make sure RLS is enabled on the table with oid
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT relrowsecurity FROM pg_class WHERE oid = ?"
        )) {
            ps.setLong(1, tableOid);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next() || !rs.getBoolean(1)) {
                    throw new RlsConfigurationException(
                            "CRITICAL: RLS is not enabled on table '" +
                                    rule.table() + "'"
                    );
                }
            }
        }

        log.debug("RLS is enabled on table '{}'", rule.table());


        // Validate specific policy + required variable
        try (PreparedStatement ps = conn.prepareStatement(
                """
                        SELECT p.polqual
                        FROM pg_policies p
                        WHERE p.polrelid = ?
                          AND p.polname = ?;
                        """
        )) {
            ps.setLong(1, tableOid);
            ps.setString(2, rule.policy());

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new RlsConfigurationException(
                            "CRITICAL: RLS policy '" + rule.policy() +
                                    "' does not exist on table '" + rule.table() + "'"
                    );
                }

                String qualifier = rs.getString("polqual");  // fix here
                if (qualifier == null || !qualifier.contains(rule.requiredVariable())) {
                    throw new RlsConfigurationException(
                            "CRITICAL: RLS policy '" + rule.policy() +
                                    "' does not reference required variable '" +
                                    rule.requiredVariable() + "'"
                    );
                }
            }
        }

        log.debug("RLS validated successfully for table '{}'", rule.table());
    }


}
