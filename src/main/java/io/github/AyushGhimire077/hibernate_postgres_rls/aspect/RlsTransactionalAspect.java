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
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

@Aspect
public class RlsTransactionalAspect {

    private static final Logger log =
            LoggerFactory.getLogger(RlsTransactionalAspect.class);

    private final JdbcTemplate jdbcTemplate;

    public RlsTransactionalAspect(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }


    @Before("@annotation(org.springframework.transaction.annotation.Transactional)")
    public void beforeTransactionalMethod() {

        Map<String, String> ctx = ClientContext.getAll();
        if (ctx == null || ctx.isEmpty()) return;


        log.debug("Transactional method detected → applying RLS context");
        ctx.forEach((key, value) -> {
            // ClientContext.put() already validates, so we can trust the values here
            String sessionKey = key.startsWith("app.") ? key : "app." + key;
            
            try {
                jdbcTemplate.queryForObject(
                        "SELECT set_config(?, ?, true)",
                        String.class,
                        sessionKey,
                        value
                );
                log.trace("Set RLS config: {} = {}", sessionKey, value);
            } catch (Exception e) {
                log.error("Failed to set RLS config for key: {}", sessionKey, e);
                throw new RlsSecurityException("Failed to set RLS session config for key: " + sessionKey, e);
            }
        });
    }
}
