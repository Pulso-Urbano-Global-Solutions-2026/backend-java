package br.com.pulsourbano.exception;

public class IngestaoException extends RuntimeException {
    public IngestaoException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
