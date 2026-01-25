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
    @ValueSource(strings = {"public", "custom_schema", "Internal"})
    void shouldAcceptValidSchemas(String schema) {
        assertDoesNotThrow(() -> SqlIdentifierValidator.validateSchema(schema));
    }

    @ParameterizedTest
    @ValueSource(strings = {"123schema", "schema-name", "schema name"})
    void shouldRejectInvalidSchemas(String schema) {
        assertThrows(RlsSecurityException.class, () -> SqlIdentifierValidator.validateSchema(schema));
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
