package com.trilogy.app.crm.bean;

/**
 * 
 * TaxableTopUp class has 2 properties amount and tax. It exposes methods to compute
 * amounts chargeable to PaymentGateway/CreditCard and airtime balance amount to be credited 
 * to a subscriber profile.
 * 
 * @author <a href='mailto:ameya.bhurke@redknee.com'>Ameya Bhurke</a>
 *
 */
public class TaxableTopUp {

	private final long amount;
	private final double percentTax;
	private final boolean prepaid;
	
	public TaxableTopUp(long amount, double percentTax, boolean prepaid)
	{
		this.amount = amount;
		this.percentTax = percentTax;
		this.prepaid = prepaid;
	}
	
	public boolean isPrepaidSubscriber()
	{
		return this.prepaid;		
	}
	
	public long getAmount() 
	{
		return amount;
	}

	public double getPercentTax() 
	{
		return percentTax;
	}
	
	public long getTaxAmount()
	{
		if(isPrepaidSubscriber())
		{
			return (long) Math.round(((double)getAmount() * (getPercentTax() / 100) ));
		}
		else
		{
			return 0;
		}
	}
	
	public long getPGChargeableAmount()
	{	
		return getTaxAmount() + getAmount();
	}
	
	public long getSubscriberBalanceCrAmount()
	{
		return -1 * getAmount();
	}
}
