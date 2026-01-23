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

package io.github.AyushGhimire077.hibernate_postgres_rls.examples;

import io.github.AyushGhimire077.hibernate_postgres_rls.annotation.RlsRule;
import io.github.AyushGhimire077.hibernate_postgres_rls.annotation.RowLevelSecurity;
import io.github.AyushGhimire077.hibernate_postgres_rls.enums.PolicyType;
import jakarta.persistence.*;

/**
 * Example entity demonstrating the RAW SQL CONDITION style of RLS.
 * <p>
 * This approach uses raw SQL conditions for maximum flexibility.
 * This is useful when you need complex logic that doesn't fit standard patterns.
 * </p>
 * <p>
 * The library will create a policy like:
 * <pre>
 * CREATE POLICY user_documents_custom_policy ON user_documents FOR ALL
 * USING ( user_id = current_setting('app.user_id')::bigint AND status = 'active' )
 * WITH CHECK ( user_id = current_setting('app.user_id')::bigint AND status = 'active' )
 * </pre>
 * </p>
 */
@Entity
@Table(name = "user_documents")
@RowLevelSecurity(force = true)
@RlsRule(
        using = "user_id = current_setting('app.user_id')::bigint AND status = 'active'",
        withCheck = "user_id = current_setting('app.user_id')::bigint AND status = 'active'",
        name = "user_documents_custom_policy",
        policyType = PolicyType.ALL
)
public class UserDocumentsCustomCondition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "document_name")
    private String documentName;

    @Column(name = "content")
    private String content;

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDocumentName() {
        return documentName;
    }

    public void setDocumentName(String documentName) {
        this.documentName = documentName;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
