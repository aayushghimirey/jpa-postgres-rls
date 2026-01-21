package io.github.AyushGhimire077.hibernate_postgres_rls.config;

import org.springframework.boot.context.properties.ConfigurationProperties;


/*
 *  Config holder from application.yml or application.properties
 *
 *   Eg :
 *   spring.rls.enabled= true;
 *   spring.rls.mode= "validate"
 * */
@ConfigurationProperties(prefix = "spring.rls")
public class RlsProperties {

    private boolean enabled = false;
    private String mode = "validate";

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getMode() {
        return mode;
    }

    @Override
    public String toString() {
        return "RlsProperties{" +
                "enabled=" + enabled +
                ", mode='" + mode + '\'' +
                '}';
    }

    public void setMode(String mode) {
        this.mode = mode;
    }
}
