package io.github.AyushGhimire077.hibernate_postgres_rls.filter;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.filter.OncePerRequestFilter;

import io.github.AyushGhimire077.hibernate_postgres_rls.aspect.ClientContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class RlsFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RlsFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {


        ClientContext.clear();

        log.info("Request hit ----------------------------------------------");

        try {
            String tenantId = request.getHeader("X-Tenant-Id");
            log.info("Tenant id found ------------------------- {}", tenantId);
            if (tenantId != null) {
                ClientContext.put("tenant_id", tenantId);
            }
            filterChain.doFilter(request, response);
        } finally {
            // Cleanup: after request
            ClientContext.clear();
            log.trace("RLS Context cleared after request");
        }
    }
}
