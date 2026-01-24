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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Scans entities for RLS annotations.
 */
public class RlsAnnotationScanner {

    /**
     * Scans entities for @RlsRule and @RowLevelSecurity annotations.
     */
    public static List<RlsRule> scanEntitiesForRlsRules(Set<Class<?>> entities) {
        List<RlsRule> rlsRules = new ArrayList<>();

        for (Class<?> entity : entities) {
            // Check for both @RowLevelSecurity marker and @RlsRule
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
