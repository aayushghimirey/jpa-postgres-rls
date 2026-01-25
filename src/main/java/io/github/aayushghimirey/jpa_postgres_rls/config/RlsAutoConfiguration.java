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

package io.github.aayushghimirey.jpa_postgres_rls.config;

import io.github.aayushghimirey.jpa_postgres_rls.annotation.RlsRule;
import io.github.aayushghimirey.jpa_postgres_rls.aspect.RlsTransactionalAspect;
import io.github.aayushghimirey.jpa_postgres_rls.core.RlsAnnotationScanner;
import io.github.aayushghimirey.jpa_postgres_rls.core.RlsContext;
import io.github.aayushghimirey.jpa_postgres_rls.core.StartupValidation;
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
 * Auto-configuration for PostgreSQL Row-Level Security (RLS) integration with Hibernate.
 * <p>
 * Activates when the property <code>spring.rls.enabled=true</code> is set in the application configuration
 * and JPA is available in the classpath.
 * </p>
 * <p>
 * Provides the following beans:
 * <ul>
 *     <li>{@link RlsContext} - manages session variables for RLS enforcement.</li>
 *     <li>{@link RlsTransactionalAspect} - binds RLS session variables from method parameters.</li>
 *     <li>{@link StartupValidation} - validates RLS policies and rules on application startup.</li>
 * </ul>
 * </p>
 * <p>
 * Also performs startup validation by scanning all JPA entities for {@link RlsRule} annotations
 * and verifying them against the database.
 * </p>
 *
 * @author Aayush Ghimire
 * @since 2026
 */
@Configuration
@ConditionalOnProperty(prefix = "spring.rls", name = "enabled", havingValue = "true")
@ConditionalOnClass(EntityManagerFactory.class)
public class RlsAutoConfiguration {

    private static final Logger log = LoggerFactory.getLogger(RlsAutoConfiguration.class);

    /**
     * Core RLS context bean for managing PostgreSQL session variables.
     * <p>Requires a {@link JdbcTemplate} bean.</p>
     *
     * @param jdbcTemplate Spring JDBC template
     * @return a new {@link RlsContext} instance
     */
    @Bean
    @ConditionalOnMissingBean
    public RlsContext rlsContext(JdbcTemplate jdbcTemplate) {
        return new RlsContext(jdbcTemplate);
    }

    /**
     * Transactional aspect to bind RLS session variables from method parameters.
     * <p>Requires {@link RlsContext} bean.</p>
     *
     * @param rlsContext RLS context
     * @return a new {@link RlsTransactionalAspect} instance
     */
    @Bean
    @ConditionalOnMissingBean
    public RlsTransactionalAspect rlsTransactionalAspect(RlsContext rlsContext) {
        return new RlsTransactionalAspect(rlsContext);
    }

    /**
     * Startup validation bean to verify RLS rules against the database.
     * <p>Requires {@link DataSource} bean.</p>
     *
     * @param dataSource application datasource
     * @return a new {@link StartupValidation} instance
     */
    @Bean
    @ConditionalOnMissingBean
    public StartupValidation startupValidation(DataSource dataSource) {
        return new StartupValidation(dataSource);
    }

    /**
     * Event listener that performs RLS startup validation when the application is ready.
     * <p>Scans all JPA entities for {@link RlsRule} annotations and validates them.</p>
     *
     * @param event the application ready event
     */
    @EventListener(ApplicationReadyEvent.class)
    public void validateRls(ApplicationReadyEvent event) {
        StartupValidation validation = event.getApplicationContext().getBean(StartupValidation.class);
        EntityManagerFactory emf = event.getApplicationContext().getBean(EntityManagerFactory.class);

        Set<Class<?>> entities = emf.getMetamodel()
                .getEntities()
                .stream()
                .map(EntityType::getJavaType)
                .collect(Collectors.toSet());

        List<RlsRule> rules = RlsAnnotationScanner.scanEntitiesForRlsRules(entities);
        validation.validate(rules);
    }
}
