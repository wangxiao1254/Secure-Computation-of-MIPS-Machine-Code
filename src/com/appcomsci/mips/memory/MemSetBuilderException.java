/**
 * 
 */
package com.appcomsci.mips.memory;

/**
 * Internal error thrown by MemSetBuilder
 * @author mcintosh
 *
 */
@SuppressWarnings("serial")
public class MemSetBuilderException extends Exception {

	/**
	 * 
	 */
	public MemSetBuilderException() {
		super();
	}

	/**
	 * @param message
	 */
	public MemSetBuilderException(String message) {
		super(message);
	}

	/**
	 * @param cause
	 */
	public MemSetBuilderException(Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public MemSetBuilderException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * @param message
	 * @param cause
	 * @param enableSuppression
	 * @param writableStackTrace
	 */
	public MemSetBuilderException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
