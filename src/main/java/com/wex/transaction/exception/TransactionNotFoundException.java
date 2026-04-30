package com.wex.transaction.exception;

import java.util.UUID;

public class TransactionNotFoundException extends RuntimeException {

    private static final long serialVersionUID = 4320134367584159606L;

	public TransactionNotFoundException(UUID id) {
        super("Transaction not found: " + id);
    }
}
