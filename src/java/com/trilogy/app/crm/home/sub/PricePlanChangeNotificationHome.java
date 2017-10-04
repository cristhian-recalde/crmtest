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
package com.trilogy.app.crm.home.sub;

import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.priceplan.SecondaryPricePlanActivationAgent;
import com.trilogy.app.crm.support.Lookup;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xlog.log.MinorLogMsg;

/**
 * Sends Notification to subscriber as and when applicable on 
 * PricePlan change or PricePlanVersion change. 
 * 
 * @author sgaidhani
 * @since 9.5.1
 */
public class PricePlanChangeNotificationHome extends HomeProxy
{
    

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public PricePlanChangeNotificationHome(Home delegate)
    {
        super(delegate);
    }

    @Override
    public Object store(Context ctx, Object obj) throws HomeException
    {
        final Subscriber oldSub = (Subscriber) ctx.get(Lookup.OLDSUBSCRIBER);
         
        final Subscriber newSub = (Subscriber)super.store(ctx, obj);
        
        try
        {
            if (oldSub != null && newSub != null)
            {
            	//if PricePlan change has occurred. Look for the specific cause.
            	if(oldSub.getPricePlan() != newSub.getPricePlan())
            	{
                	
            		//If the change is related to Secondary PricePlan, look for it is primary to secondary or secondary to primary.
            		if(oldSub.getSecondaryPricePlan() != newSub.getSecondaryPricePlan())
            		{
            			//Secondary to primary case.
            			if(newSub.getSecondaryPricePlan() == SecondaryPricePlanActivationAgent.DEFAULT_SECONDARY_PRICE_PLAN_ID)
            			{
            				com.redknee.app.crm.support.PricePlanNotificationSupport.sendSecondarytoPrimaryNotification(ctx, oldSub, newSub);
            			}
            			//Primary to secondary case.
            			else
            			{
            				//Ideally a new method should be written for Primary to Secondary price plan switch.
            				com.redknee.app.crm.support.PricePlanNotificationSupport.sendSecondarytoPrimaryNotification(ctx, oldSub, newSub);
            			}
            		}
            		//else if(...)
            		//In future, normal price plan switch (only primary) can be handled.
            		//In future, normal price plan version change can be handled.
            		//In future, normal price plan scheduled switch can be handled.
            	}
                
            }
        }
        catch (Exception e)
        {
            new MinorLogMsg(this.getClass(), "Error occured when attempting to send PricePlan Change notification to sub=" 
                    + oldSub.getId(), e).log(ctx);
        }
        return newSub;
    }

}
