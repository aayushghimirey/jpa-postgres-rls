package io.github.AyushGhimire077.hibernate_postgres_rls;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;

@SpringBootApplication // Required to start the context
// Activates your library
@EntityScan("io.github.AyushGhimire077.hibernate_postgres_rls")
public class TestApplication {
    public static void main(String[] args) {
        SpringApplication.run(TestApplication.class, args);
    }
}