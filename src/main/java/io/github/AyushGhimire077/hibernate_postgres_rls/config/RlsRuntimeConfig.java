package io.github.AyushGhimire077.hibernate_postgres_rls.config;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 *  Hibernate doesn't know about spring beans so injection will not working
 *   so runtime config is needed
 *
 *   // bridge between Spring and Hibernate
 * */
public class RlsRuntimeConfig {

    private static final Logger log = LoggerFactory.getLogger(RlsRuntimeConfig.class);
    private static volatile RlsProperties properties;

    public static void set(RlsProperties props) {
        log.info("Setting props ----------------------------------- {}", props);
        properties = props;
    }

    public static RlsProperties get() {
        return properties;
    }

}
