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
 * Marker annotation for JPA entities that are protected by Row-Level Security (RLS) .
 * <p>
 * Use this annotation on entities to indicate that RLS validation should be applied
 * at startup and enforced for database operations.
 * </p>
 *
 * <p><b>Primary goal:</b> to highlight entities that require RLS protection.</p>
 *
 * @author Aayush Ghimire
 * @since 2026
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface RowLevelSecurity {
}
