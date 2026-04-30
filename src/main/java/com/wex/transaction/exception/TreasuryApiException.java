package com.wex.transaction.exception;

public class TreasuryApiException extends RuntimeException {

    private static final long serialVersionUID = 9161457625922791403L;

	public TreasuryApiException(String message, Throwable cause) {
        super(message, cause);
    }
}
