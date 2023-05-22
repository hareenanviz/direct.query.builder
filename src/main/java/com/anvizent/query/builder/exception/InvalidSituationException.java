package com.anvizent.query.builder.exception;

/**
 * @author Hareen Bejjanki
 * @author Apurva Deshmukh
 *
 */
public class InvalidSituationException extends Exception {

	private static final long serialVersionUID = 1L;

	public InvalidSituationException() {
		super();
	}

	public InvalidSituationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public InvalidSituationException(String message, Throwable cause) {
		super(message, cause);
	}

	public InvalidSituationException(String message) {
		super(message);
	}

	public InvalidSituationException(Throwable cause) {
		super(cause);
	}
}