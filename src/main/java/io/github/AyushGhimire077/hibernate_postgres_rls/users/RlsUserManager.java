package io.github.AyushGhimire077.hibernate_postgres_rls.users;

import java.util.List;

public interface RlsUserManager {

    // Basic Setup
    void createTenant(String username, String password, String schema);

    // Detailed Setup
    void createTenant(String username, String password, String schema, boolean grantUsage);

    // Specific Permission Management
    void grantPermissions(String username, String schema, List<RlsPermission> permissions);

    void grantAllPermissions(String username, String schema);

    void revokeAllPermissions(String username, String schema);

    void dropTenant(String username);

    enum RlsPermission {
        SELECT, INSERT, UPDATE, DELETE, REFERENCES, TRIGGER
    }
}