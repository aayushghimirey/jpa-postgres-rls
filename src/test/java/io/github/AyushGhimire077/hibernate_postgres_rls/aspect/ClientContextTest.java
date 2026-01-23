
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
package io.github.AyushGhimire077.hibernate_postgres_rls.aspect;

import io.github.AyushGhimire077.hibernate_postgres_rls.exception.RlsSecurityException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class ClientContextTest {

    @AfterEach
    void tearDown() {
        ClientContext.clear();
    }

    @Test
    void shouldStoreAndRetrieveContext() {
        ClientContext.put("tenant_id", "123");
        ClientContext.put("user_role", "admin");

        assertEquals("123", ClientContext.get("tenant_id"));
        assertEquals("admin", ClientContext.get("user_role"));
        assertTrue(ClientContext.contains("tenant_id"));
        assertFalse(ClientContext.contains("non_existent"));
    }

    @Test
    void shouldReturnAllEntries() {
        ClientContext.put("key1", "val1");
        ClientContext.put("key2", "val2");

        Map<String, String> all = ClientContext.getAll();
        assertEquals(2, all.size());
        assertEquals("val1", all.get("key1"));
        assertEquals("val2", all.get("key2"));
    }

    @Test
    void shouldBeThreadIsolated() throws InterruptedException {
        ClientContext.put("main", "thread");

        CountDownLatch latch = new CountDownLatch(1);
        ExecutorService executor = Executors.newSingleThreadExecutor();

        executor.submit(() -> {
            assertNull(ClientContext.get("main"));
            ClientContext.put("worker", "thread");
            assertEquals("thread", ClientContext.get("worker"));
            latch.countDown();
        });

        assertTrue(latch.await(5, TimeUnit.SECONDS));
        assertNull(ClientContext.get("worker"));
        assertEquals("thread", ClientContext.get("main"));

        executor.shutdown();
    }

    @Test
    void shouldClearContext() {
        ClientContext.put("key", "value");
        ClientContext.clear();

        assertTrue(ClientContext.getAll().isEmpty());
        assertNull(ClientContext.get("key"));
    }

    @Test
    void shouldRejectInvalidInputs() {
        assertThrows(RlsSecurityException.class, () -> ClientContext.put("invalid key", "value"));
        assertThrows(RlsSecurityException.class, () -> ClientContext.put("key", "invalid value;"));
    }

    @Test
    void shouldReturnImmutableMap() {
        ClientContext.put("key", "value");
        Map<String, String> all = ClientContext.getAll();

        assertThrows(UnsupportedOperationException.class, () -> all.put("new", "val"));
    }
}
