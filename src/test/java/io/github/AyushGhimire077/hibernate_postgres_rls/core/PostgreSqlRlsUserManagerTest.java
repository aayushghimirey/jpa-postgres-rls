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

package io.github.AyushGhimire077.hibernate_postgres_rls.core;

import io.github.AyushGhimire077.hibernate_postgres_rls.exception.RlsPolicyException;
import io.github.AyushGhimire077.hibernate_postgres_rls.exception.RlsSecurityException;
import io.github.AyushGhimire077.hibernate_postgres_rls.users.RlsUserManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class PostgreSqlRlsUserManagerTest {

    private PostgreSqlRlsUserManager userManager;

    @Mock
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        userManager = new PostgreSqlRlsUserManager(jdbcTemplate);
    }

    @Test
    void shouldCreateTenantWithAllPermissions() {
        userManager.createTenant("tenant1", "password123", "public");

        // Verify role creation
        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        verify(jdbcTemplate, atLeastOnce()).execute(sqlCaptor.capture());
        
        List<String> allSql = sqlCaptor.getAllValues();
        // Check for role creation in EXECUTE block
        assertTrue(allSql.stream().anyMatch(sql -> sql.contains("CREATE ROLE \"tenant1\"")));
        assertTrue(allSql.stream().anyMatch(sql -> sql.contains("GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA \"public\" TO \"tenant1\"")));
    }

    @Test
    void shouldCreateTenantWithoutAutoGrant() {
        userManager.createTenant("tenant1", "password123", "public", false);

        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        verify(jdbcTemplate, atLeastOnce()).execute(sqlCaptor.capture());
        
        List<String> allSql = sqlCaptor.getAllValues();
        assertTrue(allSql.stream().anyMatch(sql -> sql.contains("CREATE ROLE \"tenant1\"")));
        assertFalse(allSql.stream().anyMatch(sql -> sql.contains("GRANT ALL PRIVILEGES")));
    }

    @Test
    void shouldGrantSpecificPermissions() {
        userManager.grantPermissions("tenant1", "public", 
                Collections.singletonList(RlsUserManager.RlsPermission.SELECT));

        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        verify(jdbcTemplate, times(3)).execute(sqlCaptor.capture());
        
        List<String> allSql = sqlCaptor.getAllValues();
        assertTrue(allSql.stream().anyMatch(sql -> sql.contains("GRANT SELECT ON ALL TABLES IN SCHEMA \"public\" TO \"tenant1\"")));
    }

    @Test
    void shouldDropTenant() {
        userManager.dropTenant("tenant1");

        verify(jdbcTemplate).execute(contains("DROP ROLE IF EXISTS \"tenant1\""));
    }

    @Test
    void shouldSetActiveRole() {
        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class), eq("tenant1"))).thenReturn(1);
        
        userManager.setActiveRole("tenant1");

        verify(jdbcTemplate).execute(eq("SET ROLE \"tenant1\""));
    }

    @Test
    void shouldNotSetActiveRoleIfNonExistent() {
        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class), eq("tenant1"))).thenReturn(0);
        
        userManager.setActiveRole("tenant1");

        verify(jdbcTemplate, never()).execute(startsWith("SET ROLE"));
    }

    @Test
    void shouldRejectInvalidUsername() {
        assertThrows(RlsSecurityException.class, () -> 
                userManager.createTenant("invalid; username", "password", "public"));
    }

    @Test
    void shouldHandleDatabaseErrorsGracefully() {
        doThrow(new RuntimeException("DB Error")).when(jdbcTemplate).execute(anyString());

        assertThrows(RlsPolicyException.class, () -> userManager.dropTenant("tenant1"));
    }
}
