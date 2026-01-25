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
 * Binds a method parameter to a PostgreSQL session variable (e.g., "app.tenant_id").
 * <p>
 * When applied to a method parameter, the annotated value will be set as the specified
 * PostgreSQL session variable for the duration of the transaction.
 * </p>
 *
 * <p>
 * Example usage:
 * <pre>
 * &#64;Transactional
 * public List&lt;Staff&gt; getAll(&#64;RlsSession("tenant_id") UUID tenantId) {
 *     return staffRepo.findAll();
 * }
 * </pre>
 * </p>
 *
 * <p><b>Note:</b> The session variable must match the one used in your RLS policy.
 * If the variable name does not contain a dot, "app." will be prefixed by default.</p>
 *
 * @author Aayush Ghimire
 * @since 2026
 */
@Target({ElementType.PARAMETER, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface RlsSession {

    /**
     * The name of the PostgreSQL session variable to bind the parameter value to.
     *
     * @return session variable name
     */
    String value();
}
