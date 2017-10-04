/*
 * This code is a protected work and subject to domestic and international
 * copyright law(s).  A complete listing of authors of this work is readily
 * available.  Additionally, source code is, by its very nature, confidential
 * information and inextricably contains trade secrets and other information
 * proprietary, valuable and sensitive to Redknee.  No unauthorized use,
 * disclosure, manipulation or otherwise is permitted, and may only be used
 * in accordance with the terms of the license agreement entered into with
 * Redknee Inc. and/or its subsidiaries.
 *
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.subscriber.provision;

import java.util.Iterator;
import java.util.List;

import com.trilogy.app.crm.bean.AuxiliaryServiceTypeEnum;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.core.AuxiliaryService;
import com.trilogy.app.crm.client.TFAAuxiliarServiceClientException;
import com.trilogy.app.crm.support.Lookup;
import com.trilogy.framework.xhome.beans.SafetyUtil;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xlog.log.LogSupport;


/*
 * TFA Home class to update TFA on subscriber updation.
 * Primary use is when MSISDN changes 
 * @author bhagyashree.dhavalshankh@redknee.com
 * 
 */
public class TFAAuxServiceProvisionUpdationHome extends HomeProxy {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public TFAAuxServiceProvisionUpdationHome(Context ctx, Home home)
	{
		super(home); 
	}
	
		
    @Override
    public Object store(Context ctx, Object obj) throws HomeException
    {
    	LogSupport.debug(ctx, this, "SubscriberPipeline[store].....");
    	final Subscriber sub = (Subscriber)obj;
    	final Subscriber oldSub = (Subscriber) ctx.get(Lookup.OLDSUBSCRIBER); 	
    	
    	boolean msisdnChanged = oldSub != null	&& !SafetyUtil.safeEquals(oldSub.getMSISDN(), sub.getMSISDN());
    	boolean updateTFA = false;

        if(msisdnChanged)
        {
        	List<com.redknee.app.crm.bean.core.custom.AuxiliaryService> subAuxServices = TFAAuxServiceSupport.getTFAAuxiliaryServices(ctx, oldSub);
        	try
        	{
        		if(subAuxServices != null)
        		{
	        		Iterator<com.redknee.app.crm.bean.core.custom.AuxiliaryService> it = subAuxServices.iterator();
	        		while (it.hasNext()) 
	        		{
	        			AuxiliaryService auxService = it.next();  
	        			
	        			AuxiliaryServiceTypeEnum auxServiceType = auxService.getType();
	        			
	        			if(auxServiceType == AuxiliaryServiceTypeEnum.TFA){
	        				updateTFA = true;
	        				break;
	        			}
	        		}
	        		if(updateTFA)
	        			TFAAuxServiceSupport.changeMsisdn(ctx, oldSub, sub);
        		}
        	}
        	catch (TFAAuxiliarServiceClientException e) {
       			throw new HomeException(e);
       		}
       	}

        return super.store(ctx, obj);
    }
    
}
