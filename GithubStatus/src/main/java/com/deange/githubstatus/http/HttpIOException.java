package com.deange.githubstatus.http;

import java.io.IOException;

public class HttpIOException extends IOException {

    private int mStatusCode;

    public HttpIOException(final int statusCode) {
        super();
        setStatusCode(statusCode);
    }

    public HttpIOException(final String detailMessage, final int statusCode) {
        super(detailMessage);
        setStatusCode(statusCode);
    }

    public HttpIOException(final Throwable cause, final int statusCode) {
        super(cause);
        setStatusCode(statusCode);
    }

    public HttpIOException(final String message, final Throwable cause, final int statusCode) {
        super(message, cause);
        setStatusCode(statusCode);
    }

    private void setStatusCode(final int statusCode) {
        mStatusCode = statusCode;
    }

    public int getStatusCode() {
        return mStatusCode;
    }

    public boolean isInformational() {
        return mStatusCode >= 100 && mStatusCode <= 199;
    }

    public boolean isSuccess() {
        return mStatusCode >= 200 && mStatusCode <= 299;
    }

    public boolean isRedirection() {
        return mStatusCode >= 300 && mStatusCode <= 399;
    }

    public boolean isClientError() {
        return mStatusCode >= 400 && mStatusCode <= 499;
    }

    public boolean isServerError() {
        return mStatusCode >= 500 && mStatusCode <= 599;
    }

}
