/*
 * Copyright (C) 2026 Aayush Ghimire
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.aayushghimirey.jpa_postgres_rls.util;

import io.github.aayushghimirey.jpa_postgres_rls.exception.RlsSecurityException;

import java.util.regex.Pattern;

/**
 * Utility for validating SQL identifiers and session variables to prevent injection.
 */
public final class SqlIdentifierValidator {

    // PostgreSQL identifiers can contain letters, underscores, and digits (not at start)
    // and must be <= 63 bytes by default.
    private static final Pattern IDENTIFIER_PATTERN = Pattern.compile("^[a-zA-Z_][a-zA-Z0-9_$]*$");

    // Session values for set_config should be safe (alphanumeric, hyphens, underscores)
    private static final Pattern SESSION_VALUE_PATTERN = Pattern.compile("^[a-zA-Z0-9_\\-]+$");

    private SqlIdentifierValidator() {
        // Utility class
    }

    /**
     * Validates that the given string is a safe PostgreSQL identifier.
     *
     * @param identifier the identifier to validate
     * @param fieldName  the name of the field for error reporting
     * @throws RlsSecurityException if the identifier is invalid or potentially unsafe
     */
    public static void validateIdentifier(String identifier, String fieldName) {
        if (identifier == null || identifier.trim().isEmpty()) {
            throw new RlsSecurityException(fieldName + " cannot be null or empty");
        }

        if (identifier.length() > 63) {
            throw new RlsSecurityException(fieldName + " is too long (max 63 characters): " + identifier);
        }

        if (!IDENTIFIER_PATTERN.matcher(identifier).matches()) {
            throw new RlsSecurityException(fieldName + " contains invalid characters: " + identifier);
        }

        // Check for reserved keywords if necessary, though quoting usually handles this
        // but since we are generating DDL, we want to be extra strict.
    }

    /**
     * Checks if the given string is a potentially valid identifier without throwing exception.
     */
    public static boolean isValidIdentifier(String identifier) {
        if (identifier == null || identifier.isEmpty() || identifier.length() > 63) {
            return false;
        }
        return IDENTIFIER_PATTERN.matcher(identifier).matches();
    }

    /**
     * Quotes a PostgreSQL identifier if it's not already quoted.
     *
     * @param identifier the identifier to quote
     * @return the quoted identifier
     */
    public static String quoteIdentifier(String identifier) {
        if (identifier == null) return null;
        if (identifier.startsWith("\"") && identifier.endsWith("\"")) return identifier;

        // Escape existing double quotes by doubling them
        return "\"" + identifier.replace("\"", "\"\"") + "\"";
    }

    /**
     * Validates that the given string is a safe PostgreSQL table name.
     */
    public static void validateTable(String tableName) {
        validateIdentifier(tableName, "Table name");
    }

    /**
     * Validates that the given string is a safe PostgreSQL schema name.
     */
    public static void validateSchema(String schemaName) {
        validateIdentifier(schemaName, "Schema name");
    }

    /**
     * Validates a session key (e.g., 'tenant_id') for use in set_config.
     */
    public static void validateSessionKey(String key) {
        if (key == null || !key.matches("^[a-zA-Z0-9_.]+$")) {
            throw new RlsSecurityException("Session key contains invalid characters. Only alphanumeric, underscores, and dots allowed: " + key);
        }
    }

    /**
     * Validates a session value (e.g., '123') for use in set_config.
     */
    public static void validateSessionValue(String value) {
        if (value == null || !SESSION_VALUE_PATTERN.matcher(value).matches()) {
            throw new RlsSecurityException("Session value contains invalid characters: " + value);
        }
    }

    /**
     * Validates a policy name for use in RLS definitions.
     */
    public static void validatePolicy(String policyName) {
        validateIdentifier(policyName, "Policy name");
    }
}
