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
package io.github.AyushGhimire077.hibernate_postgres_rls.aspect;

 import io.github.AyushGhimire077.hibernate_postgres_rls.annotation.RlsSession;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import static org.mockito.Mockito.*;

class RlsTransactionalAspectTest {

    private RlsTransactionalAspect aspect;

    @Mock
    private io.github.AyushGhimire077.hibernate_postgres_rls.core.RlsContext rlsContext;

    @Mock
    private JoinPoint joinPoint;

    @Mock
    private MethodSignature methodSignature;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        aspect = new RlsTransactionalAspect(rlsContext);
    }

    @Test
    void shouldApplyContextFromParameters() throws Exception {
        // Prepare method with @RlsContext annotation
        Method method = MockService.class.getMethod("testMethod", String.class);

        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getMethod()).thenReturn(method);
        when(joinPoint.getArgs()).thenReturn(new Object[]{"tenant-123"});

        aspect.beforeTransactionalMethod(joinPoint);

        verify(rlsContext).with("app.tenant_id", "tenant-123");
        verify(rlsContext).apply();
    }

    @Test
    void shouldSkipIfNoAnnotationsFound() throws Exception {
        Method method = MockService.class.getMethod("noAnnotationMethod", String.class);

        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getMethod()).thenReturn(method);
        when(joinPoint.getArgs()).thenReturn(new Object[]{"data"});

        aspect.beforeTransactionalMethod(joinPoint);

        verify(rlsContext, never()).with(anyString(), any());
        verify(rlsContext, never()).apply();
    }

    // Mock interface for testing reflection
    private static class MockService {
        public void testMethod(@RlsSession("app.tenant_id") String tenantId) {
        }

        public void noAnnotationMethod(String data) {
        }
    }
}
