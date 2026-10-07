package io.felixtech.teltonika.tachosync;

final class Utils {
    static Throwable getCause(Throwable t) {
        Throwable cause = t;

        while (cause.getCause() != null) {
            cause = cause.getCause();
        }

        return cause;
    }
}
