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

package io.github.AyushGhimire077.hibernate_postgres_rls.util;

/**
 * Utility class for building common Row Level Security (RLS) policy conditions.
 * <p>
 * This class provides helper methods to generate SQL conditions for typical RLS scenarios,
 * reducing boilerplate and preventing common mistakes in policy definitions.
 * </p>
 *
 * <h3>Usage Example</h3>
 * <pre>
 * {@code
 * @RlsRule(
 *     using = RlsConditionBuilder.tenantIsolation("tenant_id", "tenant_id", "bigint"),
 *     withCheck = RlsConditionBuilder.tenantIsolation("tenant_id", "tenant_id", "bigint"),
 *     policyType = PolicyType.ALL
 * )
 * }
 * </pre>
 *
 * @since 0.0.1
 */
public final class RlsConditionBuilder {

    private RlsConditionBuilder() {
        // Utility class - prevent instantiation
    }

    /**
     * Creates a tenant isolation condition that filters rows based on a tenant ID column.
     * <p>
     * This is the most common RLS pattern for multi-tenant applications.
     * </p>
     *
     * @param column      the database column name containing the tenant ID
     * @param sessionKey  the session variable key (without 'app.' prefix)
     * @param castType    the PostgreSQL type to cast to (e.g., "text", "bigint", "uuid")
     * @return SQL condition string
     *
     * @example
     * <pre>
     * tenantIsolation("tenant_id", "tenant_id", "bigint")
     * // Returns: "tenant_id = NULLIF(current_setting('app.tenant_id', true), '')::bigint"
     * </pre>
     */
    public static String tenantIsolation(String column, String sessionKey, String castType) {
        validateInputs(column, sessionKey, castType);
        String qualifiedKey = sessionKey.contains(".") ? sessionKey : "app." + sessionKey;
        return String.format(
                "%s = NULLIF(current_setting('%s', true), '')::%s",
                column, qualifiedKey, castType
        );
    }

    /**
     * Creates a tenant isolation condition with superuser bypass.
     * <p>
     * Allows the 'postgres' superuser to bypass RLS restrictions, which is useful
     * for administrative operations and maintenance tasks.
     * </p>
     *
     * @param column      the database column name containing the tenant ID
     * @param sessionKey  the session variable key (without 'app.' prefix)
     * @param castType    the PostgreSQL type to cast to
     * @return SQL condition string with superuser bypass
     *
     * @example
     * <pre>
     * tenantIsolationWithBypass("tenant_id", "tenant_id", "text")
     * // Returns: "(tenant_id = NULLIF(current_setting('app.tenant_id', true), '')::text) OR current_user = 'postgres'"
     * </pre>
     */
    public static String tenantIsolationWithBypass(String column, String sessionKey, String castType) {
        return withSuperuserBypass(tenantIsolation(column, sessionKey, castType));
    }

    /**
     * Creates a user isolation condition that filters rows based on a user ID column.
     *
     * @param column      the database column name containing the user ID
     * @param sessionKey  the session variable key (without 'app.' prefix)
     * @param castType    the PostgreSQL type to cast to
     * @return SQL condition string
     *
     * @example
     * <pre>
     * userIsolation("user_id", "user_id", "bigint")
     * // Returns: "user_id = NULLIF(current_setting('app.user_id', true), '')::bigint"
     * </pre>
     */
    public static String userIsolation(String column, String sessionKey, String castType) {
        return tenantIsolation(column, sessionKey, castType); // Same logic
    }

