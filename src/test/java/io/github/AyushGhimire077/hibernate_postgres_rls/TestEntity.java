package io.github.AyushGhimire077.hibernate_postgres_rls;

import io.github.AyushGhimire077.hibernate_postgres_rls.annotation.RlsPolicy;
import io.github.AyushGhimire077.hibernate_postgres_rls.emuns.PolicyType;
import jakarta.persistence.*;
import io.github.AyushGhimire077.hibernate_postgres_rls.annotation.RlsProtected;

@Entity
@RlsProtected(force = true)
@RlsPolicy(column = "tenant_id", sessionKey = "tenant_id", policyType = PolicyType.ALL)
public class TestEntity {
    @Id
    @GeneratedValue
    private Long id;

    private String name;

    @Column(name = "tenant_id")
    private Long tenantId;

}
