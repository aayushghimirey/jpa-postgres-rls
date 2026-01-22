package io.github.AyushGhimire077.hibernate_postgres_rls.aspect;

import java.sql.PreparedStatement;
import java.util.Map;
import java.util.stream.Collectors;

import org.hibernate.resource.jdbc.spi.StatementInspector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/*
 *  Inject session variables
 *  this was per query
 * */
//public class RlsQueryInterceptor implements StatementInspector {
//
//    private static final Logger log = LoggerFactory.getLogger(RlsQueryInterceptor.class);
//
//    @Override
//    public String inspect(String sql) {
//
//        Map<String, String> contextMap = ClientContext.getAll();
//        if (contextMap == null || contextMap.isEmpty()) {
//            return sql;
//        }
//        String normalizedSql = sql.toLowerCase().trim();
//
//        if (normalizedSql.startsWith("select 1") ||
//                normalizedSql.startsWith("select null") ||
//                normalizedSql.contains("pg_catalog") ||
//                normalizedSql.contains("information_schema") ||
//                normalizedSql.contains("current_schema") ||
//                normalizedSql.contains("version()")) {
//            return sql;
//        }
//
//        try {
//            String setCommands = contextMap.entrySet().stream()
//                    .map(entry -> {
//                        String key = entry.getKey().startsWith("app.") ? entry.getKey() : "app." + entry.getKey();
//                        return String.format("set_config('%s', '%s', true)", key, entry.getValue());
//                    })
//                    .collect(Collectors.joining(" "));
//
//            return setCommands + "\n" + sql;
//        } catch (Exception e) {
//            log.error("Failed to inject RLS variables", e);
//            return sql; // Fallback to original SQL on error
//        }
//    }
//}


// per transaction
@Component
public class RlsTransactionInitializer {

    private static final Logger log =
            LoggerFactory.getLogger(RlsTransactionInitializer.class);

    private final JdbcTemplate jdbcTemplate;

    public RlsTransactionInitializer(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void applyRlsContextIfNeeded() {

        if (!TransactionSynchronizationManager.isActualTransactionActive()) {
            log.trace("No active transaction → skipping RLS context setup");
            return;
        }

        Map<String, String> ctx = ClientContext.getAll();
        if (ctx == null || ctx.isEmpty()) {
            log.trace("ClientContext empty → no RLS variables to apply");
            return;
        }

        log.debug("Registering RLS context for transaction: {}", ctx.keySet());

        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {

                    @Override
                    public void beforeCommit(boolean readOnly) {
                        log.debug(
                                "Applying RLS variables before commit (readOnly={})",
                                readOnly
                        );

                        ctx.forEach((key, value) -> {
                            log.trace("SET LOCAL app.{} = {}", key, value);

                            jdbcTemplate.update(
                                    "SET LOCAL app." + key + " = ?",
                                    value
                            );
                        });
                    }
                }
        );
    }
}
