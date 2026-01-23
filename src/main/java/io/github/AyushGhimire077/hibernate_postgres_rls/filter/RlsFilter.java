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
import io.github.AyushGhimire077.hibernate_postgres_rls.exception.RlsSecurityException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filter that sets PostgreSQL RLS session variables based on request headers.
 * <p>
 * This filter extracts RLS-related headers from incoming HTTP requests
 * and populates the {@link ClientContext}. It ensures that the context is
 * cleared after the request is processed to prevent data leakage in connection pools.
 * </p>
 *
 * <h3>Work Flow:</h3>
 * <ol>
 *     <li>Clears any existing context for the thread.</li>
 *     <li>Extracts {@code X-Tenant-Id} and {@code X-Policy-Key} headers.</li>
 *     <li>Validates and populates the {@link ClientContext}.</li>
 *     <li>Proceeds with the filter chain.</li>
 *     <li>Finally, clears the context.</li>
 * </ol>
 *
 * @author Aayush Ghimire
 */
public class RlsFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RlsFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // Always start with a clean context
        ClientContext.clear();

        String tenantId = request.getHeader("X-Tenant-Id");
        String policyKey = request.getHeader("X-Policy-Key");

        log.trace("RLS Filter processing request: {}. Headers: X-Tenant-Id={}, X-Policy-Key={}",
                request.getRequestURI(), tenantId, policyKey);

        try {
            if (tenantId != null && policyKey != null) {
                try {
                    ClientContext.put(policyKey, tenantId);
                    log.debug("RLS context set for request: {} = {}", policyKey, tenantId);
                } catch (RlsSecurityException e) {
                    log.warn("Invalid RLS headers detected in request. TenantId: {}, PolicyKey: {}. Error: {}",
                            tenantId, policyKey, e.getMessage());

                }
            }

            filterChain.doFilter(request, response);

        } finally {
            ClientContext.clear(); // clean
            log.trace("RLS Context cleared after request: {}", request.getRequestURI());
        }
    }
}
