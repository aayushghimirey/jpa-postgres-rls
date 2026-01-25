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

package io.github.aayushghimirey.jpa_postgres_rls.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to define Row-Level Security (RLS) rules for a JPA entity.
 * <p>
 * Use this to specify the table, the RLS policy name, and the required session variable.
 * Example:
 * <pre>
 * CREATE POLICY "policy_name" ON "table_name"
 *   USING (current_setting('session_variable')::uuid = 'dynamic_value');
 * </pre>
 * </p>
 *
 * @author Aayush Ghimire
 * @since 2026
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface RlsRule {

    /**
     * The database table to which the RLS policy applies.
     *
     * @return table name
     */
    String table();

    /**
     * The expected RLS policy name in the database.
     *
     * @return policy name
     */
    String policy();

    /**
     * The session variable that must be set for this policy to function.
     * Example: "app.tenant_id"
     * <p><b>Note:</b> This value should match the session variable used in the RLS policy.</p>
     * <p>Example RLS usage:</p>
     * <pre>
     * CREATE POLICY policy_name ON table_name
     *   USING (current_setting('session_variable')::uuid = 'dynamic_value');
     * </pre>
     * <p>If the session variable does not contain a dot, "app." is prefixed by default.</p>
     *
     * @return session variable name
     */
    String requiredVariable();

}