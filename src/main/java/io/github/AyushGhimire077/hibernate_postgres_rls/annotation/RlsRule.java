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
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import io.github.AyushGhimire077.hibernate_postgres_rls.enums.PolicyType;

/**
 * Defines Row Level Security (RLS) policy rules for a JPA entity.
 * <p>
 * This annotation must be used in conjunction with {@link RowLevelSecurity}.
 * It allows for granular control over which rows are visible or modifiable
 * based on PostgreSQL session variables.
 * </p>
 * <h3>Policy Clauses:</h3>
 * <ul>
 * <li><b>USING:</b> Defines which rows are visible (SELECT) and which existing
 * rows can be targeted for UPDATE/DELETE.</li>
 * <li><b>WITH CHECK:</b> Defines the constraints for new data being written.
 * New rows (INSERT) and the resulting state of modified rows (UPDATE) must satisfy this.</li>
 * </ul>
 * <h3>Example Usage:</h3>
 * <pre>{@code
 * @RowLevelSecurity
 * @RlsRule(
 * name = "tenant_isolation_policy",
 * policyType = PolicyType.ALL,
 * using = "tenant_id = current_setting('app.tenant_id')::bigint",
 * withCheck = "tenant_id = current_setting('app.tenant_id')::bigint"
 * )
 * @Entity
 * public class Staff { ... }
 * }</pre>
 *
 * @author Aayush Ghimire
 */
@Repeatable(RlsRules.class)
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface RlsRule {

    /**
     * The name of the PostgreSQL policy.
     * <p>
     * If empty, the library defaults to: {@code rls_policy_[table_name]}.
     * </p>
     */
    String name() default "";

    /**
     * The database operation scope for this rule.
     */
    PolicyType policyType() default PolicyType.ALL;

    /**
     * The SQL condition for the PostgreSQL {@code USING} clause.
     */
    String using() default "";

    /**
     * The SQL condition for the PostgreSQL {@code WITH CHECK} clause.
     */
    String withCheck() default "";
}