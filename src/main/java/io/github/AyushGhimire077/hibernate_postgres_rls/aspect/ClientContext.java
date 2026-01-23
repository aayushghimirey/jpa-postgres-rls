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

package io.github.AyushGhimire077.hibernate_postgres_rls.aspect;

import io.github.AyushGhimire077.hibernate_postgres_rls.util.SqlIdentifierValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


/**
 * Manages thread-local storage for PostgreSQL session variables used in RLS.
 * <p>
 * This context acts as a bridge between the security layer (e.g., a Web Filter)
 * and the database layer (the Transactional Aspect).
 * </p>
 * * <h3>Example Usage:</h3>
 * <pre>{@code
 * // Setting a custom key
 * ClientContext.put("tenant_id", "550e8400-e29b-41d4-a716-446655440000");
 * }</pre>
 *
 * @author Aayush Ghimire
 */
public class ClientContext {

    private static final ThreadLocal<Map<String, String>> CONTEXT =
            ThreadLocal.withInitial(HashMap::new);
    private static final Logger log = LoggerFactory.getLogger(ClientContext.class);

    /**
     * Stores a value in the current thread's RLS context with validation.
     * <p>
     * Both the key and value are validated to prevent SQL injection attacks.
     * Invalid keys or values will throw a {@link io.github.AyushGhimire077.hibernate_postgres_rls.exception.RlsSecurityException}.
     * </p>
     *
     * @param key   The session variable key (e.g., "tenant_id").
     *              The library will automatically prefix this with 'app.' when setting in PostgreSQL.
     * @param value The value to be enforced by the RLS policy.
     * @throws io.github.AyushGhimire077.hibernate_postgres_rls.exception.RlsSecurityException if key or value is invalid
     */
    public static void put(String key, String value) {
        // Validate key and value to prevent SQL injection
        SqlIdentifierValidator.validateSessionKey(key);
        SqlIdentifierValidator.validateSessionValue(value);

        log.info("Setting RLS context: {}={}", key, value);
        CONTEXT.get().put(key, value);
    }

    /**
     * Retrieves a value from the current thread's RLS context.
     *
     * @param key The session variable key
     * @return The value associated with the key, or null if not found
     */
    public static String get(String key) {
        return CONTEXT.get().get(key);
    }

    /**
     * Checks if a key exists in the current thread's RLS context.
     *
     * @param key The session variable key
     * @return true if the key exists, false otherwise
     */
    public static boolean contains(String key) {
        return CONTEXT.get().containsKey(key);
    }

    /**
     * Returns all key-value pairs in the current thread's RLS context.
     * <p>
     * This method is thread-safe and returns an immutable view to prevent external modification.
     * </p>
     *
     * @return An immutable map of all context values, or an empty map if no context exists
     */
    public static Map<String, String> getAll() {
        Map<String, String> map = CONTEXT.get();
        return (map != null && !map.isEmpty()) ? Collections.unmodifiableMap(new HashMap<>(map)) : Collections.emptyMap();
    }


    /**
     * Clears the context to prevent memory leaks and "tenant leaking"
     * between reused threads.
     * <p>
     * <b>Important:</b> This should always be called in a finally block
     * or using try-with-resources to ensure cleanup even if an exception occurs.
     * </p>
     */
    public static void clear() {
        CONTEXT.get().clear();
        CONTEXT.remove();
    }

    private ClientContext() {
    }


}
