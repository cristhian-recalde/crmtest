package com.trilogy.app.crm.transaction;

public class PaymentDistributionException extends Exception
{
	public PaymentDistributionException(String msg,int reason)
	{
		super(msg);
		setReason(reason);
	}
	
	public PaymentDistributionException(String msg,Throwable t,int reason)
	{
		super(msg,t);
		setReason(reason);
	}
	
	public PaymentDistributionException(Throwable t,int reason)
	{
		super(t);
		setReason(reason);
	}
	
	private int reason;

	public int getReason() {
		return reason;
	}

	protected void setReason(int reason) {
		this.reason = reason;
	}
}
