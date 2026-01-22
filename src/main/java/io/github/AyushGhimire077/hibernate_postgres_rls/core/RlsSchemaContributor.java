package io.github.AyushGhimire077.hibernate_postgres_rls.core;

import java.util.ArrayList;
import java.util.List;

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

import io.github.AyushGhimire077.hibernate_postgres_rls.annotation.RlsRule;
import io.github.AyushGhimire077.hibernate_postgres_rls.annotation.RowLevelSecurity;

public class RlsSchemaContributor implements AdditionalMappingContributor {

    private static final Logger log = LoggerFactory.getLogger(RlsSchemaContributor.class);

    @Override
    public void contribute(AdditionalMappingContributions contributions, InFlightMetadataCollector collector,
                           ResourceStreamLocator locator, MetadataBuildingContext context) {

        log.info(">>> RLS Contributor: Initialising RLS Mapping Contributor");

        var settings = context.getBootstrapContext().getServiceRegistry()
                .getService(ConfigurationService.class)
                .getSettings();

        Object enabledProp = settings.getOrDefault("spring.rls.enabled",
                settings.get("spring.jpa.properties.spring.rls.enabled"));

        String mode = (String) settings.getOrDefault("spring.rls.mode",
                settings.get("spring.jpa.properties.spring.rls.mode"));

        log.info(">>> RLS Contributor: RLS enabled property value: {}", enabledProp);

        boolean isEnabled = "true".equals(String.valueOf(enabledProp));

        if (!isEnabled) {
            log.info(">>> RLS Contributor: RLS disabled in properties, skipping");
            return;
        }

        collector.getEntityBindings().forEach(binding -> {

            Class<?> entityClass = binding.getMappedClass();

            if (entityClass == null)
                return;

            if (!entityClass.isAnnotationPresent(RowLevelSecurity.class))
                return;

            RlsRule policy = entityClass.getAnnotation(RlsRule.class);
            if (policy == null) {
                throw new IllegalStateException(
                        "Entity " + entityClass.getName() + " is @RowLevelSecurity but missing @RlsRule");
            }

            String tableName = binding.getTable().getQuotedName();
            String column = policy.column();
            String sessionKey = policy.sessionKey();
            String qualifiedKey = sessionKey.contains(".") ? sessionKey : "app." + sessionKey;
            String policyType = policy.policyType().name();
            String castType = policy.castType();

            boolean force = entityClass
                    .getAnnotation(RowLevelSecurity.class)
                    .force();


            log.info("Session found {}", sessionKey);
            log.info("Qualified session key {}", qualifiedKey);

            String policyName = tableName.replace("\"", "") + "_rls_policy";
            String enableRls = "ALTER TABLE " + tableName + " ENABLE ROW LEVEL SECURITY";
            String forceRls = "ALTER TABLE " + tableName + " FORCE ROW LEVEL SECURITY";

            log.info(">>> RLS Contributor: Enabling RLS on {} using column {}", tableName, column);

            // Allow bypassing RLS for 'postgres' superuser if needed, or maintain strict
            // tenant isolation
            // Adding 'OR current_user = 'postgres'' is a common practical requirement for
            // maintenance
            String checkClause = String.format(
                    "( (%s = NULLIF(current_setting('%s', true), '')::%s) OR current_user = 'postgres' )",
                    column, qualifiedKey, castType);

            String createPolicy = String.format(
                    "CREATE POLICY %s_policy ON %s FOR %s " +
                            "USING %s " +
                            "WITH CHECK %s",
                    tableName, tableName, policyType,
                    checkClause,
                    checkClause);
            collector.addAuxiliaryDatabaseObject(new AbstractAuxiliaryDatabaseObject() {

                @Override
                public String[] sqlCreateStrings(SqlStringGenerationContext ctx) {
                    List<String> sql = new ArrayList<>();
                    sql.add(enableRls);
                    if (force)
                        sql.add(forceRls);

                    log.info("Creating policy {}", createPolicy);

                    if ("create".equalsIgnoreCase(mode) || "update".equalsIgnoreCase(mode)) {
                        // Drop existing policy to ensure we can recreate it
                        sql.add("DROP POLICY IF EXISTS " + policyName + " ON " + tableName);
                        sql.add(createPolicy);
                    }
                    return sql.toArray(new String[0]);
                }

                @Override
                public String[] sqlDropStrings(SqlStringGenerationContext ctx) {

                    return new String[]{"DROP POLICY IF EXISTS " + policyName + " ON " + tableName};
                }
            });

            log.info(">>> RLS Contributor: RLS policy registered for {}", tableName);

        });

    }

}
