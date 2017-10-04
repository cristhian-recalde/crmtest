package com.trilogy.app.crm.transaction;


/**
 * Indicates that an exception occurred while validating the subscriber type for
 * a payment exception.
 * @author marcio.marques@redknee.com
 */
public class PrepaidPaymentException extends IllegalStateException
{
    
    public PrepaidPaymentException(String s)
    {
        super(s);
    }

    public PrepaidPaymentException(String s, Throwable t)
    {
        super(s, t);
    }

    public PrepaidPaymentException(Throwable t)
    {
        super(t);
    }    
}
