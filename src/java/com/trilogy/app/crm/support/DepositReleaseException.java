package com.trilogy.app.crm.support;

public class DepositReleaseException  extends Exception
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
     */
    public DepositReleaseException()
    {
        // EMPTY
    }

    /**
     * Creates a new DepositReleaseException.
     *
     * @param message The detail message.
     */
    public DepositReleaseException(final String message)
    {
        super(message);
    }

    /**
     * Creates a new DepositReleaseException.
     *
     * @param message The detail message.
     * @param cause The Throwable that caused this exception to be thrown.
     */
    public DepositReleaseException(final String message, final Throwable cause)
    {
        super(message, cause);
    }

    /**
     * Creates a new DepositReleaseException.
     *
     * @param cause The Throwable that caused this exception to be thrown.
     */
    public DepositReleaseException(final Throwable cause)
    {
        super(cause);
    }

}
