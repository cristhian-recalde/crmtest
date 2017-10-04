package com.trilogy.app.crm.move.processor;

/**
 * @author vgote
 * @since 9.5
 *
 */
public class AccountMoveException extends Exception
{
	/**
     * Creates a new WriteOffTransactionGenerationException.
     */
    public AccountMoveException()
    {
        // EMPTY
    }

    /**
     * Creates a new AccountMoveException.
     *
     * @param message The detail message.
     */
    public AccountMoveException(final String message)
    {
        super(message);
    }

    /**
     * Creates a new AccountMoveException.
     *
     * @param message The detail message.
     * @param cause The Throwable that caused this exception to be thrown.
     */
    public AccountMoveException(final String message, final Throwable cause)
    {
        super(message, cause);
    }

    /**
     * Creates a new AccountMoveException.
     *
     * @param cause The Throwable that caused this exception to be thrown.
     */
    public AccountMoveException(final Throwable cause)
    {
        super(cause);
    }

}
