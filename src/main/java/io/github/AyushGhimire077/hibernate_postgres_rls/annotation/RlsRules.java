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
package io.github.AyushGhimire077.hibernate_postgres_rls.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Container annotation for multiple {@link RlsRule} annotations.
 * <p>
 * This annotation is automatically used when multiple {@code @RlsRule}
 * annotations are applied to the same entity.
 * </p>
 * <h3>Example Usage:</h3>
 * <pre>{@code
 * @Entity
 * @RowLevelSecurity
 * @RlsRule(
 *     name = "tenant_select_policy",
 *     policyType = PolicyType.SELECT,
 *     using = "tenant_id = current_setting('app.tenant_id')::bigint"
 * )
 * @RlsRule(
 *     name = "tenant_modify_policy",
 *     policyType = PolicyType.UPDATE,
 *     using = "tenant_id = current_setting('app.tenant_id')::bigint",
 *     withCheck = "tenant_id = current_setting('app.tenant_id')::bigint"
 * )
 * public class Document { ... }
 * }</pre>
 *
 * @author Aayush Ghimire
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface RlsRules {
    /**
     * Array of RLS rules to apply to the entity.
     */
    RlsRule[] value();
}
