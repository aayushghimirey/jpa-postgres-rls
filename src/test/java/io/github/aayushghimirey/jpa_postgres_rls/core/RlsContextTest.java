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
package io.github.aayushghimirey.jpa_postgres_rls.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCallback;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.sql.PreparedStatement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class RlsContextTest {

    private RlsContext rlsContext;

    @Mock
    private JdbcTemplate jdbcTemplate;

    @Mock
    private PreparedStatement preparedStatement;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        rlsContext = new RlsContext(jdbcTemplate);
        when(jdbcTemplate.execute(any(String.class), any(PreparedStatementCallback.class)))
                .thenAnswer(invocation -> null);
    }

    @Test
    void shouldThrowIfNoTransactionActive() {
        assertThrows(IllegalStateException.class, () -> rlsContext.with("test", "val").apply());
    }

    @Test
    void shouldApplyVariablesUsingSetContext() throws Exception {
        // Mock active transaction
        TransactionSynchronizationManager.setActualTransactionActive(true);
        try {
            rlsContext.with("tenant_id", "123").apply();

            // Capture the SQL and PreparedStatementCallback
            ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
            verify(jdbcTemplate).execute(sqlCaptor.capture(), any(PreparedStatementCallback.class));

            assertEquals("SELECT set_config('app.tenant_id', ?, true)", sqlCaptor.getValue());
        } finally {
            TransactionSynchronizationManager.setActualTransactionActive(false);
        }
    }

    @Test
    void shouldPrefixWithAppIfNotPresent() {
        TransactionSynchronizationManager.setActualTransactionActive(true);
        try {
            rlsContext.with("tenant_id", "123").apply();
            verify(jdbcTemplate).execute(contains("app.tenant_id"), any(PreparedStatementCallback.class));
        } finally {
            TransactionSynchronizationManager.setActualTransactionActive(false);
        }
    }

    @Test
    void shouldNotDoublePrefixIfAppPresent() {
        TransactionSynchronizationManager.setActualTransactionActive(true);
        try {
            rlsContext.with("app.tenant_id", "123").apply();
            verify(jdbcTemplate).execute(eq("SELECT set_config('app.tenant_id', ?, true)"), any(PreparedStatementCallback.class));
        } finally {
            TransactionSynchronizationManager.setActualTransactionActive(false);
        }
    }
}
