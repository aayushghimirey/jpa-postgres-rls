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
import io.github.aayushghimirey.jpa_postgres_rls.annotation.RowLevelSecurity;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Utility class to scan JPA entities for RLS (Row Level Security) annotations.
 * <p>
 * It detects entities marked with {@link RowLevelSecurity} and extracts
 * {@link RlsRule} annotations for use in startup validation and enforcement.
 * </p>
 * <p>
 * Example usage:
 * <pre>
 * Set<Class<?>> entities = entityManagerFactory.getMetamodel()
 *     .getEntities()
 *     .stream()
 *     .map(e -> e.getJavaType())
 *     .collect(Collectors.toSet());
 *
 * List&lt;RlsRule&gt; rules = RlsAnnotationScanner.scanEntitiesForRlsRules(entities);
 * </pre>
 *
 * @author Aayush Ghimire
 * @since 2026
 */
public class RlsAnnotationScanner {


    /**
     * Scans a set of entity classes for {@link RlsRule} annotations.
     * <p>
     * Only entities annotated with {@link RowLevelSecurity} or directly with {@link RlsRule}
     * are considered. Extracted {@link RlsRule} annotations are returned in a list.
     * </p>
     *
     * @param entities the set of entity classes to scan
     * @return list of detected RlsRule annotations
     */
    public static List<RlsRule> scanEntitiesForRlsRules(Set<Class<?>> entities) {
        List<RlsRule> rlsRules = new ArrayList<>();

        for (Class<?> entity : entities) {
            // Include entity if it has either @RowLevelSecurity or @RlsRule
            if (entity.isAnnotationPresent(RowLevelSecurity.class) || entity.isAnnotationPresent(RlsRule.class)) {
                RlsRule rule = entity.getAnnotation(RlsRule.class);
                if (rule != null) {
                    rlsRules.add(rule);
                }
            }
        }

        return rlsRules;
    }
}
