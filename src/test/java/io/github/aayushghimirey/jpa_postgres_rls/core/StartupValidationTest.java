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

package io.github.aayushghimirey.jpa_postgres_rls.core;

import io.github.aayushghimirey.jpa_postgres_rls.annotation.RlsRule;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

class StartupValidationTest {

    private StartupValidation validation;

    @Mock
    private DataSource dataSource;

    @Mock
    private Connection connection;

    @Mock
    private PreparedStatement psClass;

    @Mock
    private PreparedStatement psPolicy;

    @Mock
    private ResultSet rsClass;

    @Mock
    private ResultSet rsPolicy;

    @Mock
    private RlsRule rule;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        validation = new StartupValidation(dataSource);
        
        when(dataSource.getConnection()).thenReturn(connection);
        
        // Default rule behavior
        when(rule.table()).thenReturn("test_table");
//        when(rule.schema()).thenReturn("public");
        when(rule.policy()).thenReturn("test_policy");
        when(rule.requiredVariable()).thenReturn("app.tenant_id");
    }
//
//    @Test
//    void shouldPassWhenDbCorrect() throws Exception {
//        when(connection.prepareStatement(contains("pg_class"))).thenReturn(psClass);
//        when(psClass.executeQuery()).thenReturn(rsClass);
//        when(rsClass.next()).thenReturn(true);
//        when(rsClass.getBoolean(1)).thenReturn(true);
//
//        when(connection.prepareStatement(contains("pg_policies"))).thenReturn(psPolicy);
//        when(psPolicy.executeQuery()).thenReturn(rsPolicy);
//        when(rsPolicy.next()).thenReturn(true);
//
//        assertDoesNotThrow(() -> validation.validate(List.of(rule)));
//    }
//
//    @Test
//    void shouldFailWhenRlsNotEnabled() throws Exception {
//        when(connection.prepareStatement(contains("pg_class"))).thenReturn(psClass);
//        when(psClass.executeQuery()).thenReturn(rsClass);
//        when(rsClass.next()).thenReturn(true);
//        when(rsClass.getBoolean(1)).thenReturn(false);
//
//        assertThrows(IllegalStateException.class, () -> validation.validate(List.of(rule)));
//    }
//
//    @Test
//    void shouldFailWhenPolicyMissing() throws Exception {
//        when(connection.prepareStatement(contains("pg_class"))).thenReturn(psClass);
//        when(psClass.executeQuery()).thenReturn(rsClass);
//        when(rsClass.next()).thenReturn(true);
//        when(rsClass.getBoolean(1)).thenReturn(true);
//
//        when(connection.prepareStatement(contains("pg_policies"))).thenReturn(psPolicy);
//        when(psPolicy.executeQuery()).thenReturn(rsPolicy);
//        when(rsPolicy.next()).thenReturn(false);
//
//        assertThrows(IllegalStateException.class, () -> validation.validate(List.of(rule)));
//    }
}
