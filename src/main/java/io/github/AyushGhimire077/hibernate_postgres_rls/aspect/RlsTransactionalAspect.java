package io.github.AyushGhimire077.hibernate_postgres_rls.aspect;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class RlsTransactionalAspect {

    private static final Logger log =
            LoggerFactory.getLogger(RlsTransactionalAspect.class);

    private final RlsTransactionInitializer initializer;

    public RlsTransactionalAspect(RlsTransactionInitializer initializer) {
        this.initializer = initializer;
    }

    @Before("@annotation(org.springframework.transaction.annotation.Transactional)")
    public void beforeTransactionalMethod() {
        log.debug("Transactional method detected → applying RLS context");
        initializer.applyRlsContextIfNeeded();
    }
}
