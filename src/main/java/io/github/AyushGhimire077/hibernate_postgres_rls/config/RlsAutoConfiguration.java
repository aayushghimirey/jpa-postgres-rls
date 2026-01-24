package io.github.AyushGhimire077.hibernate_postgres_rls.config;

import io.github.AyushGhimire077.hibernate_postgres_rls.annotation.RlsRule;
import io.github.AyushGhimire077.hibernate_postgres_rls.aspect.RlsTransactionalAspect;
import io.github.AyushGhimire077.hibernate_postgres_rls.core.RlsAnnotationScanner;
import io.github.AyushGhimire077.hibernate_postgres_rls.core.RlsContext;
import io.github.AyushGhimire077.hibernate_postgres_rls.core.StartupValidation;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.metamodel.EntityType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.*;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Autoconfiguration for PostgreSQL Row Level Security (RLS).
 */
@Configuration
@ConditionalOnProperty(prefix = "spring.rls", name = "enabled", havingValue = "true")
@ConditionalOnClass(EntityManagerFactory.class)
public class RlsAutoConfiguration {

    private static final Logger log = LoggerFactory.getLogger(RlsAutoConfiguration.class);


    /**
     * Core RLS context bean for managing PostgreSQL session variables.
     * <h5>Depends on <b>JdbcTemplate</b> being present.</h5>
     *
     */
    @Bean
    @ConditionalOnBean(JdbcTemplate.class)
    @ConditionalOnMissingBean
    public RlsContext rlsContext(JdbcTemplate jdbcTemplate) {
        return new RlsContext(jdbcTemplate);
    }


    /**
     * Transactional aspect to bind RLS session variables from method parameters.
     * <h5>Depends on <b>RlsContext</b> being present
     *
     */
    @Bean
    @ConditionalOnBean(RlsContext.class)
    @ConditionalOnMissingBean
    public RlsTransactionalAspect rlsTransactionalAspect(RlsContext rlsContext) {
        return new RlsTransactionalAspect(rlsContext);
    }


    /**
     * Startup validation bean to verify RLS rules against the database.
     * <h5>Depends on <b>DataSource</b> being present.</h5>
     *
     */
    @Bean
    @ConditionalOnBean(DataSource.class)
    @ConditionalOnMissingBean
    public StartupValidation startupValidation(DataSource dataSource) {
        return new StartupValidation(dataSource);
    }


    @EventListener(ApplicationReadyEvent.class)
    @ConditionalOnBean(StartupValidation.class)
    public void validateRls(ApplicationReadyEvent event) {

        log.info("RLS auto-configuration: running startup validation");

        EntityManagerFactory emf =
                event.getApplicationContext().getBean(EntityManagerFactory.class);

        StartupValidation validation =
                event.getApplicationContext().getBean(StartupValidation.class);

        Set<Class<?>> entities = emf.getMetamodel()
                .getEntities()
                .stream()
                .map(EntityType::getJavaType)
                .collect(Collectors.toSet());

        List<RlsRule> rules = RlsAnnotationScanner.scanEntitiesForRlsRules(entities);

        /*
         * Validate all the rules found
         * */
        validation.validate(rules);
    }
}
