/*
 * Copyright (C) 2026 Aayush Ghimire
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.aayushghimirey.jpa_postgres_rls.aspect;

import io.github.aayushghimirey.jpa_postgres_rls.annotation.RlsSession;
import io.github.aayushghimirey.jpa_postgres_rls.core.RlsContext;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * Aspect to automatically bind RLS (Row-Level Security) session variables
 * from method parameters annotated with {@link RlsSession}.
 * <p>
 * Any method annotated with {@link org.springframework.transaction.annotation.Transactional}
 * or within a transactional class will trigger this aspect before execution.
 * </p>
 * <p>
 * For each parameter annotated with {@link RlsSession}, the aspect will:
 * <ul>
 *     <li>Extract the session variable name from the annotation</li>
 *     <li>Bind the parameter value to the {@link RlsContext}</li>
 *     <li>Execute RLS session variables for the current transaction</li>
 * </ul>
 * </p>
 * <p>
 * Example usage:
 * <pre>
 * &#64;Transactional
 * public void createStaff(&#64;RlsSession("app.tenant_id") UUID tenantId, Staff staff) {
 *     // tenantId will automatically be set in PostgreSQL session for RLS
 * }
 * </pre>
 *
 * @author Aayush Ghimire
 * @since 2026
 */
@Aspect
public class RlsTransactionalAspect {

    private static final Logger log = LoggerFactory.getLogger(RlsTransactionalAspect.class);
    private final RlsContext rlsContext;

    public RlsTransactionalAspect(RlsContext rlsContext) {
        this.rlsContext = rlsContext;
    }

    /**
     * Advice executed before any method annotated with {@link org.springframework.transaction.annotation.Transactional}
     * or within a transactional class.
     * <p>
     * It scans method parameters for {@link RlsSession} annotations and binds their values
     * to the PostgreSQL session using {@link RlsContext#with(String, Object)}.
     * </p>
     *
     * @param joinPoint the join point of the method execution
     */
    @Before("@annotation(org.springframework.transaction.annotation.Transactional) || @within(org.springframework.transaction.annotation.Transactional)")
    public void beforeTransactionalMethod(JoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Object[] args = joinPoint.getArgs();
        Annotation[][] parameterAnnotations = method.getParameterAnnotations();

        log.debug("Transactional annotation found on {}, applying RLS policy with values: {}", method.getName(), args);

        boolean anyVariableSet = false;

        for (int i = 0; i < parameterAnnotations.length; i++) {
            for (Annotation annotation : parameterAnnotations[i]) {
                if (annotation instanceof RlsSession rlsSession) {
                    String sessionKey = rlsSession.value();
                    Object sessionValue = args[i];

                    log.debug("Found @RlsSession on parameter {}: {} = {}", i, sessionKey, sessionValue);

                    // Set the session variable in RLS context (thread-local)
                    rlsContext.with(sessionKey, sessionValue);
                    anyVariableSet = true;
                }
            }
        }

        if (anyVariableSet) {
            // Apply all SET LOCAL statements to the database session
            log.info("Applying RLS session variables to the current transaction");
            rlsContext.apply();
        }
    }
}
