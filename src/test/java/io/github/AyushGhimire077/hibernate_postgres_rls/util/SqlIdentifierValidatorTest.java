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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class SqlIdentifierValidatorTest {

    @ParameterizedTest
    @ValueSource(strings = {"users", "user_profiles", "_internal_table", "table$123", "Product"})
    void shouldAcceptValidIdentifiers(String identifier) {
        assertDoesNotThrow(() -> SqlIdentifierValidator.validateIdentifier(identifier, "test"));
        assertTrue(SqlIdentifierValidator.isValidIdentifier(identifier));
    }

    @ParameterizedTest
    @ValueSource(strings = {"123table", "table-name", "table name", "table;DROP", "table--", "table/*", "table\""})
    void shouldRejectInvalidIdentifiers(String identifier) {
        assertThrows(RlsSecurityException.class, () -> SqlIdentifierValidator.validateIdentifier(identifier, "test"));
        assertFalse(SqlIdentifierValidator.isValidIdentifier(identifier));
    }

    @Test
    void shouldRejectTooLongIdentifiers() {
        String longIdentifier = "a".repeat(64);
        assertThrows(RlsSecurityException.class, () -> SqlIdentifierValidator.validateIdentifier(longIdentifier, "test"));
    }

    @Test
    void shouldRejectNullOrEmptyIdentifiers() {
        assertThrows(RlsSecurityException.class, () -> SqlIdentifierValidator.validateIdentifier(null, "test"));
        assertThrows(RlsSecurityException.class, () -> SqlIdentifierValidator.validateIdentifier("", "test"));
        assertThrows(RlsSecurityException.class, () -> SqlIdentifierValidator.validateIdentifier("   ", "test"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"tenant_id", "USER_ID", "key123", "tenant.id", "app.tenant_id"})
    void shouldAcceptValidSessionKeys(String key) {
        assertDoesNotThrow(() -> SqlIdentifierValidator.validateSessionKey(key));
    }

    @ParameterizedTest
    @ValueSource(strings = {"tenant-id", "tenant id", "key$123"})
    void shouldRejectInvalidSessionKeys(String key) {
        assertThrows(RlsSecurityException.class, () -> SqlIdentifierValidator.validateSessionKey(key));
    }

    @ParameterizedTest
    @ValueSource(strings = {"tenant-123", "550e8400-e29b-41d4-a716-446655440000", "user_1"})
    void shouldAcceptValidSessionValues(String value) {
        assertDoesNotThrow(() -> SqlIdentifierValidator.validateSessionValue(value));
    }

    @ParameterizedTest
    @ValueSource(strings = {"value with space", "value;DROP", "value'OR'1'='1"})
    void shouldRejectInvalidSessionValues(String value) {
        assertThrows(RlsSecurityException.class, () -> SqlIdentifierValidator.validateSessionValue(value));
    }

    @Test
    void shouldQuoteIdentifiers() {
        assertEquals("\"users\"", SqlIdentifierValidator.quoteIdentifier("users"));
        assertEquals("\"UserTable\"", SqlIdentifierValidator.quoteIdentifier("UserTable"));
        assertEquals("\"quoted\"\"table\"", SqlIdentifierValidator.quoteIdentifier("quoted\"table"));
    }
}
