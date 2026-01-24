package io.github.AyushGhimire077.hibernate_postgres_rls.aspect;

import io.github.AyushGhimire077.hibernate_postgres_rls.annotation.RlsSession;
import io.github.AyushGhimire077.hibernate_postgres_rls.core.RlsContext;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * Aspect to automatically apply RLS session variables from annotated method parameters.
 */
@Aspect
public class RlsTransactionalAspect {

    private static final Logger log = LoggerFactory.getLogger(RlsTransactionalAspect.class);
    private final RlsContext rlsContext;

    public RlsTransactionalAspect(RlsContext rlsContext) {
        this.rlsContext = rlsContext;
    }

    @Before("@annotation(org.springframework.transaction.annotation.Transactional) || @within(org.springframework.transaction.annotation.Transactional)")
    public void beforeTransactionalMethod(JoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Object[] args = joinPoint.getArgs();
        Annotation[][] parameterAnnotations = method.getParameterAnnotations();

        boolean anyVariableSet = false;


        for (int i = 0; i < parameterAnnotations.length; i++) {
            for (Annotation annotation : parameterAnnotations[i]) {

                if (annotation instanceof RlsSession rlsSession) {
                    String session_key = rlsSession.value();
                    Object session_value = args[i];

                    log.debug("Found @RlsSession on parameter {}: {} = {}", i, session_key, session_value);
                    /*
                     * Validate and set the session variable in RLS context local thread
                     * */
                    rlsContext.with(session_key, session_value);
                    anyVariableSet = true;
                }
            }
        }


        if (anyVariableSet) {
            /*
             *  Execute the SET LOCAL statements to bind session variables
             * */
            rlsContext.apply();
        }
    }
}
