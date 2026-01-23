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

import io.github.AyushGhimire077.hibernate_postgres_rls.enums.PolicyType;
import io.github.AyushGhimire077.hibernate_postgres_rls.annotation.RlsRule;
import io.github.AyushGhimire077.hibernate_postgres_rls.annotation.RowLevelSecurity;
import io.github.AyushGhimire077.hibernate_postgres_rls.exception.RlsConfigurationException;
import org.hibernate.boot.ResourceStreamLocator;
import org.hibernate.boot.model.relational.AuxiliaryDatabaseObject;
import org.hibernate.boot.model.relational.QualifiedTableName;
import org.hibernate.boot.model.relational.SqlStringGenerationContext;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.spi.AdditionalMappingContributions;
import org.hibernate.boot.spi.InFlightMetadataCollector;
import org.hibernate.boot.spi.MetadataBuildingContext;
import org.hibernate.boot.spi.BootstrapContext;
import org.hibernate.engine.config.spi.ConfigurationService;
import org.hibernate.mapping.PersistentClass;
import org.hibernate.mapping.Table;
import org.hibernate.service.ServiceRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RlsSchemaContributorTest {

    private RlsSchemaContributor contributor;

    @Mock
    private AdditionalMappingContributions contributions;

    @Mock
    private InFlightMetadataCollector collector;

    @Mock
    private ResourceStreamLocator locator;

    @Mock
    private MetadataBuildingContext context;

    @Mock
    private BootstrapContext bootstrapContext;

    @Mock
    private StandardServiceRegistry serviceRegistry;

    @Mock
    private ConfigurationService configurationService;

    @Mock
    private PersistentClass binding;

    @Mock
    private Table table;

    @Mock
    private SqlStringGenerationContext sqlCtx;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        contributor = new RlsSchemaContributor();

        when(context.getBootstrapContext()).thenReturn(bootstrapContext);
        when(bootstrapContext.getServiceRegistry()).thenReturn(serviceRegistry);
        when(serviceRegistry.getService(ConfigurationService.class)).thenReturn(configurationService);
        
        Map<String, Object> settings = new HashMap<>();
        settings.put("spring.rls.enabled", "true");
        when(configurationService.getSettings()).thenReturn(settings);
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldRegisterPolicyForAnnotatedEntity() {
        when(binding.getMappedClass()).thenReturn((Class) TestEntity.class);
        when(binding.getTable()).thenReturn(table);
        when(table.getName()).thenReturn("test_table");
        QualifiedTableName qualifiedTableName = mock(QualifiedTableName.class);
        when(table.getQualifiedTableName()).thenReturn(qualifiedTableName);
        when(qualifiedTableName.render()).thenReturn("public.test_table");
        
        when(collector.getEntityBindings()).thenReturn(Collections.singleton(binding));

        contributor.contribute(contributions, collector, locator, context);

        ArgumentCaptor<AuxiliaryDatabaseObject> objectCaptor = ArgumentCaptor.forClass(AuxiliaryDatabaseObject.class);
        verify(collector).addAuxiliaryDatabaseObject(objectCaptor.capture());

        AuxiliaryDatabaseObject ado = objectCaptor.getValue();
        String[] createSql = ado.sqlCreateStrings(sqlCtx);
        
        boolean hasEnable = false;
        boolean hasPolicy = false;
        for (String sql : createSql) {
            if (sql.contains("ALTER TABLE public.test_table ENABLE ROW LEVEL SECURITY")) hasEnable = true;
            if (sql.contains("CREATE POLICY \"rls_policy_test_table_all\" ON public.test_table")) hasPolicy = true;
        }
        
        assertTrue(hasEnable);
        assertTrue(hasPolicy);
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldRegisterMultiplePolicies() {
        when(binding.getMappedClass()).thenReturn((Class) MultiPolicyEntity.class);
        when(binding.getTable()).thenReturn(table);
        when(table.getName()).thenReturn("multi_table");
        QualifiedTableName qualifiedTableName = mock(QualifiedTableName.class);
        when(table.getQualifiedTableName()).thenReturn(qualifiedTableName);
        when(qualifiedTableName.render()).thenReturn("public.multi_table");
        
        when(collector.getEntityBindings()).thenReturn(Collections.singleton(binding));

        contributor.contribute(contributions, collector, locator, context);

        ArgumentCaptor<AuxiliaryDatabaseObject> objectCaptor = ArgumentCaptor.forClass(AuxiliaryDatabaseObject.class);
        verify(collector).addAuxiliaryDatabaseObject(objectCaptor.capture());

        AuxiliaryDatabaseObject ado = objectCaptor.getValue();
        String[] createSql = ado.sqlCreateStrings(sqlCtx);
        
        long policyCount = Arrays.stream(createSql).filter(sql -> sql.contains("CREATE POLICY")).count();
        assertEquals(2, policyCount);
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldThrowExceptionIfMissingRule() {
        when(binding.getMappedClass()).thenReturn((Class) MissingRuleEntity.class);
        when(collector.getEntityBindings()).thenReturn(Collections.singleton(binding));

        assertThrows(RlsConfigurationException.class, () -> 
                contributor.contribute(contributions, collector, locator, context));
    }

    @Test
    void shouldSkipIfDisabled() {
        Map<String, Object> settings = new HashMap<>();
        settings.put("spring.rls.enabled", "false");
        when(configurationService.getSettings()).thenReturn(settings);

        contributor.contribute(contributions, collector, locator, context);

        verify(collector, never()).getEntityBindings();
    }

    @RowLevelSecurity
    @RlsRule(using = "tenant_id = 1", withCheck = "tenant_id = 1")
    static class TestEntity {}

    @RowLevelSecurity
    @RlsRule(name = "p1", policyType = io.github.AyushGhimire077.hibernate_postgres_rls.enums.PolicyType.SELECT, using = "1=1")
    @RlsRule(name = "p2", policyType = io.github.AyushGhimire077.hibernate_postgres_rls.enums.PolicyType.UPDATE, using = "1=1", withCheck = "1=1")
    static class MultiPolicyEntity {}

    @RowLevelSecurity
    static class MissingRuleEntity {}
}
