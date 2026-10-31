package net.rageevent.governor.api.exceptions;

import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;

public class GovernorException extends RuntimeException {
    public GovernorException(String message) {
        super(message);
    }
    public GovernorException(String message, Throwable cause){
        super(message,cause);
    }

    public static RuntimeException propagate(Throwable error) {
        Throwable cause = unwrap(error);
        if (cause instanceof RuntimeException runtime) return runtime;
        return new GovernorException(cause.getClass().getSimpleName() + ": " + cause.getMessage(), cause);
    }

    /** Strips {@link CompletionException} and {@link ExecutionException} wrappers. */
    public static Throwable unwrap(Throwable error) {
        Throwable cause = error;
        while ((cause instanceof CompletionException || cause instanceof ExecutionException) && cause.getCause() != null) {
            cause = cause.getCause();
        }
        return cause;
    }
}
