package io.github.aayushghimirey.jpa_postgres_rls.exception;

public class RlsConfigurationException extends RlsException{
    public RlsConfigurationException(String message) {
        super(message);
    }

    public RlsConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }

    public RlsConfigurationException(Throwable cause) {
        super(cause);
    }
}
