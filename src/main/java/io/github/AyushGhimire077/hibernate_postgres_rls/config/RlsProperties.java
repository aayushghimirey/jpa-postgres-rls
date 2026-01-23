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

import io.github.AyushGhimire077.hibernate_postgres_rls.enums.RlsMode;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for Row Level Security (RLS).
 * <p>
 * This class binds to properties prefixed with {@code spring.rls} in the
 * application's configuration files (e.g., {@code application.properties}
 * or {@code application.yml}).
 * </p>
 *
 * <h3>Example Configuration:</h3>
 * <pre>{@code
 * spring.rls.enabled=true
 * spring.rls.mode=enforce
 * }</pre>
 *
 * @author Aayush Ghimire
 */
@ConfigurationProperties(prefix = "spring.rls")
public class RlsProperties {

    /**
     * Master switch for Row Level Security support.
     * When true, the library will scan entities and apply RLS DDL during schema generation.
     */
    private boolean enabled = false;

    /**
     * The operation mode for RLS management.
     * <ul>
     *   <li>{@code VALIDATE}: Only checks for the presence of RLS on tables.</li>
     *   <li>{@code ENFORCE}: Generates and executes RLS DDL (ENABLE, FORCE, CREATE POLICY).</li>
     * </ul>
     */
    private RlsMode mode = RlsMode.VALIDATE;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public RlsMode getMode() {
        return mode;
    }

    public void setMode(RlsMode mode) {
        this.mode = mode;
    }
}
