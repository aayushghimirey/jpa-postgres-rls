package io.github.AyushGhimire077.hibernate_postgres_rls.annotation;

import io.github.AyushGhimire077.hibernate_postgres_rls.emuns.PolicyType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface RlsPolicy {
    String column();

    String castType() default "text"; // uuid, bigint, text

    String sessionKey();

    PolicyType policyType() default PolicyType.ALL;

}
