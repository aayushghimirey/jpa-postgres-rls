# Postgres Hibernate RLS

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Java CI](https://github.com/AyushGhimire077/hibernate-postgres-rls/actions/workflows/maven.yml/badge.svg)](https://github.com/AyushGhimire077/hibernate-postgres-rls/actions)

A robust Spring Boot library that seamlessly integrates **PostgreSQL Row Level Security (RLS)** with **Hibernate**. Secure your multi-tenant data at the database level using simple annotations, preventing data leakage by design.

## Features

- **Annotation-based Security**: Protect entities with `@RowLevelSecurity` and define rules with `@RlsRule`.
- **Automatic Policy Management**: policies are created, updated, and managed automatically during application startup.
- **Connection Pool Safe**: Built-in `RlsFilter` ensures session keys are cleared between requests, preventing leakage in connection pooling scenarios.
- **Spring Boot Auto-configuration**: Zero boilerplate setup. Just add the dependency and properties.

## Installation

Add the dependency to your `pom.xml`:

```xml
<dependency>
    <groupId>io.github.AyushGhimire077</groupId>
    <artifactId>hibernate-postgres-rls</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```

## Configuration

Configure the library in your `application.yml`:

```yaml
spring:
  rls:
    enabled: true
    mode: update # Options: create, update, none
```

## Usage

### 1. Annotate your Entity

Mark the entities you want to protect. The library will automatically creating the Postgres Policy for you.

```java
import io.github.AyushGhimire077.hibernate_postgres_rls.annotation.RowLevelSecurity;
import io.github.AyushGhimire077.hibernate_postgres_rls.annotation.RlsRule;
import io.github.AyushGhimire077.hibernate_postgres_rls.enums.PolicyType;
import jakarta.persistence.Entity;

@Entity
@RowLevelSecurity(force = true) // 'force = true' enforces RLS even for table owners
@RlsRule(
    column = "tenant_id",       // The database column to filter by
    sessionKey = "app.tenant",  // The Postgres session variable to check (current_setting('app.tenant'))
    policyType = PolicyType.ALL // Operation to apply logical (ALL, SELECT, UPDATE, etc.)
)
public class Product {
    // ...
}
```

### 2. Set the Context

In your application code (e.g., in a filter, interceptor, or service), set the current tenant context. The library's `RlsFilter` safely manages the lifecycle of this context for web requests.

```java
import io.github.AyushGhimire077.hibernate_postgres_rls.aspect.ClientContext;

// ... inside your authentication flow
String currentTenantId = currentUser.getTenantId();
ClientContext.set("app.tenant", currentTenantId);
```

For standard web applications, the `RlsFilter` will automatically ensure that this context doesn't leak to subsequent requests on the same thread.

## How it Execution Works

1. **Schema Generation**: `RlsSchemaContributor` scans for `@RowLevelSecurity` entities and injects the necessary `CREATE POLICY` SQL statements during the Hibernate schema update phase.
2. **Query Interception**: `RlsQueryInterceptor` intercepts every SQL query. Before the query executes, it injects a `SET "app.tenant" = 'value';` command into the transaction, ensuring Postgres uses the correct security context.
3. **Safety**: `RlsFilter` cleans up the `ClientContext` after every request.

## Contributing

Contributions are welcome! Please open an issue or submit a pull request.

## License

This project is licensed under the Apache 2.0 License.
