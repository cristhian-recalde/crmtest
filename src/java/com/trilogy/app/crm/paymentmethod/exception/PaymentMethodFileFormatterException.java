package com.trilogy.app.crm.paymentmethod.exception;


/**
 * 
 * @author meenal.rastogi
 *
 */
public class PaymentMethodFileFormatterException extends Exception 
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private int code = 0;
	
	public PaymentMethodFileFormatterException(int _code)
	{
		this.code = _code;
	}

	public PaymentMethodFileFormatterException(String msg , int _code) 
	{
		super(msg);
		this.code = _code;
	}

	public PaymentMethodFileFormatterException(Throwable throwable , int _code) {
		super(throwable);
		this.code = _code;
	}

	public PaymentMethodFileFormatterException( String message, Throwable throwable) {
		super(message, throwable);
		
	}
	
	public PaymentMethodFileFormatterException(String msg, Throwable throwable , int _code) {
		super(msg, throwable);
		this.code = _code;
	}
	
	public PaymentMethodFileFormatterException(String message) {
		super(message);
	}

	public int getErrorCode()
	{
		return code;
	}
}

