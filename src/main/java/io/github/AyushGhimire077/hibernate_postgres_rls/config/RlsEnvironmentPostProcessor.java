package io.github.AyushGhimire077.hibernate_postgres_rls.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;

public class RlsEnvironmentPostProcessor implements EnvironmentPostProcessor {

    private static final Logger log = LoggerFactory.getLogger(RlsEnvironmentPostProcessor.class);

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        Boolean isEnabled = environment.getProperty("spring.rls.enabled", Boolean.class, false);
        String mode = environment.getProperty("spring.rls.mode", String.class, "validate");

        if (isEnabled) {
            System.setProperty("hibernate.rls.enabled", "true");
            System.setProperty("hibernate.rls.mode", mode);
        }
    }
}
