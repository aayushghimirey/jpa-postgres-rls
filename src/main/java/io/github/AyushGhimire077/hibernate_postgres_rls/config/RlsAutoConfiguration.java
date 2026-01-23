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

package io.github.AyushGhimire077.hibernate_postgres_rls.config;

import io.github.AyushGhimire077.hibernate_postgres_rls.aspect.RlsTransactionalAspect;
import io.github.AyushGhimire077.hibernate_postgres_rls.filter.RlsFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import io.github.AyushGhimire077.hibernate_postgres_rls.core.PostgreSqlRlsUserManager;
import io.github.AyushGhimire077.hibernate_postgres_rls.users.RlsUserManager;


/**
 * Auto-configuration class for Row Level Security (RLS) in PostgreSQL.
 */
@Configuration
@ConditionalOnProperty(prefix = "spring.rls", name = "enabled", havingValue = "true")
@EnableConfigurationProperties(RlsProperties.class)
public class RlsAutoConfiguration {

    private static final Logger log = LoggerFactory.getLogger(RlsAutoConfiguration.class);

    @Bean
    public RlsUserManager rlsUserManager(JdbcTemplate jdbcTemplate) {
        return new PostgreSqlRlsUserManager(jdbcTemplate);
    }

    @Bean
    public RlsFilter rlsFilter() {
        return new RlsFilter();
    }

    @Bean
    public RlsTransactionalAspect rlsTransactionalAspect(JdbcTemplate jdbcTemplate) {
        return new RlsTransactionalAspect(jdbcTemplate);
    }
}
