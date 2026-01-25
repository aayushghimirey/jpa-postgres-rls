# JPA Postgres RLS

[![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Build Status](https://img.shields.io/badge/build-passing-brightgreen)](https://github.com/aayushghimirey/jpa-postgres-rls)
[![JPA](https://img.shields.io/badge/JPA-3.x-orange.svg)](https://jakarta.ee/specifications/persistence/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-12+-blue.svg)](https://www.postgresql.org/)
[![Java](https://img.shields.io/badge/Java-21-brightgreen.svg)](https://www.oracle.com/java/)

---

## Overview

**JPA Postgres RLS** is a Java/Spring Boot library that enables **PostgreSQL Row-Level Security (RLS)** for JPA entities.  
### Key Features
* **Startup Validation:** Fails fast if DB policies, tables, or required session variables are missing.
* **Declarative Binding:** Use `@RlsSession` on method parameters to automatically set Postgres variables.
* **Zero Leakage:** Uses `SET LOCAL` within transactions to ensure variables are cleared after commit/rollback.
* **Type Safe:** Supports mapping Java objects/Longs directly to Postgres session settings.

---

## Installation

Currently, this library must be installed locally.

```bash
git clone https://github.com/aayushghimirey/jpa-postgres-rls.git
cd jpa-postgres-rls
mvn clean install
```

Add the dependency to your `pom.xml`:

```xml

<dependency>
    <groupId>io.github.aayushghimirey</groupId>
    <artifactId>jpa-postgres-rls</artifactId>
    <version>2.0.0</version>
</dependency>
```

## 📖 Usage

### 1. Annotate your Entities

Define your RLS expectations:

```java

@Entity
@Data
@RowLevelSecurity
@RlsRule(policy = "staff_isolation_policy", requiredVariable = "app.tenant_id", table = "staff")
public class Staff {
    @Id
    private Long id;

    @Column(name = "tenant_id")
    private Long tenantId;
}
```

### 2. Bind the Context

#### Option A: Annotation Sugar (Recommended)

Annotate your `@Transactional` method parameters:

```java

@Service
public class StaffService {

    @Transactional(readOnly = true)
    public List<Staff> getStaffs(@RlsSession("app.tenant_id") Long tenantId) {
        // 'app.tenant_id' is automatically set via SET LOCAL for this transaction.
        return staffRepository.findAll();
    }
}
```

#### Option B: Manual Binding

Inject and use `RlsContext` for more control:

```java

@Autowired
private RlsContext rlsContext;

@Transactional
public void doWork(Long tenantId) {
    rlsContext.with("app.tenant_id", tenantId).apply();
    // ...
}
```

## ⚙️ Configuration

```properties
# application.yml
spring.rls.enabled=true
```

## 🛡 Security & Best Practices

1. **DB Owns Polices**: Create policies via Flyway, Liquibase, or manual SQL:
   ```sql
   ALTER TABLE staff ENABLE ROW LEVEL SECURITY;
   CREATE POLICY staff_isolation ON staff TO ALL
   USING (tenant_id = current_setting('app.tenant_id')::bigint);
   ```
2. **Fail Fast**: If RLS or a policy is missing, the application will not start.
3. **Transaction Scoped**: All variables use `SET LOCAL`, automatically cleared on commit/rollback.

## 📜 License

Distributed under the Apache License 2.0. See `LICENSE` for more information.

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.
