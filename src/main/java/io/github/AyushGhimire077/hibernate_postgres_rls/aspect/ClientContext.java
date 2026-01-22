package io.github.AyushGhimire077.hibernate_postgres_rls.aspect;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ClientContext {

    private static final ThreadLocal<Map<String, String>> CONTEXT =
            ThreadLocal.withInitial(HashMap::new);

    public static void put(String key, String value) {
        CONTEXT.get().put(key, value);
    }

    public static Map<String, String> getAll() {
        Map<String, String> map = CONTEXT.get();
        return (map != null) ? map : Collections.emptyMap();
    }

    public static void clear() {
        CONTEXT.get().clear();
        CONTEXT.remove();
    }

    private ClientContext() {
    }


}
