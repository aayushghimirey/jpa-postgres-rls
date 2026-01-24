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

package io.github.AyushGhimire077.hibernate_postgres_rls.core;

import io.github.AyushGhimire077.hibernate_postgres_rls.annotation.RlsRule;
import io.github.AyushGhimire077.hibernate_postgres_rls.annotation.RowLevelSecurity;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RlsAnnotationScannerTest {

    @Test
    void shouldFindRlsRules() {
        Set<Class<?>> entities = Set.of(AnnotatedEntity.class, NonAnnotatedEntity.class, MarkerOnlyEntity.class);
        List<RlsRule> rules = RlsAnnotationScanner.scanEntitiesForRlsRules(entities);

        assertEquals(1, rules.size());
        assertEquals("test_table", rules.get(0).table());
        assertEquals("test_policy", rules.get(0).policy());
    }

    @Test
    void shouldHandleEmptySet() {
        assertTrue(RlsAnnotationScanner.scanEntitiesForRlsRules(Set.of()).isEmpty());
    }

    @RowLevelSecurity
    @RlsRule(table = "test_table", policy = "test_policy", requiredVariable = "app.tenant_id")
    private static class AnnotatedEntity {}

    @RowLevelSecurity
    private static class MarkerOnlyEntity {}

    private static class NonAnnotatedEntity {}
}
