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
import com.trilogy.app.crm.support.Lookup;
import com.trilogy.app.crm.support.PricePlanSwitchLimitSupport;
import com.trilogy.app.urcs.promotion.v2_0.Counter;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xlog.log.InfoLogMsg;
import com.trilogy.framework.xlog.log.MinorLogMsg;

/**
 * Price Plan change counter incrementer.
 * This home increments the counter on URCS for given Subscription, for Price Plan Switch Limit
 * feature. The threshold validation is not done in this home. The increment should only happen
 * on GUI changes or API calls (looks for Lookup.PRICEPLAN_SWITCH_COUNTER_INCREMENT in context).
 * 
 * 
 * @author amahmood
 * @since 8.5
 * @see SgR.CRM.1619
 */
public class PricePlanSwitchCounterHome extends HomeProxy
{
    

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public PricePlanSwitchCounterHome(Home delegate)
    {
        super(delegate);
    }

    @Override
    public Object store(Context ctx, Object obj) throws HomeException
    {
        final Subscriber newSub = (Subscriber)obj;
        final Subscriber oldSub = (Subscriber) ctx.get(Lookup.OLDSUBSCRIBER);
        final Subscriber updatedSub;
         
        updatedSub = (Subscriber)super.store(ctx, newSub);
        

        try
        {
            if (oldSub != null && newSub != null &&
                    oldSub.getPricePlan() != newSub.getPricePlan() &&
                    ctx.getBoolean(Lookup.PRICEPLAN_SWITCH_COUNTER_INCREMENT, false))
            {
                Counter counter = PricePlanSwitchLimitSupport.incrementPricePlanSwitchCounter(ctx, updatedSub);
                
                final StringBuilder msg = new StringBuilder()
                    .append("Incremented PricePlanSwitchLimit counter instance for subscription = ")
                    .append(updatedSub.getId())
                    .append(", count = ")
                    .append(counter == null ? "null" : counter.value);            
                    
                new InfoLogMsg(this.getClass(), msg.toString(), null).log(ctx);
            }
        }
        catch (Exception e)
        {
            new MinorLogMsg(this.getClass(), "Error occured when attempting to update PricePlanSwitch count for sub=" 
                    + oldSub.getId(), e).log(ctx);
        }
        
        
        return updatedSub;
    }

}
