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


package io.github.AyushGhimire077.hibernate_postgres_rls.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RlsConditionBuilderTest {

    @Test
    void shouldBuildTenantCondition() {
        String condition = RlsConditionBuilder.tenantIsolation("tenant_id", "tenant_id", "bigint");
        assertEquals("tenant_id = NULLIF(current_setting('app.tenant_id', true), '')::bigint", condition);
    }

    @Test
    void shouldBuildUserCondition() {
        String condition = RlsConditionBuilder.userIsolation("owner_id", "user_id", "uuid");
        assertEquals("owner_id = NULLIF(current_setting('app.user_id', true), '')::uuid", condition);
    }

    @Test
    void shouldBuildSuperuserBypass() {
        String condition = RlsConditionBuilder.tenantIsolationWithBypass("tenant_id", "tenant_id", "text");
        assertEquals("(tenant_id = NULLIF(current_setting('app.tenant_id', true), '')::text) OR current_user = 'postgres'", condition);
    }

    @Test
    void shouldBuildStatusFilter() {
        String condition = RlsConditionBuilder.statusFilter("status", "ACTIVE");
        assertEquals("status = 'ACTIVE'", condition);
    }

    @Test
    void shouldCombineConditions() {
        String c1 = "a = 1";
        String c2 = "b = 2";
        
        assertEquals("(a = 1) AND (b = 2)", RlsConditionBuilder.and(c1, c2));
        assertEquals("(a = 1) OR (b = 2)", RlsConditionBuilder.or(c1, c2));
    }
}
