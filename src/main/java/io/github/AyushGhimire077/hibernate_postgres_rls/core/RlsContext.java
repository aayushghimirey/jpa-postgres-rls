package io.github.AyushGhimire077.hibernate_postgres_rls.core;

import io.github.AyushGhimire077.hibernate_postgres_rls.util.SqlIdentifierValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.HashMap;
import java.util.Map;

/**
 * Core API for transaction-scoped PostgreSQL session variable binding.
 * <p>
 * Ensures variables like {@code app.tenant_id} are safely set using {@code SET LOCAL}
 * and managed across transaction lifecycles.
 * </p>
 * Responsible for applying staged session variables to the current PostgreSQL session.
 * <p>
 * This class is used and called by the {@link io.github.AyushGhimire077.hibernate_postgres_rls.aspect.RlsTransactionalAspect} to bind.
 *
 */
public class RlsContext {

    private static final Logger log = LoggerFactory.getLogger(RlsContext.class);
    private final JdbcTemplate jdbcTemplate;
    private final ThreadLocal<Map<String, Object>> stagedVariables = ThreadLocal.withInitial(HashMap::new);

    public RlsContext(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Stage a session variable for the next apply() call.
     */
    public RlsContext with(String session_key, Object session_value) {
        SqlIdentifierValidator.validateSessionKey(session_key);
        stagedVariables.get().put(session_key, session_value);
        return this;
    }

    /**
     * Apply staged variables to the current PostgreSQL session using SET LOCAL.
     * Must be called within an active transaction.
     */
    public void apply() {
        if (!TransactionSynchronizationManager.isActualTransactionActive()) {
            throw new IllegalStateException("RLS context requires an active transaction");
        }

        Map<String, Object> vars = stagedVariables.get();
        if (vars.isEmpty()) return;

        vars.forEach((key, value) -> {
            /**
             * If session key does not contain a dot, prefix with "app."
             * */
            String sessionKey = key.contains(".") ? key : "app." + key;
            String valStr = (value == null) ? "" : value.toString();

            log.debug("Applying RLS session variable: {} = {}", sessionKey, valStr);

            try {
                jdbcTemplate.execute(String.format("SELECT set_config('%s', ?, true)", sessionKey), (java.sql.PreparedStatement ps) -> {
                    ps.setString(1, valStr);
                    ps.executeQuery();
                    return null;
                });
            } catch (Exception e) {
                log.error("Failed to set RLS variable: {}", sessionKey, e);
                throw new RuntimeException("RLS session bind failed: " + sessionKey, e);
            }
        });

        vars.clear();
    }
}
