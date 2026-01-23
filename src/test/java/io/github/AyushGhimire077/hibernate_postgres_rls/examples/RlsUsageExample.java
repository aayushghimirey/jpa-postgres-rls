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

import io.github.AyushGhimire077.hibernate_postgres_rls.aspect.ClientContext;
import io.github.AyushGhimire077.hibernate_postgres_rls.users.RlsUserManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;

/**
 * Example service demonstrating how to use RlsUserManager and ClientContext
 * to dynamically manage RLS users and set context at runtime.
 * <p>
 * This shows the typical workflow for a multi-tenant application using RLS.
 * </p>
 */
@Service
public class RlsUsageExample {

    @Autowired
    private RlsUserManager rlsUserManager;

    /**
     * Example: Setting up a new tenant at runtime.
     * This would typically be called during tenant onboarding.
     */
    @Transactional
    public void setupNewTenant(String tenantName, String password) {
        // Create a new database role for the tenant
        rlsUserManager.createTenant(tenantName, password, "public");
        
        // The tenant now has full access to the 'public' schema
        // and RLS policies will filter data based on their role
    }

    /**
     * Example: Setting up a tenant with specific permissions only.
     */
    @Transactional
    public void setupTenantWithLimitedAccess(String tenantName, String password) {
        // Create tenant without auto-granting permissions
        rlsUserManager.createTenant(tenantName, password, "public", false);
        
        // Grant only SELECT and INSERT permissions
        rlsUserManager.grantPermissions(
            tenantName, 
            "public", 
            Arrays.asList(
                RlsUserManager.RlsPermission.SELECT,
                RlsUserManager.RlsPermission.INSERT
            )
        );
    }

    /**
     * Example: Setting RLS context for the current request/transaction.
     * This would typically be called in a filter or interceptor based on
     * the authenticated user.
     */
    @Transactional
    public void performTenantOperation(String tenantId, String userId) {
        // Set the tenant context - this will be used by RLS policies
        ClientContext.put("tenant_id", tenantId);
        ClientContext.put("user_id", userId);
        
        // Now any database operations in this transaction will be filtered
        // by the RLS policies using these context values
        
        // Example: Save or query entities
        // The RLS policies will automatically filter results based on tenant_id
        
        // Don't forget to clear context after the transaction
        // (This is typically done in a finally block or using try-with-resources)
    }

    /**
     * Example: Switching active role for administrative operations.
     * Use with caution - this changes the database role for the current session.
     */
    @Transactional
    public void performAdminOperation(String adminRoleName) {
        // Switch to admin role
        rlsUserManager.setActiveRole(adminRoleName);
        
        // Perform admin operations
        // ...
        
        // Note: The role persists for the duration of the transaction
    }

    /**
     * Example: Cleaning up context (typically in a filter's finally block).
     */
    public void cleanupContext() {
        ClientContext.clear();
    }

    /**
     * Example: Removing a tenant.
     */
    @Transactional
    public void removeTenant(String tenantName) {
        // First revoke all permissions
        rlsUserManager.revokeAllPermissions(tenantName, "public");
        
        // Then drop the role
        rlsUserManager.dropTenant(tenantName);
    }
}
