# Hibernate Postgres RLS

[![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Build Status](https://img.shields.io/badge/build-passing-brightgreen)](https://github.com/AyushGhimire077/hibernate-postgres-rls)
[![Hibernate](https://img.shields.io/badge/Hibernate-6.x-orange.svg)](https://hibernate.org/)
[![Postgres](https://img.shields.io/badge/PostgreSQL-12+-blue.svg)](https://www.postgresql.org/)

Automated PostgreSQL **Row Level Security (RLS)** for Hibernate. This library validates database security policies and manages transaction-scoped session variables in Spring Boot.

## 🚀 Features

- **Database-First RLS**: Database owns the policies; the library validates expectations.
- **Fail-Fast Validation**: Verification of RLS and policies on application startup.
- **Transaction-Scoped Binding**: Bind session variables (like `app.tenant_id`) automatically using `SET LOCAL`.
- **Annotation Sugar**: Use `@RlsSession` on method parameters for zero-boilerplate binding.
- **Thread-Safe**: Safe for multi-threaded applications using staged session context.

## 🛠 Installation

Add the dependency to your `pom.xml`:

```xml
<dependency>
    <groupId>io.github.AyushGhimire077</groupId>
    <artifactId>hibernate-postgres-rls</artifactId>
    <version>2.1.0</version>
</dependency>
```

## 📖 Usage

### 1. Annotate your Entities

Define your RLS expectations:

```java
@Entity
@RowLevelSecurity
@RlsRule(
    table = "documents",
    policy = "tenant_isolation",
    requiredVariable = "app.tenant_id"
)
public class Document {
    @Id
    private Long id;
    
    @Column(name = "tenant_id")
    private String tenantId;
}
```

### 2. Bind the Context

#### Option A: Annotation Sugar (Recommended)

Annotate your `@Transactional` method parameters:

```java
@Service
public class DocumentService {

    @Transactional
    public List<Document> getDocuments(@RlsSession("app.tenant_id") String tenantId) {
        // 'app.tenant_id' is automatically set via SET LOCAL for this transaction.
        return documentRepository.findAll();
    }
}
```

#### Option B: Manual Binding

Inject and use `RlsContext` for more control:

```java
@Autowired
private RlsContext rlsContext;

@Transactional
public void doWork(String tenantId) {
    rlsContext.with("app.tenant_id", tenantId).apply();
    // ...
}
```

## ⚙️ Configuration

```properties
# application.yml
spring.rls.enabled=true

# Optional: Debugging SQL and session variable binding
logging.level.org.hibernate.SQL=DEBUG
logging.level.org.hibernate.orm.jdbc.bind=TRACE
logging.level.org.springframework.jdbc.core.JdbcTemplate=DEBUG
```

## 🛡 Security & Best Practices

1. **DB Owns Polices**: Create policies via Flyway, Liquibase, or manual SQL:
   ```sql
   ALTER TABLE documents ENABLE ROW LEVEL SECURITY;
   CREATE POLICY tenant_isolation ON documents H
   USING (tenant_id = current_setting('app.tenant_id')::bigint);
   ```
2. **Fail Fast**: If RLS or a policy is missing, the application will not start.
3. **Transaction Scoped**: All variables use `SET LOCAL`, automatically cleared on commit/rollback.

## 📜 License

Distributed under the Apache License 2.0. See `LICENSE` for more information.

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.