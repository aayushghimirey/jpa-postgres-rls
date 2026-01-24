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
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Binds a parameter value to a PostgreSQL session variable (e.g. app.tenant_id).
 * <p> This annotation can be applied to method parameters.
 * When used in a method parameter, the annotated parameter's value will be set
 * as the specified PostgreSQL session variable for the duration of the transaction.
 * </p>
 */
@Target({ElementType.PARAMETER, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface RlsSession {
    /**
     * The name of the session variable (e.g., "app.tenant_id").
     *
     * <b>NOTE:</b>
     * <p>This values should match the session variable used in the RLS policies.</p>
     * Eg:
     * <p> CREAT POLICY * ON *
     * USING (current_setting(<b>session variable</b>);</p>
     *
     */
    String value();


}