    /**
     * Creates a multi-tenant condition with user-level filtering.
     * <p>
     * Useful for scenarios where data is partitioned by both tenant and user,
     * such as a SaaS application where each tenant has multiple users.
     * </p>
     *
     * @param tenantColumn    the database column name for tenant ID
     * @param tenantKey       the session variable key for tenant ID
     * @param tenantCastType  the PostgreSQL type for tenant ID
     * @param userColumn      the database column name for user ID
     * @param userKey         the session variable key for user ID
     * @param userCastType    the PostgreSQL type for user ID
     * @return SQL condition string combining both filters
     *
     * @example
     * <pre>
     * multiTenantWithUser("tenant_id", "tenant_id", "bigint", "user_id", "user_id", "bigint")
     * // Returns: "tenant_id = NULLIF(current_setting('app.tenant_id', true), '')::bigint AND user_id = NULLIF(current_setting('app.user_id', true), '')::bigint"
     * </pre>
     */
    public static String multiTenantWithUser(
            String tenantColumn, String tenantKey, String tenantCastType,
            String userColumn, String userKey, String userCastType) {
        return String.format(
                "%s AND %s",
                tenantIsolation(tenantColumn, tenantKey, tenantCastType),
                userIsolation(userColumn, userKey, userCastType)
        );
    }

    /**
     * Wraps a condition with a superuser bypass clause.
     * <p>
     * This allows the 'postgres' user (or any superuser) to bypass the RLS policy,
     * which is often necessary for administrative tasks.
     * </p>
     *
     * @param condition the base SQL condition
     * @return the condition wrapped with superuser bypass
     *
     * @example
     * <pre>
     * withSuperuserBypass("tenant_id = 123")
     * // Returns: "(tenant_id = 123) OR current_user = 'postgres'"
     * </pre>
     */
    public static String withSuperuserBypass(String condition) {
        if (condition == null || condition.trim().isEmpty()) {
            throw new IllegalArgumentException("Condition cannot be null or empty");
        }
        return String.format("(%s) OR current_user = 'postgres'", condition);
    }

    /**
     * Creates a condition that allows access only to rows with a specific status.
     * <p>
     * Useful for implementing soft deletes or workflow-based access control.
     * </p>
     *
     * @param statusColumn the database column name for status
     * @param statusValue  the allowed status value
     * @return SQL condition string
     *
     * @example
     * <pre>
     * statusFilter("status", "active")
     * // Returns: "status = 'active'"
     * </pre>
     */
    public static String statusFilter(String statusColumn, String statusValue) {
        validateInputs(statusColumn, statusValue);
        return String.format("%s = '%s'", statusColumn, statusValue);
    }

    /**
     * Combines multiple conditions with AND logic.
     *
     * @param conditions the conditions to combine
     * @return combined SQL condition string
     *
     * @example
     * <pre>
     * and("tenant_id = 1", "status = 'active'", "deleted_at IS NULL")
     * // Returns: "(tenant_id = 1) AND (status = 'active') AND (deleted_at IS NULL)"
     * </pre>
     */
    public static String and(String... conditions) {
        if (conditions == null || conditions.length == 0) {
            throw new IllegalArgumentException("At least one condition is required");
        }
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < conditions.length; i++) {
            if (i > 0) {
                result.append(" AND ");
            }
            result.append("(").append(conditions[i]).append(")");
        }
        return result.toString();
    }

    /**
     * Combines multiple conditions with OR logic.
     *
     * @param conditions the conditions to combine
     * @return combined SQL condition string
     *
     * @example
     * <pre>
     * or("owner_id = 1", "is_public = true")
     * // Returns: "(owner_id = 1) OR (is_public = true)"
     * </pre>
     */
    public static String or(String... conditions) {
        if (conditions == null || conditions.length == 0) {
            throw new IllegalArgumentException("At least one condition is required");
        }
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < conditions.length; i++) {
            if (i > 0) {
                result.append(" OR ");
            }
            result.append("(").append(conditions[i]).append(")");
        }
        return result.toString();
    }

    /**
     * Validates that input parameters are not null or empty.
     *
     * @param inputs the inputs to validate
     * @throws IllegalArgumentException if any input is null or empty
     */
    private static void validateInputs(String... inputs) {
        for (String input : inputs) {
            if (input == null || input.trim().isEmpty()) {
                throw new IllegalArgumentException("Input parameters cannot be null or empty");
            }
        }
    }
}
