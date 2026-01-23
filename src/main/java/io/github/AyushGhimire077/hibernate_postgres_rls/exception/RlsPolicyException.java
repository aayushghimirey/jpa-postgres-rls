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

package io.github.AyushGhimire077.hibernate_postgres_rls.exception;

/**
 * Exception thrown when there is an error with RLS policy creation, update, or management.
 *
 * @author Aayush Ghimire
 */
public class RlsPolicyException extends RlsException {

    public RlsPolicyException(String message) {
        super(message);
    }

    public RlsPolicyException(String message, Throwable cause) {
        super(message, cause);
    }
}
