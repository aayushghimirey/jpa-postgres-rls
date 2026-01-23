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

import io.github.AyushGhimire077.hibernate_postgres_rls.annotation.RlsRule;
import io.github.AyushGhimire077.hibernate_postgres_rls.annotation.RlsRules;
import io.github.AyushGhimire077.hibernate_postgres_rls.annotation.RowLevelSecurity;
import io.github.AyushGhimire077.hibernate_postgres_rls.exception.RlsConfigurationException;
import io.github.AyushGhimire077.hibernate_postgres_rls.util.SqlIdentifierValidator;
import org.hibernate.boot.ResourceStreamLocator;
import org.hibernate.boot.model.relational.AbstractAuxiliaryDatabaseObject;
import org.hibernate.boot.model.relational.SqlStringGenerationContext;
import org.hibernate.boot.spi.AdditionalMappingContributions;
import org.hibernate.boot.spi.AdditionalMappingContributor;
import org.hibernate.boot.spi.InFlightMetadataCollector;
import org.hibernate.boot.spi.MetadataBuildingContext;
import org.hibernate.engine.config.spi.ConfigurationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Hibernate {@link AdditionalMappingContributor} that generates PostgreSQL RLS DDL.
 * <p>
 * This class scans entities for {@link RowLevelSecurity} and {@link RlsRule} annotations
 * and registers auxiliary database objects to enable RLS and create policies.
 * </p>
 *
 * @author Aayush Ghimire
 */
class RlsSchemaContributor implements AdditionalMappingContributor {

    private static final Logger log = LoggerFactory.getLogger(RlsSchemaContributor.class);

    @Override
    public void contribute(AdditionalMappingContributions contributions, InFlightMetadataCollector collector,
                           ResourceStreamLocator locator, MetadataBuildingContext context) {

        log.info(">>> RLS Contributor: Initializing RLS Mapping Contributor");

        var settings = context.getBootstrapContext().getServiceRegistry()
                .getService(ConfigurationService.class)
                .getSettings();

        Object enabledProp = settings.getOrDefault("spring.rls.enabled",
                settings.get("spring.jpa.properties.spring.rls.enabled"));

        log.info(">>> RLS Contributor: RLS enabled property value: {}", enabledProp);

        boolean isEnabled = "true".equals(String.valueOf(enabledProp));

        if (!isEnabled) {
            log.info(">>> RLS Contributor: RLS disabled in properties, skipping");
            return;
        }

        Object modeProp = settings.getOrDefault("spring.rls.mode",
                settings.get("spring.jpa.properties.spring.rls.mode"));
        String modeStr = String.valueOf(modeProp).toUpperCase();
        
        if ("VALIDATE".equals(modeStr)) {
            log.info(">>> RLS Contributor: RLS mode is VALIDATE, skipping DDL generation");
            return;
        }

        collector.getEntityBindings().forEach(binding -> {
            Class<?> entityClass = binding.getMappedClass();

            if (entityClass == null || !entityClass.isAnnotationPresent(RowLevelSecurity.class)) {
                return;
            }

            log.debug(">>> RLS Contributor: Processing entity {}", entityClass.getName());

            // Extract all RLS rules
            List<RlsRule> rules = new ArrayList<>();
            if (entityClass.isAnnotationPresent(RlsRules.class)) {
                rules.addAll(Arrays.asList(entityClass.getAnnotation(RlsRules.class).value()));
            } else if (entityClass.isAnnotationPresent(RlsRule.class)) {
                rules.add(entityClass.getAnnotation(RlsRule.class));
            }

            if (rules.isEmpty()) {
                throw new RlsConfigurationException(
                        "Entity " + entityClass.getName() + " is marked @RowLevelSecurity but missing @RlsRule");
            }

            boolean force = entityClass.getAnnotation(RowLevelSecurity.class).force();
            String tableName = binding.getTable().getName();
            String qualifiedTableName = binding.getTable().getQualifiedTableName().render();

            collector.addAuxiliaryDatabaseObject(new AbstractAuxiliaryDatabaseObject() {
                @Override
                public String[] sqlCreateStrings(SqlStringGenerationContext ctx) {
                    List<String> sql = new ArrayList<>();
                    
                    // 1. Enable RLS
                    sql.add("ALTER TABLE " + qualifiedTableName + " ENABLE ROW LEVEL SECURITY");
                    if (force) {
                        sql.add("ALTER TABLE " + qualifiedTableName + " FORCE ROW LEVEL SECURITY");
                    }

                    // 2. Create Policies
                    for (RlsRule rule : rules) {
                        String policyName = rule.name().isEmpty()
                                ? "rls_policy_" + tableName + "_" + rule.policyType().name().toLowerCase()
                                : rule.name();
                        
                        // Validate policy name to prevent injection
                        SqlIdentifierValidator.validateIdentifier(policyName, "policy name");
                        
                        String policyType = rule.policyType().name();
                        String usingClause = rule.using();
                        String withCheckClause = rule.withCheck();

                        StringBuilder createPolicySQL = new StringBuilder();
                        createPolicySQL.append("CREATE POLICY ").append(SqlIdentifierValidator.quoteIdentifier(policyName))
                                .append(" ON ").append(qualifiedTableName)
                                .append(" FOR ").append(policyType);

                        if (!usingClause.isEmpty()) {
                            createPolicySQL.append(" USING (").append(usingClause).append(")");
                        }
                        
                        if (!withCheckClause.isEmpty()) {
                            createPolicySQL.append(" WITH CHECK (").append(withCheckClause).append(")");
                        }

                        log.info(">>> RLS Contributor: Registering policy: {}", createPolicySQL);
                        sql.add(createPolicySQL.toString());
                    }

                    return sql.toArray(new String[0]);
                }

                @Override
                public String[] sqlDropStrings(SqlStringGenerationContext ctx) {
                    List<String> sql = new ArrayList<>();
                    for (RlsRule rule : rules) {
                        String policyName = rule.name().isEmpty()
                                ? "rls_policy_" + tableName + "_" + rule.policyType().name().toLowerCase()
                                : rule.name();
                        
                        sql.add(String.format("DROP POLICY IF EXISTS %s ON %s", 
                                SqlIdentifierValidator.quoteIdentifier(policyName), 
                                qualifiedTableName));
                    }
                    return sql.toArray(new String[0]);
                }
            });

            log.info(">>> RLS Contributor: RLS policies registered for {}", tableName);
        });
    }
}
