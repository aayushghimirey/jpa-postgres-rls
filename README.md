# Hibernate Postgres RLS

[![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](https://www.gnu.org/licenses/gpl-3.0)
[![Build Status](https://img.shields.io/badge/build-passing-brightgreen)](https://github.com/AyushGhimire077/hibernate-postgres-rls)
[![Hibernate](https://img.shields.io/badge/Hibernate-6.x-orange.svg)](https://hibernate.org/)
[![Postgres](https://img.shields.io/badge/PostgreSQL-12+-blue.svg)](https://www.postgresql.org/)

Automated PostgreSQL **Row Level Security (RLS)** for Hibernate entities via annotations. Secure your multi-tenant or
user-partitioned data with zero boilerplate.

## 🚀 Features

- **Declarative RLS**: Use `@RowLevelSecurity` and `@RlsRule` directly on your JPA entities.
- **Repeatable Rules**: Support for multiple RLS policies per table.
- **Security First**: Built-in SQL identifier validation and quoting to prevent SQL injection.
- **Thread-Local Context**: Manage RLS session variables (like `tenant_id`) safely across requests.
- **Transactional Integration**: Automatically applies RLS context before executing `@Transactional` methods.
- **Schema Automation**: Automatically generates `ENABLE ROW LEVEL SECURITY` and `CREATE POLICY` statements during
  Hibernate schema export/update.

## 🛠 Installation

Add the dependency to your `pom.xml`:

```xml

<dependency>
    <groupId>io.github.AyushGhimire077</groupId>
    <artifactId>hibernate-postgres-rls</artifactId>
    <version>0.7.1</version>
</dependency>
```

## 📖 Usage

### 1. Annotate your Entities

Mark your entity for RLS and define your policies:

```java

@Entity
@Table(name = "user_documents")
@RowLevelSecurity(force = true) // 'force' ensures RLS applies even to the table owner
@RlsRule(
        name = "tenant_isolation",
        policyType = PolicyType.ALL,
        using = "tenant_id = current_setting('app.tenant_id', true)",
        withCheck = "tenant_id = current_setting('app.tenant_id', true)"
)
public class Document {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Column(name = "tenant_id")
    private String tenantId;
}
```

### 2. Set the Context

In your service or web layer, set the RLS context:

```java

@Service
public class DocumentService {

    @Transactional
    public void createDocument(String title) {
        // This will be automatically applied as 'SET app.tenant_id = 'my-tenant-id''
        // before the database operations in this method.
        ClientContext.put("tenant_id", "my-tenant-id");

        documentRepository.save(new Document(title, "my-tenant-id"));
    }
}
```

### 3. Web Integration

For Spring Boot applications, the `RlsFilter` can automatically extract tenant IDs from headers:

```properties
# application.properties
spring.rls.enabled=true
spring.rls.mode=ENFORCE
```

Add a custom filter or use the provided `RlsFilter` by passing headers like `X-Tenant-Id`.

## 🛡 Security

This library takes security seriously:

- **Identifier Validation**: All table names, policy names, and session keys are validated against a strict whitelist.
- **SQL Quoting**: Dynamic SQL generation uses proper PostgreSQL quoting for identifiers and literals.
- **Context Isolation**: Uses `ThreadLocal` for session state, with guaranteed cleanup in the web filter to prevent data
  leakage between requests.

## ⚙️ Configuration

| Property             | Description                                         | Default    |
|----------------------|-----------------------------------------------------|------------|
| `spring.rls.enabled` | Enable/Disable RLS support                          | `false`    |
| `spring.rls.mode`    | `VALIDATE` (check only) or `ENFORCE` (generate DDL) | `VALIDATE` |

## 🧪 Testing

The library comes with a comprehensive test suite (40+ tests).

```bash
mvn clean test
```

## 📜 License

Distributed under the GPL-3.0 License. See `LICENSE` for more information.

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

# NOTE:
Many test are generated using AI tools. Please verify their correctness before using them in production.