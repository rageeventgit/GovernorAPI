package net.rageevent.governor.api.exceptions;

public class EnvelopeTimeoutException extends RuntimeException {
    public EnvelopeTimeoutException(String message) {
        super(message);
    }
}
