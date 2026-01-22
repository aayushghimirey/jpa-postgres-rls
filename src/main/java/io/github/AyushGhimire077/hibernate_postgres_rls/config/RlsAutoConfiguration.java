package io.github.AyushGhimire077.hibernate_postgres_rls.config;

import io.github.AyushGhimire077.hibernate_postgres_rls.filter.RlsFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import io.github.AyushGhimire077.hibernate_postgres_rls.core.PostgreSqlRlsUserManager;
import io.github.AyushGhimire077.hibernate_postgres_rls.users.RlsUserManager;

@Configuration
@ConditionalOnProperty(prefix = "spring.rls", name = "enabled", havingValue = "true")
public class RlsAutoConfiguration {

    private static final Logger log = LoggerFactory.getLogger(RlsAutoConfiguration.class);

    @Bean
    @ConfigurationProperties(prefix = "spring.rls")
    public RlsProperties rlsProperties() {
        return new RlsProperties();
    }

    @Bean
    public RlsUserManager rlsUserManager(JdbcTemplate jdbcTemplate) {
        return new PostgreSqlRlsUserManager(jdbcTemplate);
    }

    @Bean
    public RlsFilter rlsFilter() {
        return new RlsFilter();
    }
}
