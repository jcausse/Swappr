package ar.edu.itba.paw.service.exception;

public class SelfSuspendException extends Exception {

    public SelfSuspendException(String message) {
        super(message);
    }

    public SelfSuspendException(String message, Throwable cause) {
        super(message, cause);
    }
}
