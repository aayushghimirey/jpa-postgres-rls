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

package io.github.AyushGhimire077.hibernate_postgres_rls.filter;

import io.github.AyushGhimire077.hibernate_postgres_rls.aspect.ClientContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RlsFilterTest {

    private RlsFilter rlsFilter;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        rlsFilter = new RlsFilter();
        ClientContext.clear();
    }

    @AfterEach
    void tearDown() {
        ClientContext.clear();
    }

    @Test
    void shouldExtractHeadersAndSetContext() throws ServletException, IOException {
        when(request.getHeader("X-Tenant-Id")).thenReturn("tenant-123");
        when(request.getHeader("X-Policy-Key")).thenReturn("tenant_id");

        // Verify context is set during filter execution
        doAnswer(invocation -> {
            assertEquals("tenant-123", ClientContext.get("tenant_id"));
            return null;
        }).when(filterChain).doFilter(request, response);

        rlsFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        // Verify context is cleared after request
        assertTrue(ClientContext.getAll().isEmpty());
    }

    @Test
    void shouldHandleMissingHeaders() throws ServletException, IOException {
        when(request.getHeader("X-Tenant-Id")).thenReturn(null);
        when(request.getHeader("X-Policy-Key")).thenReturn(null);

        rlsFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertTrue(ClientContext.getAll().isEmpty());
    }

    @Test
    void shouldHandleInvalidHeadersGracefully() throws ServletException, IOException {
        when(request.getHeader("X-Tenant-Id")).thenReturn("invalid; injection");
        when(request.getHeader("X-Policy-Key")).thenReturn("tenant_id");

        rlsFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertTrue(ClientContext.getAll().isEmpty()); // Should not set context if invalid
    }

    @Test
    void shouldClearExistingContextBeforeProcessing() throws ServletException, IOException {
        ClientContext.put("old_key", "old_value");
        
        when(request.getHeader("X-Tenant-Id")).thenReturn(null);
        
        doAnswer(invocation -> {
            assertTrue(ClientContext.getAll().isEmpty());
            return null;
        }).when(filterChain).doFilter(request, response);

        rlsFilter.doFilterInternal(request, response, filterChain);
    }
}
