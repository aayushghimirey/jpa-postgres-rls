/*
 * Copyright (C) 2026 Aayush Ghimire
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package io.github.AyushGhimire077.hibernate_postgres_rls.config;

import io.github.AyushGhimire077.hibernate_postgres_rls.aspect.RlsTransactionalAspect;
import io.github.AyushGhimire077.hibernate_postgres_rls.filter.RlsFilter;
import io.github.AyushGhimire077.hibernate_postgres_rls.users.RlsUserManager;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class RlsAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(RlsAutoConfiguration.class))
            .withBean(JdbcTemplate.class, () -> mock(JdbcTemplate.class));

    @Test
    void shouldNotLoadBeansIfDisabled() {
        this.contextRunner.withPropertyValues("spring.rls.enabled=false")
                .run(context -> {
                    assertThat(context).doesNotHaveBean(RlsProperties.class);
                    assertThat(context).doesNotHaveBean(RlsUserManager.class);
                    assertThat(context).doesNotHaveBean(RlsFilter.class);
                    assertThat(context).doesNotHaveBean(RlsTransactionalAspect.class);
                });
    }

    @Test
    void shouldLoadBeansIfEnabled() {
        this.contextRunner.withPropertyValues("spring.rls.enabled=true")
                .run(context -> {
                    assertThat(context).hasSingleBean(RlsProperties.class);
                    assertThat(context).hasSingleBean(RlsUserManager.class);
                    assertThat(context).hasSingleBean(RlsFilter.class);
                    assertThat(context).hasSingleBean(RlsTransactionalAspect.class);
                });
    }

    @Test
    void shouldConfigureCustomProperties() {
        this.contextRunner.withPropertyValues(
                "spring.rls.enabled=true",
                "spring.rls.mode=VALIDATE"
        ).run(context -> {
            RlsProperties properties = context.getBean(RlsProperties.class);
            assertThat(properties.isEnabled()).isTrue();
            assertThat(properties.getMode()).isEqualTo(io.github.AyushGhimire077.hibernate_postgres_rls.enums.RlsMode.VALIDATE);
        });
    }
}
