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
 * Base exception for all Row-Level Security (RLS) related errors.
 * <p>
 * Can be used to wrap database binding issues, misconfigurations, or validation failures.
 * </p>
 * <p>
 * Example usage:
 * <pre>
 * if (!rlsPolicyValid) {
 *     throw new RlsException("RLS policy is missing required session variable");
 * }
 * </pre>
 * </p>
 *
 * @author Aayush
 * @since 2026
 */
public class RlsException extends RuntimeException {

    /**
     * Constructs a new RlsException with the specified detail message.
     *
     * @param message the detail message
     */
    public RlsException(String message) {
        super(message);
    }

    /**
     * Constructs a new RlsException with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause   the cause
     */
    public RlsException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new RlsException with the specified cause.
     *
     * @param cause the cause
     */
    public RlsException(Throwable cause) {
        super(cause);
    }
}
