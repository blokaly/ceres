package com.blokaly.ceres.bitfinex;

public class ApiException extends Exception {

    private static final long serialVersionUID = -7200375305921643828L;

    public ApiException() {
    }

    public ApiException(final String message) {
        super(message);
    }

    public ApiException(final Throwable cause) {
        super(cause);
    }

    public ApiException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public ApiException(final String message, final Throwable cause, final boolean enableSuppression, final boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
