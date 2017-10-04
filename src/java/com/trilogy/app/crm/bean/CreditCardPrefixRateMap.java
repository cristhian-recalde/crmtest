package com.trilogy.app.crm.bean;

/**
 * 
 * @author <a href='mailto:ameya.bhurke@redknee.com'>Ameya Bhurke</a>
 *
 */
public class CreditCardPrefixRateMap extends AbstractCreditCardPrefixRateMap {

	public CreditCardPrefixRateMap()
	{
		
	}
	
	public TaxableTopUp getTotalTopUp(long amount, boolean prepaid)
	{
		return new TaxableTopUp(amount, getTaxRate(), prepaid);
	}
}
