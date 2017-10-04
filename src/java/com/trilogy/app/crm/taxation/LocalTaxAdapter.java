package com.trilogy.app.crm.taxation;

import java.util.Collection;
import java.util.Iterator;

import billsoft.eztax.ZipAddress;

import com.trilogy.app.crm.bean.CreditCardPrefixRateMap;
import com.trilogy.app.crm.bean.CreditCardPrefixRateMapHome;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.TaxableTopUp;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;


public class LocalTaxAdapter implements TaxAdapter
{

    @Override
    public long calculatePaymentTax(Context ctx, int spid, String msisdn, long amount,ZipAddress zipAddress ) throws HomeException
    {
        return getTotalTopUp(ctx, spid, msisdn, amount).getTaxAmount();
    }
    
    public static final TaxableTopUp getTotalTopUp(Context ctx , Subscriber subscriber, long amount) throws HomeException
    {
    	Home prefixRateMapHome = (Home)ctx.get(CreditCardPrefixRateMapHome.class);
    	Collection<CreditCardPrefixRateMap> prefixRateMapCollection = prefixRateMapHome.selectAll();
    	Iterator<CreditCardPrefixRateMap> prefixRateMapIterator = prefixRateMapCollection.iterator();
    	
    	while ( prefixRateMapIterator.hasNext() )
    	{
    		CreditCardPrefixRateMap prefixRateMap = prefixRateMapIterator.next();
    		
    		if (subscriber.getMsisdn().startsWith(prefixRateMap.getMsisdnPrefix()) 
            		//Validation is skipped on SPID for this release, as it is an Optional 
            		//Parameter in SubscriptionReference.
    				//&& prefixRateMap.getSpid() == subscriber.getSpid() 
    				)
    		{
    			return prefixRateMap.getTotalTopUp(amount, subscriber.isPrepaid());
    		}        			
    	}    	
    	
    	return new TaxableTopUp(amount, 0.0 , true);
    }
    
    public static final TaxableTopUp getTotalTopUp(Context ctx ,  int spid, String msisdn, long amount) throws HomeException
    {
    	Home prefixRateMapHome = (Home)ctx.get(CreditCardPrefixRateMapHome.class);
    	Collection<CreditCardPrefixRateMap> prefixRateMapCollection = prefixRateMapHome.selectAll();
    	Iterator<CreditCardPrefixRateMap> prefixRateMapIterator = prefixRateMapCollection.iterator();

    	while ( prefixRateMapIterator.hasNext() )
    	{
    		CreditCardPrefixRateMap prefixRateMap = prefixRateMapIterator.next();

    		if (msisdn!= null && msisdn.startsWith(prefixRateMap.getMsisdnPrefix())
            		//Validation is skipped on SPID for this release, as it is an Optional 
            		//Parameter in SubscriptionReference.
    				//&& prefixRateMap.getSpid() == subscriber.getSpid() 
    			)
    		{
    			return prefixRateMap.getTotalTopUp(amount, true);  
    		}  
    	}

    	return new TaxableTopUp(amount, 0.0 , true);
    }
}
