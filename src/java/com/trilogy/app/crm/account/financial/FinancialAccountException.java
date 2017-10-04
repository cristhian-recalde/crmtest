package com.trilogy.app.crm.account.financial;

/**
 * @author kkadam
 *
 */
public class FinancialAccountException extends Exception
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
     */
    public FinancialAccountException()
    {
        // EMPTY
    }

    /**
     * Creates a new FinancialAccountException.
     *
     * @param message The detail message.
     */
    public FinancialAccountException(final String message)
    {
        super(message);
    }

    /**
     * Creates a new FinancialAccountException.
     *
     * @param message The detail message.
     * @param cause The Throwable that caused this exception to be thrown.
     */
    public FinancialAccountException(final String message, final Throwable cause)
    {
        super(message, cause);
    }

    /**
     * Creates a new FinancialAccountException.
     *
     * @param cause The Throwable that caused this exception to be thrown.
     */
    public FinancialAccountException(final Throwable cause)
    {
        super(cause);
    }

}
