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

package io.github.AyushGhimire077.hibernate_postgres_rls.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Defines RLS expectations for a JPA entity (table, expected policy, and required session variable).
 * <b>Thought</b>
 * <p>
 * Policy required its name, on which table and session variable.
 * Eg:
 * CREAT POLICY "policy-name" ON "table-name"
 * USING (current_setting("session variable")::{type} = "dynamic value");
 * </p>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface RlsRule {

    /**
     * Name of the database table to which the RLS policy applies.
     */
    String table();

    /**
     * Name of the RLS policy expected to exist in the database.
     */
    String policy();

    /**
     * The name of the session variable (e.g., "app.tenant_id") that must be set
     * for this policy to function correctly.
     */
    String requiredVariable();

}