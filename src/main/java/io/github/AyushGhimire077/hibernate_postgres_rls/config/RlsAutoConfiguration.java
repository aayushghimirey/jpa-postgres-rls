package io.github.AyushGhimire077.hibernate_postgres_rls.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(prefix = "spring.rls", name = "enabled", havingValue = "true")
public class RlsAutoConfiguration {

    private static final Logger log = LoggerFactory.getLogger(RlsAutoConfiguration.class);

    @Bean
    @ConfigurationProperties(prefix = "spring.rls")
    public RlsProperties rlsProperties() {
        return new RlsProperties();
    }


    @Bean
    public InitializingBean rlsInitializer(RlsProperties props) {
        return () -> {
            RlsRuntimeConfig.set(props);
            log.info("Setting props ----------------------------------- {}", props);
        };
    }
}
