package com.anvizent.query.builder.exception;

/**
 * @author Hareen Bejjanki
 * @author Apurva Deshmukh
 *
 */
public class UnderConstructionException extends Exception {

	private static final long serialVersionUID = 1L;

	public UnderConstructionException() {
		super();
	}

	public UnderConstructionException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public UnderConstructionException(String message, Throwable cause) {
		super(message, cause);
	}

	public UnderConstructionException(String message) {
		super(message);
	}

	public UnderConstructionException(Throwable cause) {
		super(cause);
	}

}
