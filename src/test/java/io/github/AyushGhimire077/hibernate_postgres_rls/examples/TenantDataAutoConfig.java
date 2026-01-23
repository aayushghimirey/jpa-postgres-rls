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

package io.github.AyushGhimire077.hibernate_postgres_rls.examples;

import io.github.AyushGhimire077.hibernate_postgres_rls.annotation.RlsRule;
import io.github.AyushGhimire077.hibernate_postgres_rls.annotation.RowLevelSecurity;
import io.github.AyushGhimire077.hibernate_postgres_rls.enums.PolicyType;
import jakarta.persistence.*;

/**
 * Example entity demonstrating tenant isolation RLS pattern.
 * <p>
 * This example shows a common multi-tenant pattern where data is filtered by tenant_id.
 * The policy allows the postgres superuser to bypass RLS for administrative tasks.
 * </p>
 * <p>
 * The library will create a policy like:
 * <pre>
 * CREATE POLICY tenant_data_rls_policy ON tenant_data FOR ALL
 * USING ( (tenant_id = NULLIF(current_setting('app.tenant_id', true), '')::text) OR current_user = 'postgres' )
 * WITH CHECK ( (tenant_id = NULLIF(current_setting('app.tenant_id', true), '')::text) OR current_user = 'postgres' )
 * </pre>
 * </p>
 * <p>
 * <b>Note:</b> You can use {@link io.github.AyushGhimire077.hibernate_postgres_rls.util.RlsConditionBuilder}
 * to generate these conditions programmatically outside of annotations.
 * </p>
 */
@Entity
@Table(name = "tenant_data")
@RowLevelSecurity(force = false)
@RlsRule(
        name = "tenant_data_rls_policy",
        using = "(tenant_id = NULLIF(current_setting('app.tenant_id', true), '')::text) OR current_user = 'postgres'",
        withCheck = "(tenant_id = NULLIF(current_setting('app.tenant_id', true), '')::text) OR current_user = 'postgres'",
        policyType = PolicyType.ALL
)
public class TenantDataAutoConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private String tenantId;

    @Column(name = "data")
    private String data;

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
}
