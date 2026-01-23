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

import io.github.AyushGhimire077.hibernate_postgres_rls.exception.RlsSecurityException;

import java.util.regex.Pattern;

/**
 * Utility class for validating and quoting SQL identifiers and session variables.
 * <p>
 * This class helps prevent SQL injection by ensuring that all identifiers
 * passed to dynamic DDL or session commands (SET CONFIG) are safe and
 * follow PostgreSQL naming rules.
 * </p>
 *
 * @author Aayush Ghimire
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
}
