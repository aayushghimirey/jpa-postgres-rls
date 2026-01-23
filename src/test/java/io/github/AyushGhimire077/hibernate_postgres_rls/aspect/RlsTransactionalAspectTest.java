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

import io.github.AyushGhimire077.hibernate_postgres_rls.exception.RlsSecurityException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class RlsTransactionalAspectTest {

    private RlsTransactionalAspect aspect;

    @Mock
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        aspect = new RlsTransactionalAspect(jdbcTemplate);
        ClientContext.clear();
    }

    @AfterEach
    void tearDown() {
        ClientContext.clear();
    }

    @Test
    void shouldApplyContextToDatabase() {
        ClientContext.put("tenant_id", "123");
        
        aspect.beforeTransactionalMethod();

        verify(jdbcTemplate).queryForObject(
                eq("SELECT set_config(?, ?, true)"),
                eq(String.class),
                eq("app.tenant_id"),
                eq("123")
        );
    }

    @Test
    void shouldHandleExistingAppPrefix() {
        ClientContext.put("app.tenant_id", "123");
        
        aspect.beforeTransactionalMethod();

        verify(jdbcTemplate).queryForObject(
                anyString(),
                eq(String.class),
                eq("app.tenant_id"),
                eq("123")
        );
    }

    @Test
    void shouldSkipIfContextEmpty() {
        aspect.beforeTransactionalMethod();
        verify(jdbcTemplate, never()).queryForObject(anyString(), eq(String.class), anyString(), anyString());
    }

    @Test
    void shouldThrowSecurityExceptionOnDatabaseError() {
        ClientContext.put("key", "val");
        doThrow(new RuntimeException("DB Error")).when(jdbcTemplate)
                .queryForObject(anyString(), eq(String.class), anyString(), anyString());

        assertThrows(RlsSecurityException.class, () -> aspect.beforeTransactionalMethod());
    }
}
