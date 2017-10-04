package com.trilogy.app.crm.paymentmethod.exception;

/**
 * 
 * @author meenal.rastogi
 * @since 10.2
 *
 */
public class PaymentMethodProcessorException extends Exception 
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private int code = 0;
	
	public PaymentMethodProcessorException(int _code)
	{
		this.code = _code;
	}

	public PaymentMethodProcessorException(String msg , int _code) 
	{
		super(msg);
		this.code = _code;
	}

	public PaymentMethodProcessorException(Throwable throwable , int _code) {
		super(throwable);
		this.code = _code;
	}

	public PaymentMethodProcessorException( String message, Throwable throwable) {
		super(message, throwable);
		
	}
	
	public PaymentMethodProcessorException(String msg, Throwable throwable , int _code) {
		super(msg, throwable);
		this.code = _code;
	}
	
	public PaymentMethodProcessorException(String message) {
		super(message);
	}

	public int getErrorCode()
	{
		return code;
	}

}
