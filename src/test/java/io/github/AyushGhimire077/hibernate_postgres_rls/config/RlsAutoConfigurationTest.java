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

package io.github.AyushGhimire077.hibernate_postgres_rls.config;

import io.github.AyushGhimire077.hibernate_postgres_rls.aspect.RlsTransactionalAspect;
import io.github.AyushGhimire077.hibernate_postgres_rls.core.RlsContext;
import io.github.AyushGhimire077.hibernate_postgres_rls.core.StartupValidation;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class RlsAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(RlsAutoConfiguration.class))
            .withBean(JdbcTemplate.class, () -> mock(JdbcTemplate.class))
            .withBean(DataSource.class, () -> mock(DataSource.class))
            .withBean(EntityManagerFactory.class, () -> mock(EntityManagerFactory.class));

//    @Test
//    void shouldNotLoadBeansIfDisabled() {
//        this.contextRunner.withPropertyValues("spring.rls.enabled=false")
//                .run(context -> {
//                    assertThat(context).doesNotHaveBean(RlsProperties.class);
//                    assertThat(context).doesNotHaveBean(RlsContext.class);
//                    assertThat(context).doesNotHaveBean(StartupValidation.class);
//                    assertThat(context).doesNotHaveBean(RlsTransactionalAspect.class);
//                });
//    }
//
//    @Test
//    void shouldLoadBeansIfEnabled() {
//        this.contextRunner.withPropertyValues("spring.rls.enabled=true")
//                .run(context -> {
//                    assertThat(context).hasSingleBean(RlsProperties.class);
//                    assertThat(context).hasSingleBean(RlsContext.class);
//                    assertThat(context).hasSingleBean(StartupValidation.class);
//                    assertThat(context).hasSingleBean(RlsTransactionalAspect.class);
//                });
//    }
}
