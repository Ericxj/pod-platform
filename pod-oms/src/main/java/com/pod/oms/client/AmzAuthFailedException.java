package com.pod.oms.client;

/**
 * Thrown when Amazon SP-API returns 401/403 so the job can create CHANNEL_DATA hold (AMZ_AUTH_FAILED).
 */
public class AmzAuthFailedException extends RuntimeException {

    private final int httpStatus;

    public AmzAuthFailedException(int httpStatus, String message) {
        super(message);
        this.httpStatus = httpStatus;
    }

    public AmzAuthFailedException(int httpStatus, String message, Throwable cause) {
        super(message, cause);
        this.httpStatus = httpStatus;
    }

    public int getHttpStatus() {
        return httpStatus;
    }
}
