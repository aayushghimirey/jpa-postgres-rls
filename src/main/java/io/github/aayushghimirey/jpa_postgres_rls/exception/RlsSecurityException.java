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

package io.github.aayushghimirey.jpa_postgres_rls.exception;

/**
 * Exception thrown for security-related Row-Level Security (RLS) issues,
 * such as invalid table, policy, or session variable identifiers.
 * <p>
 * Extends {@link RlsException} and is intended to indicate potential security
 * or misconfiguration problems in RLS usage.
 * </p>
 *
 * <p>
 * Example usage:
 * <pre>
 * if (!SqlIdentifierValidator.isValid(tableName)) {
 *     throw new RlsSecurityException("Invalid RLS table identifier: " + tableName);
 * }
 * </pre>
 * </p>
 *
 * @author Aayush Ghimire
 * @since 2026
 */
public class RlsSecurityException extends RlsException {

    /**
     * Constructs a new RlsSecurityException with the specified detail message.
     *
     * @param message the detail message
     */
    public RlsSecurityException(String message) {
        super(message);
    }

}
