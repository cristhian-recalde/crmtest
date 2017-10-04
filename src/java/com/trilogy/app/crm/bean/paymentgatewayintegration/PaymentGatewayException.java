package com.trilogy.app.crm.bean.paymentgatewayintegration;

public class PaymentGatewayException extends Exception {

	private int code = 0;
	
	public PaymentGatewayException(int _code)
	{
		this.code = _code;
	}

	public PaymentGatewayException(String arg0 , int _code) 
	{
		super(arg0);
		this.code = _code;
	}

	public PaymentGatewayException(Throwable arg0 , int _code) {
		super(arg0);
		this.code = _code;
	}

	public PaymentGatewayException(String arg0, Throwable arg1 , int _code) {
		super(arg0, arg1);
		this.code = _code;
	}
	
	public int getErrorCode()
	{
		return code;
	}

}
