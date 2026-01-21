package io.github.AyushGhimire077.hibernate_postgres_rls.core;

import io.github.AyushGhimire077.hibernate_postgres_rls.annotation.RlsProtected;
import io.github.AyushGhimire077.hibernate_postgres_rls.annotation.RlsPolicy;
import io.github.AyushGhimire077.hibernate_postgres_rls.config.RlsProperties;
import io.github.AyushGhimire077.hibernate_postgres_rls.config.RlsRuntimeConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.hibernate.boot.ResourceStreamLocator;
import org.hibernate.boot.model.relational.AbstractAuxiliaryDatabaseObject;
import org.hibernate.boot.model.relational.SqlStringGenerationContext;
import org.hibernate.boot.spi.*;

import java.util.ArrayList;
import java.util.List;


public class RlsMappingContributor implements AdditionalMappingContributor {

    private static final Logger log = LoggerFactory.getLogger(RlsMappingContributor.class);

    @Override
    public void contribute(AdditionalMappingContributions contributions, InFlightMetadataCollector collector, ResourceStreamLocator locator, MetadataBuildingContext context) {

        log.info(">>> RLS Contributor: Initialising RLS Mapping Contributor");

        RlsProperties props = RlsRuntimeConfig.get();

        if (props == null || !props.isEnabled()) {
            log.info(">>> RLS Contributor: RLS disabled, skipping contributor");
            return;
        }


        collector.getEntityBindings().forEach(binding -> {

            Class<?> entityClass = binding.getMappedClass();

            if (entityClass == null) return;

            if (!entityClass.isAnnotationPresent(RlsProtected.class)) return;

            RlsPolicy policy = entityClass.getAnnotation(RlsPolicy.class);
            if (policy == null) {
                throw new IllegalStateException("Entity " + entityClass.getName() + " is @RlsProtected but missing @RlsPolicy");
            }

            String tableName = binding.getTable().getQuotedName();
            String column = policy.column();
            String sessionKey = policy.sessionKey();
            String policyType = policy.policyType().name();
            String castType = policy.castType();

            boolean force = entityClass
                    .getAnnotation(RlsProtected.class)
                    .force();

            String policyName = tableName.replace("\"", "") + "_rls_policy";

            String enableRls = "ALTER TABLE " + tableName + " ENABLE ROW LEVEL SECURITY";
            String forceRls = "ALTER TABLE " + tableName + " FORCE ROW LEVEL SECURITY";


            log.info(">>> RLS Contributor: Enabling RLS on {} using column {}", tableName, column);


            String createPolicy = """
                          CREATE POLICY %s
                          ON %s
                          FOR %s
                             USING (
                                 %s = NULLIF(current_setting('%s', true), '')::%s
                             )
                    """
                    .formatted(
                            policyName,
                            tableName,
                            policyType,
                            column,
                            sessionKey,
                            castType
                    );


            collector.addAuxiliaryDatabaseObject(new AbstractAuxiliaryDatabaseObject() {


                @Override
                public String[] sqlCreateStrings(SqlStringGenerationContext ctx) {

                    List<String> sql = new ArrayList<>();
                    sql.add(enableRls);
                    if (force) sql.add(forceRls);
                    sql.add("DROP POLICY IF EXISTS " + policyName + " ON " + tableName);
                    sql.add(createPolicy);
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