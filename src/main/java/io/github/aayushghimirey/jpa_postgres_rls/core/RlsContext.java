package io.github.aayushghimirey.jpa_postgres_rls.core;

import io.github.aayushghimirey.jpa_postgres_rls.util.SqlIdentifierValidator;
import io.github.aayushghimirey.jpa_postgres_rls.annotation.RlsSession;
import io.github.aayushghimirey.jpa_postgres_rls.aspect.RlsTransactionalAspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.sql.PreparedStatement;
import java.util.HashMap;
import java.util.Map;

/**
 * Core API for managing transaction-scoped PostgreSQL session variables.
 * <p>
 * This class allows safe binding of variables (e.g., {@code app.tenant_id}) within a
 * transaction scope using PostgreSQL {@code SET LOCAL} statements.
 * </p>
 * <p>
 * Typically invoked by {@link RlsTransactionalAspect}
 * to automatically bind method parameters annotated with {@link RlsSession}.
 * </p>
 * <p>
 * Usage example:
 * <pre>
 * rlsContext.with("tenant_id", tenantId);
 * rlsContext.apply(); // must be called within an active transaction
 * </pre>
 *
 * @author Aayush Ghimire
 * @since 2026
 */
public class RlsContext {

    private static final Logger log = LoggerFactory.getLogger(RlsContext.class);
    private final JdbcTemplate jdbcTemplate;
    private final ThreadLocal<Map<String, Object>> stagedVariables = ThreadLocal.withInitial(HashMap::new);

    public RlsContext(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Stage a session variable for the next {@link #apply()} call.
     * <p>
     * This only stages the variable in thread-local storage; it is not applied to the database yet.
     * </p>
     *
     * @param sessionKey   the PostgreSQL session variable name
     * @param sessionValue the value to bind
     * @return this context for chaining
     */
    public RlsContext with(String sessionKey, Object sessionValue) {
        SqlIdentifierValidator.validateSessionKey(sessionKey);
        stagedVariables.get().put(sessionKey, sessionValue);
        return this;
    }

    /**
     * Apply all staged session variables to the current PostgreSQL session using {@code SET LOCAL}.
     * <p>
     * Must be called within an active transaction. After applying, the staged variables are cleared.
     * </p>
     *
     * @throws IllegalStateException if called outside an active transaction
     * @throws RuntimeException      if setting a session variable fails
     */
    public void apply() {
        if (!TransactionSynchronizationManager.isActualTransactionActive()) {
            throw new IllegalStateException("RLS context requires an active transaction");
        }

        Map<String, Object> vars = stagedVariables.get();
        if (vars.isEmpty()) return;

        vars.forEach((key, value) -> {
            // Prefix with "app." if session key has no dot
            String sessionKey = key.contains(".") ? key : "app." + key;
            String valStr = (value == null) ? "" : value.toString();

            log.info("Applying RLS session variable: {} = {}", sessionKey, valStr);

            try {
                Object execute = jdbcTemplate.execute(String.format("SELECT set_config('%s', ?, true)", sessionKey), (PreparedStatement ps) -> {
                    ps.setString(1, valStr);
                    ps.executeQuery();
                    return null;
                });
                if (execute == null) {
                    log.error("Failed to set RLS variable: {}", sessionKey);
//                    throw new RuntimeException("RLS session bind failed: " + sessionKey);
                }
            } catch (Exception e) {
                log.error("Failed to set RLS variable: {}", sessionKey, e);
                throw new RuntimeException("RLS session bind failed: " + sessionKey, e);
            }
        });

        // Clear after applying to avoid leaking variables across transactions
        vars.clear();
    }
}
