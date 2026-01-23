/*
 * Copyright (c) 2026 Aayush Ghimire
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */


package io.github.AyushGhimire077.hibernate_postgres_rls.users;

import java.util.List;

/**
 *
 * Helper interface for managing database roles (tenants) in a PostgreSQL database with Row Level Security (RLS).
 * <p>
 * This interface provides methods to create and drop roles, grant and revoke permissions,
 * and set the active role for the current database session.
 * </p>
 *
 * @author Aayush Ghimire
 *
 */
public interface RlsUserManager {

    /**
     * Creates a new database role (tenant) with the specified credentials.
     * Automatically grants USAGE on the schema and ALL PRIVILEGES on tables and sequences.
     *
     * @param username the name of the role to create
     * @param password the password for the role
     * @param schema   the database schema to grant access to
     */
    void createTenant(String username, String password, String schema);

    /**
     * Creates a new database role (tenant) with the specified credentials.
     * Allows control over whether to automatically grant permissions.
     *
     * @param username   the name of the role to create
     * @param password   the password for the role
     * @param schema     the database schema to grant access to
     * @param grantUsage if true, automatically grants all permissions; if false, permissions must be granted manually
     */
    void createTenant(String username, String password, String schema, boolean grantUsage);

    /**
     * Grants specific permissions to a user on a schema.
     *
     * @param username    the name of the role
     * @param schema      the database schema
     * @param permissions list of specific permissions to grant (SELECT, INSERT, UPDATE, DELETE, etc.)
     */
    void grantPermissions(String username, String schema, List<RlsPermission> permissions);

    /**
     * Grants all privileges (SELECT, INSERT, UPDATE, DELETE, etc.) to a user on a schema.
     *
     * @param username the name of the role
     * @param schema   the database schema
     */
    void grantAllPermissions(String username, String schema);

    /**
     * Revokes all privileges from a user on a schema.
     *
     * @param username the name of the role
     * @param schema   the database schema
     */
    void revokeAllPermissions(String username, String schema);

    /**
     * Drops (deletes) a database role.
     * <p>
     * <strong>Warning:</strong> This is a destructive operation and cannot be undone.
     * </p>
     *
     * @param username the name of the role to drop
     */
    void dropTenant(String username);

    /**
     * Sets the active role for the current database session.
     * <p>
     * This is typically used to switch the session context to a specific tenant,
     * enabling Row Level Security policies to filter data based on the active role.
     * </p>
     * <p>
     * <strong>Important:</strong> This should be called within a transaction context,
     * and the role will remain active for the duration of that transaction.
     * </p>
     *
     * @param username the name of the role to activate
     */
    void setActiveRole(String username);

    /**
     * Enumeration of database permissions that can be granted to roles.
     */
    enum RlsPermission {
        SELECT, INSERT, UPDATE, DELETE, REFERENCES, TRIGGER
    }
}