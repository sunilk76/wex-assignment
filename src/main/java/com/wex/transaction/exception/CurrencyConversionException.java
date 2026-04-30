package com.wex.transaction.exception;

public class CurrencyConversionException extends RuntimeException {

    private static final long serialVersionUID = -1901737229085797315L;

	public CurrencyConversionException(String message) {
        super(message);
    }
}
